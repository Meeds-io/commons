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
package org.exoplatform.commons.search.job;

import org.exoplatform.commons.search.index.IndexingOperationProcessor;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.*;

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