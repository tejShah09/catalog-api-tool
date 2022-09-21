package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class ProductXMLRatesService extends AbstractShellProcessService {

    ProductXMLRatesService() {
        jobCategory = JOBKeywords.PRODUCTXMLRATES;
        reportTable = "CAPI_Bundle_AllStatus";
        inpuTableKey = "Parent_Entity_Name";

    }

    @Override
    public void startASyncProcessing(JobProperites properties) throws TalendException {

        String inpuTable = properties.jobId + "_" + JOBKeywords.COMPONENT + "_Rates";
        deleteNonLiveEntityName(properties, inpuTable);

        editEntityName(properties, inpuTable);

        createRates(properties, JOBKeywords.COMPONENT);

        approveEntity(properties, inpuTable, "Parent_Entity_GUID");

        // step 3 Stage Entity
        stageEntity(properties, inpuTable, "Parent_Entity_GUID");

        // step 4 Live Entity
        changeStrategy(properties, false);
        liveEntity(properties, inpuTable, "Parent_Entity_GUID");
        waitLiveTobeCompleted(properties, inpuTable, "Parent_Entity_GUID");
        changeStrategy(properties, true);

        sendReconfile(properties, inpuTable, JOBKeywords.BUNDLE_TYPE);

    }

    @Override
    public void startSyncProcessing(JobProperites properties) throws TalendException {
        validateInputSheetRowCount(properties, properties.jobId + "_HUBExtract_ProductRates");
    }

}
