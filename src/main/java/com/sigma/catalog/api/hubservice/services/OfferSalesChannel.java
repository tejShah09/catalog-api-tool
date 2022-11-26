package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class OfferSalesChannel extends AbstractShellProcessService {
    OfferSalesChannel() {
        jobCategory = JOBKeywords.OFFERSALESCHANNEL;
    }

    public void startASyncProcessing(JobProperites properites) throws TalendException {

       
        changeWorkFlowStatus(properites, "DeleteNonLive");
        changeWorkFlowStatus(properites, "Edit");
        // Step 1 create assoc
        createEntity(properites,JOBKeywords.OFFERSALESCHANNEL,"");
        // Step 2 Aporve Entity
        changeWorkFlowStatus(properites, "Approve");

        // step 3 Stage Entity
        changeWorkFlowStatus(properites, "Stage");

        // step 4 Live Entity
        liveEntityAndWaitToComplete(properites);

        //sendEntityReportToHUB(properites);

    }

    public void startSyncProcessing(JobProperites properites) throws TalendException {

    }

}