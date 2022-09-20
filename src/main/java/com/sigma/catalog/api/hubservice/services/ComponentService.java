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

                deleteNonLiveEntityName(properites);
                editEntityName(properites);

                // create TimeBand in RatePlanRate
                createEntity(properites, "Bundle", "'Entity','Reference'");

                // Associate Charge
                createAssoc(properites);

                // create Rate
                createRates(properites);

                // Step 2 Aporve Entity
                approveEntity(properites, properites.jobId + "_" + jobCategory + "_Associations", "Parent_Entity_GUID");

                // step 3 Stage Entity
                stageEntity(properites, properites.jobId + "_" + jobCategory + "_Associations", "Parent_Entity_GUID");


                changeStrategy(properites, false);
                liveEntity(properites, properites.jobId + "_" + jobCategory + "_Associations", "Parent_Entity_GUID");
                waitLiveTobeCompleted(properites);
                changeStrategy(properites, true);
               

                sendReconfile(properites, JOBKeywords.BUNDLE_TYPE);

        }

        public void startSyncProcessing(JobProperites properites) throws TalendException {

        }

}
