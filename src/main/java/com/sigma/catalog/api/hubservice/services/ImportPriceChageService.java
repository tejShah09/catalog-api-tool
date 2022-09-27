package com.sigma.catalog.api.hubservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendHelperService;

@Service
public class ImportPriceChageService {
    @Autowired
    ProductXMLRatesService productXmlService;

    @Autowired
    RatePlanXMLRatesService ratePlanXmlService;

    @Autowired
    public TalendHelperService talend;

    @Autowired
    public JobService jobService;

    @Async("asyncExecutorService")
    public void processAsyncXML(JobProperites properties, List<AbstractShellProcessService> xmlServices) {

        try {
            for (AbstractShellProcessService xmlService : xmlServices) {
                xmlService.startASyncProcessing(properties);
            }

            jobService.saveSucessJobs(properties, JOBKeywords.IMPORTPRICECHANGE);
        } catch (TalendException e) {
            jobService.saveErrorAndSendErrorEmail(properties, JOBKeywords.IMPORTPRICECHANGE,
                    e.getRowOuptutException(properties.jobId).toString());

        }
    }

    public ResponseEntity<Object> validateJobId(JobProperites properties) {
        try {
            jobService.jobValidation(properties.jobId);
        } catch (TalendException e) {
            jobService.saveErrorAndSendErrorEmail(properties, JOBKeywords.IMPORTPRICECHANGE,
                    e.getCustomException(properties.jobId).toString());
            return talend.generateFailResponse(properties.jobId, e);
        }
        return null;

    }

}
