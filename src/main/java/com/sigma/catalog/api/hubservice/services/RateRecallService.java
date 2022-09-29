package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class RateRecallService extends AbstractShellProcessService {

        RateRecallService() {
                jobCategory = JOBKeywords.RATERECALL;
                reportTable = "CAPI_RatePlanDetail_AllStatus";
                inpuTableKey = "Parent_Entity_Name";
        }

        public void startASyncProcessing(JobProperites properites) throws TalendException {
                String inpuTable = properites.jobId + "_" + jobCategory + "_Rates";
                deleteNonLiveEntityName(properites, inpuTable);
                editEntityName(properites, inpuTable);

                // create Rate
                createRateRecall(properites);

                // Step 2 Aporve Entity
                approveEntity(properites, inpuTable, "Parent_Entity_GUID");

                // step 3 Stage Entity
                stageEntity(properites, inpuTable, "Parent_Entity_GUID");

                // step 4 Live Entity
                makeLiveWithStatusCheck(properites, inpuTable, "Parent_Entity_GUID", inpuTable, inpuTableKey);

                //
                sendReconfile(properites, inpuTable, JOBKeywords.RATEPLAN_TYPE);

        }

        public void startSyncProcessing(JobProperites properites) throws TalendException {

        }

}
