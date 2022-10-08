package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class RatePlanRateService extends AbstractShellProcessService {

        RatePlanRateService() {
                jobCategory = JOBKeywords.RATEPLANRATE;
        }

        public void startASyncProcessing(JobProperites properites) throws TalendException {
                changeWorkFlowStatus(properites, "DeleteNonLive");
                changeWorkFlowStatus(properites, "Edit");

                // create TimeBand in RatePlanRate
                if (!checkIfTableEmpty(properites, properites.jobId + "_" + "RatePlanDetail_Entity",
                                JOBKeywords.TABLE_FILE_ROW_COUNT)) {
                        createEntity(properites, "RatePlanDetail", "'Entity','Time Band Mapping'");
                }

                // Associate Charge
                createAssoc(properites);

                // create Rate
                createRates(properites);

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
