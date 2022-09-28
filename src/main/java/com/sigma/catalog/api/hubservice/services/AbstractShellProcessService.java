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
    public String sheets;
    public String reportTable;
    public String inpuTableKey;

    public abstract void startSyncProcessing(JobProperites properties) throws TalendException;

    public abstract void startASyncProcessing(JobProperites properties) throws TalendException;

    public void createEntity(JobProperites properties) throws TalendException {
        jobService.createEntity(properties, jobCategory, jobCategory, sheets);
    }

    public void createEntity(JobProperites properties, String group, String sheets) throws TalendException {
        jobService.createEntity(properties, group, jobCategory, sheets);
    }

    public void createAssoc(JobProperites properties) throws TalendException {
        jobService.createAssoc(properties, jobCategory, jobCategory);
    }

    public void createRates(JobProperites properties) throws TalendException {
        jobService.createRates(properties, jobCategory, jobCategory);
    }

    public void createRates(JobProperites properties, String group) throws TalendException {
        jobService.createRates(properties, group, jobCategory);
    }

    public void deleteNonLiveEntityName(JobProperites properties) throws TalendException {
        jobService.deleteNonLiveEntityName(properties, properties.jobId + "_" + jobCategory + "_InputSheet",
                inpuTableKey, reportTable, jobCategory,
                properties.jobId + "_" + jobCategory + "_Entity_Status");
    }

    public void deleteNonLiveEntityName(JobProperites properties, String inputTable) throws TalendException {
        jobService.deleteNonLiveEntityName(properties, inputTable, inpuTableKey, reportTable, jobCategory,
                properties.jobId + "_" + jobCategory + "_Entity_Status");
    }

    public void editEntityName(JobProperites properties) throws TalendException {
        jobService.editEntityName(properties, properties.jobId + "_" + jobCategory + "_InputSheet", inpuTableKey,
                reportTable, jobCategory,
                properties.jobId + "_" + jobCategory + "_Entity_Status");
    }

    public void editEntityName(JobProperites properties, String inputTable) throws TalendException {
        jobService.editEntityName(properties, inputTable, inpuTableKey, reportTable, jobCategory,
                properties.jobId + "_" + jobCategory + "_Entity_Status");
    }

    public void sendReconfile(JobProperites properties, String entityType) throws TalendException {
        jobService.sendReconfile(properties, properties.jobId + "_" + jobCategory + "_InputSheet", inpuTableKey,
                reportTable,
                entityType, jobCategory);
    }

    public void sendReconfile(JobProperites properties, String entityType, String inputTableNameR,
            String inputtableKeyR) throws TalendException {
        jobService.sendReconfile(properties, inputTableNameR, inputtableKeyR,
                reportTable,
                entityType, jobCategory);
    }

    public void sendReconfile(JobProperites properties, String inputTable, String entityType) throws TalendException {
        jobService.sendReconfile(properties, inputTable, inpuTableKey, reportTable, entityType, jobCategory);
    }

    public void approveEntity(JobProperites properties) throws TalendException {
        approveEntity(properties, "select \"PublicID\" from \"" + properties.jobId + "_" + jobCategory + "_Entity\"");
    }

    public void approveEntity(JobProperites properties, String tableName, String tableKey) throws TalendException {

        approveEntity(properties, "select \"" + tableKey + "\" as \"PublicID\" from \"" + tableName + "\"");

    }

    public void approveEntity(JobProperites properties, String query) throws TalendException {
        jobService.approveEntity(properties, query, properties.jobId + "_" + jobCategory + "_Entity_Status",
                jobCategory);

    }

    public void stageEntity(JobProperites properties) throws TalendException {
        stageEntity(properties, "select \"PublicID\" from \"" + properties.jobId + "_" + jobCategory + "_Entity\"");
    }

    public void stageEntity(JobProperites properties, String tableName, String tableKey) throws TalendException {
        stageEntity(properties, "select \"" + tableKey + "\" as \"PublicID\" from \"" + tableName + "\"");
    }

    public void stageEntity(JobProperites properties, String query) throws TalendException {
        jobService.stageEntity(properties, query, properties.jobId + "_" + jobCategory + "_Entity_Status", jobCategory);
    }

    public void liveEntity(JobProperites properties) throws TalendException {
        liveEntity(properties, "select \"PublicID\" from \"" + properties.jobId + "_" + jobCategory + "_Entity\"");
    }

    public void liveEntity(JobProperites properties, String tableName, String tableKey) throws TalendException {
        liveEntity(properties, "select \"" + tableKey + "\" as \"PublicID\" from \"" + tableName + "\"");

    }

    public void liveEntity(JobProperites properties, String query) throws TalendException {
        jobService.liveEntity(properties, query, properties.jobId + "_" + jobCategory + "_Entity_Status", jobCategory);
    }

    public void waitLiveTobeCompleted(JobProperites properites) throws TalendException {
        waitLiveTobeCompleted(properites, reportTable, properites.jobId + "_" + jobCategory + "_InputSheet",
                inpuTableKey);
    }

    public void waitLiveTobeCompleted(JobProperites properites, String inputLiveTable,
            String inputLiveKey) throws TalendException {
        jobService.waitLiveTobeCompleted(properites, reportTable, inputLiveTable, inputLiveKey, jobCategory);
    }

    public void changeStrategy(JobProperites properites, boolean isHubIntegration) throws TalendException {
        jobService.changeStrategy(properites, isHubIntegration);
    }

    public void waitLiveTobeCompleted(JobProperites properites, String reportLiveTable, String inputLiveTable,
            String inputLiveKey) throws TalendException {
        jobService.waitLiveTobeCompleted(properites, reportLiveTable, inputLiveTable, inputLiveKey, jobCategory);
    }

    public void makeLiveWithStatusCheck(JobProperites properites)
            throws TalendException {
        makeLiveWithStatusCheck(properites, reportTable, properites.jobId + "_" + jobCategory + "_Entity", "PublicID",
                properites.jobId + "_" + jobCategory + "_InputSheet", inpuTableKey);
    }

    public void makeLiveWithStatusCheck(JobProperites properites, String liveInputTable,
            String liveInpuTablekey)
            throws TalendException {
        makeLiveWithStatusCheck(properites, reportTable, liveInputTable, liveInpuTablekey,
                properites.jobId + "_" + jobCategory + "_InputSheet", inpuTableKey);
    }

    public void makeLiveWithStatusCheck(JobProperites properites, String liveInputTable,
            String liveInpuTablekey, String coutInputTable,
            String countInputTableKey)
            throws TalendException {
        makeLiveWithStatusCheck(properites, reportTable, liveInputTable, liveInpuTablekey,
                coutInputTable, countInputTableKey);
    }

    public void makeLiveWithStatusCheck(JobProperites properites, String reportTable, String liveInputTable,
            String liveInpuTablekey, String coutInputTable, String countInputTableKey)
            throws TalendException {

        try {
            changeStrategy(properites, false);
            liveEntity(properites, liveInputTable, liveInpuTablekey);
            waitLiveTobeCompleted(properites, reportTable, coutInputTable, countInputTableKey);
        } catch (TalendException e) {
            throw e;
        } finally {
            changeStrategy(properites, true);
        }

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
            jobService.jobValidation(properites.jobId,jobCategory);
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

    public ResponseEntity<Object> processXML(JobProperites properties, MultipartFile[] rateXmls) {
        try {

            // SaveFile
            List<String> ratefileNames = new ArrayList<>();

            if (rateXmls != null) {

                Arrays.asList(rateXmls).stream().forEach((file) -> {

                    String oringalFile = file.getOriginalFilename();
                    if (StringUtility.contains(oringalFile, ".xml")) {
                        oringalFile = oringalFile.replace(".xml", "");
                    }
                    String fileNAme = jobCategory + "_" + oringalFile + "_"
                            + properties.jobId
                            + ".xml";
                    properties.addInputFileNAme(fileNAme);
                    talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme);
                    ratefileNames.add(TalendConstants.INPUT_FILE_LOCATION + fileNAme);

                });

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
        try {
            this.startASyncProcessing(properties);
            jobService.saveSucessJobs(properties, jobCategory);
        } catch (TalendException e) {
            jobService.saveErrorAndSendErrorEmail(properties, jobCategory,
                    e.getRowOuptutException(properties.jobId).toString());

        }
    }

}
