/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.commons.api.notification.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.picocontainer.Startable;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

public class NotificationCompletionService implements Startable {

  private final String THREAD_NUMBER_KEY = "thread-number";

  private final String ASYNC_EXECUTION_KEY = "async-execution";
  
  private final String KEEP_ALIVE_TIME = "keepAliveTime";

  private Executor executor;

  //store runnable to process notification
  private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

  private final boolean DEFAULT_ASYNC_EXECUTION = true;

  private int configThreadNumber;
  
  private int keepAliveTime;

  private boolean configAsyncExecution;

  // Set on the threads of the pool only
  private final ThreadLocal<Boolean> poolThread = new ThreadLocal<>();

  public NotificationCompletionService(InitParams params) {

    //
    ValueParam threadNumberValue = params.getValueParam(THREAD_NUMBER_KEY);
    ValueParam asyncExecution = params.getValueParam(ASYNC_EXECUTION_KEY);
    ValueParam aliveTime = params.getValueParam(KEEP_ALIVE_TIME);

    //
    try {
      configThreadNumber = Integer.parseInt(threadNumberValue.getValue());
    } catch (Exception e) {
      configThreadNumber = 0;
    }

    //
    try {
      keepAliveTime = Integer.parseInt(aliveTime.getValue());
    } catch (Exception e) {
      keepAliveTime = 10;
    }

    //
    try {
      configAsyncExecution = Boolean.parseBoolean(asyncExecution.getValue());
    } catch (Exception e) {
      configAsyncExecution = DEFAULT_ASYNC_EXECUTION;
    }
    
    // A missing, invalid or non-positive thread-number means one thread per available processor
    int threadNumber = configThreadNumber > 0 ? configThreadNumber : Runtime.getRuntime().availableProcessors();

    ThreadFactory threadFactory = new ThreadFactory() {
      public Thread newThread(Runnable runable) {
        Thread t = new Thread(() -> {
          poolThread.set(Boolean.TRUE);
          runable.run();
        }, "Notification-Thread");
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
      }
    };
    //
    if (configAsyncExecution) {
      executor = new ThreadPoolExecutor(threadNumber, threadNumber, keepAliveTime, 
                                              TimeUnit.SECONDS, workQueue, threadFactory);
      ((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);
    } else {
      executor = new DirectExecutor();
    }
  }

  /**
   * Runs the task, on the pool when the execution is asynchronous. Neither its
   * result nor an exception it lets escape is kept: a task logs its own errors.
   *
   * @param callable the task to run
   */
  @SuppressWarnings("unchecked")
  public void addTask(Callable callable) {
    executor.execute(new FutureTask<>(callable));
  }

  public void waitCompletionFinished() {
    try {
      if (executor instanceof ExecutorService) {
        ((ExecutorService) executor).awaitTermination(1, TimeUnit.SECONDS);
      }
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isAsync() {
    return configAsyncExecution;
  }

  /**
   * @return true when the current thread is a thread of the pool, false on any
   *         other thread, the caller of a synchronous execution included
   */
  public boolean isPoolThread() {
    return Boolean.TRUE.equals(poolThread.get());
  }

  private class DirectExecutor implements Executor {

    public void execute(final Runnable runnable) {
      if (Thread.interrupted()) throw new RuntimeException();

      runnable.run();
    }
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
    if(executor instanceof ExecutorService) {
      ((ExecutorService) executor).shutdown();
    }
  }
}
