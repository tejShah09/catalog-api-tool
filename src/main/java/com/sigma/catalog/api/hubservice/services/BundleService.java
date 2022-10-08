package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class BundleService extends AbstractShellProcessService {

        BundleService() {
                jobCategory = JOBKeywords.BUNDLE;             
        }

        public void startASyncProcessing(JobProperites properites) throws TalendException {


                if (!checkIfTableEmpty(properites, properites.jobId + "_" + jobCategory + "_Enddate_InputSheet",
                                JOBKeywords.EDIT_FILE_ROW_COUNT)) {
                        changeWorkFlowStatus(properites, "DeleteNonLive");
                        changeWorkFlowStatus(properites, "Edit");
                }

                // Step 1 Create Entity
                createEntity(properites, jobCategory, "'Entity','Vintage'");

                // Step 2 Aporve Entity
                changeWorkFlowStatus(properites, "Approve");

                // step 3 Stage Entity
                changeWorkFlowStatus(properites, "Stage");

                // step 4 Live Entity
                liveEntityAndWaitToComplete(properites);

                sendEntityReportToHUB(properites);

        }

        public void startSyncProcessing(JobProperites properites) throws TalendException {

        }

}
