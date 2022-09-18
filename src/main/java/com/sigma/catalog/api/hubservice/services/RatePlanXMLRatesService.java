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
        // TODO Auto-generated method stub

    }

    @Override
    public void startSyncProcessing(JobProperites properties) throws TalendException {

    }

}
