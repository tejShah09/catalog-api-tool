package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class RatePlanDetailService extends AbstractShellProcessService {

        RatePlanDetailService() {
                jobCategory = JOBKeywords.RATEPLANDETAIL;
                sheets = "'Consumption Units','Inclusive Loss Factors','Use Daylight Saving Time','Consumption Value','Demand Units','Entity'";
        }

        public void startASyncProcessing(String jobId) throws TalendException {

                // Step 1 Create Entity
                createEntity(jobId);

                // Step 1.1 Associate Charge
               // createAssoc(jobId);

                // Step 2 Aporve Entity
                approveEntity(jobId);

                // step 3 Stage Entity
                stageEntity(jobId);

                // step 4 Live Entity
                liveEntity(jobId);

        }

        public void startSyncProcessing(String jobId, String fileName) throws TalendException {

        }

}
