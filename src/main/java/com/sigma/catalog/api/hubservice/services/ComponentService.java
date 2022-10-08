package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class ComponentService extends AbstractShellProcessService {

        ComponentService() {
                jobCategory = JOBKeywords.COMPONENT;
        }

        public void startASyncProcessing(JobProperites properites) throws TalendException {

                changeWorkFlowStatus(properites, "DeleteNonLive");
                changeWorkFlowStatus(properites, "Edit");

                // create TimeBand in RatePlanRate
                // create TimeBand in RatePlanRate
                if (!checkIfTableEmpty(properites, properites.jobId + "_" + "Bundle_Entity",
                                JOBKeywords.TABLE_FILE_ROW_COUNT)) {
                        createEntity(properites, "Bundle", "'Entity','Reference'");
                }

                // Associate Charge
                createAssoc(properites);

                // create Rate
                if (!checkIfTableEmpty(properites, properites.jobId + "_" + "ProductComponent_Rates",
                                JOBKeywords.RATE_FILE_ROW_COUNT)) {
                        createRates(properites);
                }

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
