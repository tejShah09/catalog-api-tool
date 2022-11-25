package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class OfferService extends AbstractShellProcessService {
    OfferService() {
        jobCategory = JOBKeywords.OFFER;
    }

    public void startASyncProcessing(JobProperites properites) throws TalendException {

       

        // Step 1 create assoc
        createEntity(properites,JOBKeywords.OFFER,"'Market_Status','Marketing','is_a_Default_Renewal_Offer','Rewards','Electricity_Other_Fees','Gas_Other_Fees','Electricity_Offer_Eligibility','Gas_Offer_Eligibility'");
        // Step 2 Aporve Entity
        changeWorkFlowStatus(properites, "Approve");

        // step 3 Stage Entity
        changeWorkFlowStatus(properites, "Stage");

        // step 4 Live Entity
        liveEntityAndWaitToComplete(properites);

        sendEntityReportToHUB(properites);

    }

    public void startSyncProcessing(JobProperites properites) throws TalendException {

    }

}
