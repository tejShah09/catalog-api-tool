package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class ProductXMLRatesService extends AbstractShellProcessService {

    ProductXMLRatesService() {
        jobCategory = JOBKeywords.PRODUCTXMLRATES;

    }

    @Override
    public void startASyncProcessing(JobProperites properties) throws TalendException {

        String inpuTable = properties.jobId + "_" + JOBKeywords.COMPONENT + "_Rates";
        if (!checkIfTableEmpty(properties, inpuTable,
                JOBKeywords.TABLE_FILE_ROW_COUNT)) {
            changeWorkFlowStatus(properties, "DeleteNonLive");

            changeWorkFlowStatus(properties, "Edit");

            createRates(properties, JOBKeywords.COMPONENT);

        }
    }

    @Override
    public void startSyncProcessing(JobProperites properties) throws TalendException {
        validateInputSheetRowCount(properties, properties.jobId + "_HUBExtract_ProductRates");
    }

}
