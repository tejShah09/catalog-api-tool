package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class ComponentService extends AbstractShellProcessService {

        ComponentService() {
                jobCategory = JOBKeywords.COMPONENT;
                reportTable = "CAPI_Bundle_AllStatus";
                inpuTableKey = "PRODUCT_BUNDLE";
        }

        public void startASyncProcessing(JobProperites properites) throws TalendException {
                String processingTable = properites.jobId + "_" + jobCategory + "_Associations";
                deleteNonLiveEntityName(properites);
                editEntityName(properites);

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
                approveEntity(properites, processingTable, "Parent_Entity_GUID");

                // step 3 Stage Entity
                stageEntity(properites, processingTable, "Parent_Entity_GUID");

                makeLiveWithStatusCheck(properites, processingTable, "Parent_Entity_GUID");

                sendReconfile(properites, JOBKeywords.BUNDLE_TYPE);

        }

        public void startSyncProcessing(JobProperites properites) throws TalendException {

        }

}
