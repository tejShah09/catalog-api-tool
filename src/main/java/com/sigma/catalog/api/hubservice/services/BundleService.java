package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class BundleService extends AbstractShellProcessService {

        BundleService() {
                jobCategory = JOBKeywords.BUNDLE;
                sheets = "'Entity','Vintage'";
        }

        public void startASyncProcessing(String jobId) throws TalendException {

                // Step 1 Create Entity
                createEntity(jobId);

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
