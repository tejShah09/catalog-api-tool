package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class RatePlanXMLRatesService extends AbstractShellProcessService {

    RatePlanXMLRatesService() {
        jobCategory = JOBKeywords.RATEPLANXMLRATES;
        reportTable = "CAPI_RatePlanDetail_AllStatus";
        inpuTableKey = "Parent_Entity_Name";
    }

    @Override
    public void startASyncProcessing(JobProperites properties) throws TalendException {
        String inpuTable = properties.jobId + "_" + JOBKeywords.HUBRATEPLANRATE + "_Rates";
        if (!checkIfTableEmpty(properties, inpuTable,
                JOBKeywords.TABLE_FILE_ROW_COUNT)) {
            deleteNonLiveEntityName(properties, inpuTable);

            editEntityName(properties, inpuTable);

            createRates(properties, JOBKeywords.HUBRATEPLANRATE);

            approveEntity(properties, inpuTable, "Parent_Entity_GUID");

            // step 3 Stage Entity
            stageEntity(properties, inpuTable, "Parent_Entity_GUID");

            // step 4 Live Entity
            makeLiveWithStatusCheck(properties, inpuTable, "Parent_Entity_GUID", inpuTable, "Parent_Entity_Name");

            sendReconfile(properties, inpuTable, JOBKeywords.RATEPLAN_TYPE);
        }
    }

    @Override
    public void startSyncProcessing(JobProperites properties) throws TalendException {
        validateInputSheetRowCount(properties, properties.jobId + "_HUBExtract_Rates");
    }

}
