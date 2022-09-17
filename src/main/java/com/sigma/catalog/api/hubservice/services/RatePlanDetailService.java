package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class RatePlanDetailService extends AbstractShellProcessService {

        RatePlanDetailService() {
                jobCategory = JOBKeywords.RATEPLANDETAIL;
                sheets = "'Consumption Units','Inclusive Loss Factors','Use Daylight Saving Time','Consumption Value','Demand Units','Entity'";
                reportTable = "CAPI_RatePlanDetail_AllStatus";
                inpuTableKey = "Description";
        }

        public void startASyncProcessing(JobProperites properites) throws TalendException {

                // Step 1 Create Entity
                createEntity(properites);

                // Step 1.1 Associate Charge
               // createAssoc(jobId);

                // Step 2 Aporve Entity
                approveEntity(properites);

                // step 3 Stage Entity
                stageEntity(properites);

                // step 4 Live Entity
                liveEntity(properites);
                waitLiveTobeCompleted(properites);
                sendReconfile(properites, JOBKeywords.RATEPLAN_TYPE);

        }

        public void startSyncProcessing(JobProperites properites) throws TalendException {

        }

}
