/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

    package org.exoplatform.commons.search.job;

import org.exoplatform.commons.search.index.IndexingOperationProcessor;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@DisallowConcurrentExecution
public class BulkIndexingJob implements InterruptableJob {
  private static final Log LOG = ExoLogger.getExoLogger(BulkIndexingJob.class);

  private IndexingOperationProcessor indexingOperationProcessor;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LOG.debug("Running job BulkIndexingJob");
    long startTime = System.currentTimeMillis();
    getIndexingOperationProcessor().process();
    long duration = System.currentTimeMillis() - startTime;
    if (duration > 60000) {
      LOG.info("End running BulkIndexingJob in {}ms", duration);
    } else {
      LOG.debug("End running BulkIndexingJob in {}ms", duration);
    }
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    LOG.debug("Interrupting job BulkIndexingJob");
    getIndexingOperationProcessor().interrupt();
  }

  private IndexingOperationProcessor getIndexingOperationProcessor() {
    if(indexingOperationProcessor == null) {
      indexingOperationProcessor = CommonsUtils.getService(IndexingOperationProcessor.class);
    }
    return indexingOperationProcessor;
  }
}