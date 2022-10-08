package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class RatePlanXMLRatesService extends AbstractShellProcessService {

    RatePlanXMLRatesService() {
        jobCategory = JOBKeywords.RATEPLANXMLRATES;
    }

    @Override
    public void startASyncProcessing(JobProperites properties) throws TalendException {
        String inpuTable = properties.jobId + "_" + JOBKeywords.HUBRATEPLANRATE + "_Rates";
        if (!checkIfTableEmpty(properties, inpuTable,
                JOBKeywords.TABLE_FILE_ROW_COUNT)) {
            changeWorkFlowStatus(properties, "DeleteNonLive");

            changeWorkFlowStatus(properties, "Edit");

            createRates(properties, JOBKeywords.HUBRATEPLANRATE);
        }
    }

    @Override
    public void startSyncProcessing(JobProperites properties) throws TalendException {
        validateInputSheetRowCount(properties, properties.jobId + "_HUBExtract_Rates");
    }

}
