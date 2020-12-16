
package org.exoplatform.commons.dlp.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.dlp.processor.DlpOperationProcessor;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@DisallowConcurrentExecution
public class DlpJob implements InterruptableJob {
  private static final Log LOGGER = ExoLogger.getExoLogger(DlpJob.class);
  
  private DlpOperationProcessor dlpOperationProcessor;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ExoFeatureService featureService = CommonsUtils.getService(ExoFeatureService.class);
    if (featureService.isActiveFeature(DlpOperationProcessor.DLP_FEATURE)) {
      LOGGER.debug("Running dlp job");
      getDlpOperationProcessor().process();
    }
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    LOGGER.debug("Interrupting dlp job");
    getDlpOperationProcessor().interrupt();
  }

  private DlpOperationProcessor getDlpOperationProcessor() {
    if(dlpOperationProcessor == null) {
      dlpOperationProcessor = CommonsUtils.getService(DlpOperationProcessor.class);
    }
    return dlpOperationProcessor;
  }
}