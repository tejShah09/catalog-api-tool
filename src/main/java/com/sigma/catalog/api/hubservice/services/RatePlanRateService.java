package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class RatePlanRateService extends AbstractShellProcessService {

        RatePlanRateService() {
                jobCategory = JOBKeywords.RATEPLANRATE;
                reportTable = "CAPI_RatePlanDetail_AllStatus";
                inpuTableKey = "Description";
        }

        public void startASyncProcessing(JobProperites properites) throws TalendException {
                String inpuTable = properites.jobId + "_" + jobCategory + "_Rates";
                deleteNonLiveEntityName(properites);
                editEntityName(properites);

                // create TimeBand in RatePlanRate
                createEntity(properites, "RatePlanDetail", "'Entity','Time Band Mapping'");

                // Associate Charge
                createAssoc(properites);

                // create Rate
                createRates(properites);

                // Step 2 Aporve Entity
                approveEntity(properites, inpuTable, "Parent_Entity_GUID");

                // step 3 Stage Entity
                stageEntity(properites, inpuTable, "Parent_Entity_GUID");

                // step 4 Live Entity
                makeLiveWithStatusCheck(properites, inpuTable, "Parent_Entity_GUID");

                //
                sendReconfile(properites, JOBKeywords.RATEPLAN_TYPE);

        }

        public void startSyncProcessing(JobProperites properites) throws TalendException {

        }

}
