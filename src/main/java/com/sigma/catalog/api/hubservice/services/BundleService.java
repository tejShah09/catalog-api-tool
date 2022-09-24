package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class BundleService extends AbstractShellProcessService {

        BundleService() {
                jobCategory = JOBKeywords.BUNDLE;
                sheets = "'Entity','Vintage'";
                reportTable = "CAPI_Bundle_AllStatus";
                inpuTableKey = "PRODUCT_BUNDLE";
        }

        public void startASyncProcessing(JobProperites properites) throws TalendException {

                String editInputTable = properites.jobId + "_" + jobCategory + "_Enddate_InputSheet";

                if (!checkIfTableEmpty(properites, editInputTable,
                                JOBKeywords.EDIT_FILE_ROW_COUNT)) {
                        deleteNonLiveEntityName(properites, editInputTable);
                        editEntityName(properites, editInputTable);
                }

                // Step 1 Create Entity
                createEntity(properites);

                // Step 2 Aporve Entity
                approveEntity(properites);

                // step 3 Stage Entity
                stageEntity(properites);

                // step 4 Live Entity

                makeLiveWithStatusCheck(properites, reportTable, properites.jobId + "_" + jobCategory + "_Entity",
                                "PublicID", properites.jobId + "_" + jobCategory + "_Entity", "Name");

                sendReconfile(properites, JOBKeywords.BUNDLE_TYPE, properites.jobId + "_" + jobCategory + "_Entity",
                                "Name");

        }

        public void startSyncProcessing(JobProperites properites) throws TalendException {

        }

}
