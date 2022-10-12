package com.sigma.catalog.api.hubservice.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;

@Service
public class ImportPriceChageService extends AbstractShellProcessService {

    ImportPriceChageService() {
        jobCategory = JOBKeywords.IMPORTPRICECHANGE;
    }

    @Autowired
    public TalendHelperService talend;

    @Autowired
    ProductXMLRatesService productXmlService;

    @Autowired
    RatePlanXMLRatesService ratePlanXmlService;

    @Async("asyncExecutorService")
    public void processAsyncXML(JobProperites properties) {
        if (properties.isOnlySync()) {
            System.out.println("Async process is stubed");
            return;
        }

        try {

            productXmlService.startASyncProcessing(properties);
            ratePlanXmlService.startASyncProcessing(properties);
            if (!checkIfTableEmpty(properties, properties.jobId + "_" + jobCategory + "_Entity",
                    JOBKeywords.TABLE_FILE_ROW_COUNT)) {
                createReport(properties);
                changeWorkFlowStatus(properties, "Approve");

                // step 3 Stage Entity
                changeWorkFlowStatus(properties, "Stage");

                liveEntityAndWaitToComplete(properties);

                sendEntityReportToHUB(properties);
            }
            jobService.saveSucessJobs(properties, JOBKeywords.IMPORTPRICECHANGE);
        } catch (TalendException e) {
            jobService.saveErrorAndSendErrorEmail(properties, JOBKeywords.IMPORTPRICECHANGE,
                    e.getRowOuptutException(properties.jobId).toString());

        }
    }

    public ResponseEntity<Object> procesSyncXML(JobProperites properties, MultipartFile[] rateXmls,
            MultipartFile[] bundlXmls) {
        ResponseEntity<Object> resp = validateJobId(properties);
        jobService.startJOB(properties.jobId, jobCategory);

        if (resp != null) {
            return resp;
        }
        if (bundlXmls != null) {
            List<String> ratefileNames = new ArrayList<>();
            Arrays.asList(bundlXmls).stream().forEach((file) -> {

                String fileName = jobService.saveXMLFile(properties.jobId, jobCategory, file);
                properties.addInputFileNAme(fileName);

                ratefileNames.add(TalendConstants.INPUT_FILE_LOCATION + fileName);

            });

            resp = productXmlService.processXML(properties, ratefileNames);
            if (resp.getStatusCode() != HttpStatus.OK) {
                return resp;
            }
        }
        if (rateXmls != null) {
            List<String> ratefileNames = new ArrayList<>();
            Arrays.asList(rateXmls).stream().forEach((file) -> {

                String fileName = jobService.saveXMLFile(properties.jobId, jobCategory, file);
                properties.addInputFileNAme(fileName);

                ratefileNames.add(TalendConstants.INPUT_FILE_LOCATION + fileName);

            });

            resp = ratePlanXmlService.processXML(properties, ratefileNames);
            if (resp.getStatusCode() != HttpStatus.OK) {

                return resp;
            }

        }
        return talend.generateSuccessResponse(properties.jobId);
    }

    public ResponseEntity<Object> validateJobId(JobProperites properties) {
        try {
            jobService.jobValidation(properties.jobId, JOBKeywords.IMPORTPRICECHANGE);
        } catch (TalendException e) {
            jobService.saveErrorAndSendErrorEmail(properties, JOBKeywords.IMPORTPRICECHANGE,
                    e.getCustomException(properties.jobId).toString());
            return talend.generateFailResponse(properties.jobId, e);
        }
        return null;

    }

    @Override
    public void startSyncProcessing(JobProperites properties) throws TalendException {

    }

    @Override
    public void startASyncProcessing(JobProperites properties) throws TalendException {

    }

}
