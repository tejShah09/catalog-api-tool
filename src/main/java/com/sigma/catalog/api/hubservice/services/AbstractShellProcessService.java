package com.sigma.catalog.api.hubservice.services;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;

@Configuration
@EnableAsync
class AsynchConfigurationService {
    @Bean(name = "asyncExecutorService")
    public Executor asyncExecutorService() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("asyncExecutorService-");
        executor.initialize();
        return executor;
    }
}

@Service
public abstract class AbstractShellProcessService {

    @Autowired
    public TalendHelperService talend;

    @Autowired
    public JobService jobService;

    public String jobCategory;

    public abstract void startSyncProcessing(JobProperites properties) throws TalendException;

    public abstract void startASyncProcessing(JobProperites properties) throws TalendException;

    public void createEntity(JobProperites properties, String group, String sheets) throws TalendException {
        jobService.createEntity(properties, group, jobCategory, sheets);
    }

    public void createAssoc(JobProperites properties) throws TalendException {
        jobService.createAssoc(properties, jobCategory, jobCategory);
    }

    public void createRates(JobProperites properties) throws TalendException {
        jobService.createRates(properties, jobCategory, jobCategory);
    }

    public void createRateRecall(JobProperites properties) throws TalendException {
        jobService.createRateRecall(properties, jobCategory, jobCategory);
    }

    public void createRates(JobProperites properties, String group) throws TalendException {
        jobService.createRates(properties, group, jobCategory);
    }

    public void sendEntityReportToHUB(JobProperites properties) throws TalendException {
        jobService.sendEntityReportToHUB(properties, jobCategory, properties.jobId + "_" + jobCategory + "_Entity",
                properties.jobId + "_" + jobCategory + "_Report");

    }

    public void createReport(JobProperites properties) throws TalendException {
        jobService.createEntityReport(properties, properties.jobId + "_" + jobCategory + "_Entity",
                properties.jobId + "_" + jobCategory + "_Report");
    }

    public void changeWorkFlowStatus(JobProperites properties, String targeState) throws TalendException {
        jobService.changeWorkFlowWith_103Retry(properties, properties.jobId + "_" + jobCategory + "_Entity",
                properties.jobId + "_" + jobCategory + "_Report",
                properties.jobId + "_" + jobCategory + "_Entity_Status", targeState, jobCategory);

    }

    public void liveEntityAndWaitToComplete(JobProperites properites)
            throws TalendException {
        jobService.liveEntityAndWaitToComplete(properites, properites.jobId, jobCategory);
    }

    public void changeStrategy(JobProperites properites, boolean isHubIntegration) throws TalendException {
        jobService.changeStrategy(properites, isHubIntegration);
    }

    public void startGenericSyncProcessing(JobProperites properties, String fileName) throws TalendException {

        // Step 1 SaveFile
        jobService.readFileJob(properties, fileName, jobCategory);

        this.validateInputSheetRowCount(properties);
        // Step 2 ValidateFile
        jobService.validateFileJob(properties, jobCategory);

        // Step 3 CrateCAPIfile
        jobService.createCAPIFileJob(properties, jobCategory);

    }

    public void validateInputSheetRowCount(JobProperites properties) throws TalendException {
        jobService.validateForEmptyInput(properties, properties.jobId + "_" + jobCategory + "_InputSheet", jobCategory,
                JOBKeywords.FILE_ROW_COUNT);
    }

    public void validateInputSheetRowCount(JobProperites properties, String inputTable) throws TalendException {
        jobService.validateForEmptyInput(properties, inputTable, jobCategory, JOBKeywords.FILE_ROW_COUNT);
    }

    public void validateInputSheetRowCount(JobProperites properties, String inputTable, String jobType)
            throws TalendException {
        jobService.validateForEmptyInput(properties, inputTable, jobCategory, jobType);
    }

    public boolean checkIfTableEmpty(JobProperites properties, String inputTable, String jobType) {
        boolean isEmpty = true;

        try {
            validateInputSheetRowCount(properties, inputTable, jobType);
            isEmpty = false;
        } catch (TalendException e) {
            isEmpty = true;
        }

        return isEmpty;

    }

    public ResponseEntity<Object> process(JobProperites properites, MultipartFile file) {
        try {
            jobService.jobValidation(properites.jobId, jobCategory);
            jobService.startJOB(properites.jobId, jobCategory);
            
            // SaveFile
            String fileName = jobService.saveCSVFile(properites.jobId, jobCategory, file);
            properites.addInputFileNAme(fileName);
            // Generic Process
            this.startGenericSyncProcessing(properites, fileName);

            // Overrided sync process
            this.startSyncProcessing(properites);

        } catch (TalendException e) {

            jobService.saveErrorAndSendErrorEmail(properites, jobCategory,
                    e.getCustomException(properites.jobId).toString());
            return talend.generateFailResponse(properites.jobId, e);
        }

        return talend.generateSuccessResponse(properites.jobId);
    }

    public ResponseEntity<Object> processXML(JobProperites properties, List<String> ratefileNames) {
        try {

            // SaveFile

            if (ratefileNames != null && ratefileNames.size() > 0) {

                for (int i = 0; i < ratefileNames.size(); i++) {
                    HashMap<String, String> config = new HashMap<>();
                    config.put("fileName", ratefileNames.get(i));
                    talend.executeJob(properties.jobId, "Read" + jobCategory + "Sheet", config);

                }

            }
            // Overrided sync process
            this.startSyncProcessing(properties);

            jobService.validateFileJob(properties, jobCategory);
            jobService.createCAPIFileJob(properties, jobCategory);

        } catch (TalendException e) {

            jobService.saveErrorAndSendErrorEmail(properties, jobCategory,
                    e.getCustomException(properties.jobId).toString());
            return talend.generateFailResponse(properties.jobId, e);
        }

        return talend.generateSuccessResponse(properties.jobId);
    }

    @Async("asyncExecutorService")
    public void processAsync(JobProperites properties) {
        if (properties.isOnlySync()) {
            System.out.println("Async process is stubed");
            return;
        }
        try {
            this.startASyncProcessing(properties);
            jobService.saveSucessJobs(properties, jobCategory);
        } catch (TalendException e) {
            jobService.saveErrorAndSendErrorEmail(properties, jobCategory,
                    e.getRowOuptutException(properties.jobId).toString());

        }
    }

}
