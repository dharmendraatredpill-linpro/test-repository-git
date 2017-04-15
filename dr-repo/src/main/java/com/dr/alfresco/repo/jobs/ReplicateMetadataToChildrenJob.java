package com.dr.alfresco.repo.jobs;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class ReplicateMetadataToChildrenJob  extends AbstractScheduledLockedJob implements StatefulJob {

  @Override
  public void executeJob(JobExecutionContext context) throws JobExecutionException {
      JobDataMap jobData = context.getJobDetail().getJobDataMap();

      // Extract the Job executer to use
      Object executerObj = jobData.get("replicateMetadataToChildren");
      if (executerObj == null || !(executerObj instanceof ReplicateMetadataToChildren)) {
          throw new AlfrescoRuntimeException(
                  "ScheduledJob data must contain valid 'Executer' reference");
      }

      final ReplicateMetadataToChildren jobExecuter = (ReplicateMetadataToChildren) executerObj;

      AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
          public Object doWork() throws Exception {
              jobExecuter.execute();
              return null;
          }
      }, AuthenticationUtil.getSystemUserName());
  }


}
