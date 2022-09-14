package com.sigma.catalog.api.hubservice.services;

import org.apache.poi.ss.formula.functions.Rate;
import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class RatePlanRateService extends AbstractShellProcessService {

        RatePlanRateService() {
                jobCategory = JOBKeywords.RATEPLANRATE;
                reportTable = "CAPI_RatePlanDetail_AllStatus";
                reportTableKey = "Description";
        }

        public void startASyncProcessing(String jobId) throws TalendException {

                deleteNonLiveEntityName(jobId);
                editEntityName(jobId);

                // create TimeBand in RatePlanRate
                createEntity(jobId, "RatePlanDetail", "'Entity','Time Band Mapping'");

                // Associate Charge
                createAssoc(jobId);

                //create Rate
                createRates(jobId);

                // Step 2 Aporve Entity
                approveEntity(jobId, jobId + "_" + jobCategory + "_Rates", "Parent_Entity_GUID");

                // step 3 Stage Entity
                stageEntity(jobId, jobId + "_" + jobCategory + "_Rates", "Parent_Entity_GUID");

                // step 4 Live Entity
                liveEntity(jobId, jobId + "_" + jobCategory + "_Rates", "Parent_Entity_GUID");

        }

        public void startSyncProcessing(String jobId, String fileName) throws TalendException {

        }

}
