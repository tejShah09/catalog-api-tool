package com.sigma.catalog.api.hubservice.services;

import java.nio.file.Paths;
import java.util.HashMap;
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
import com.sigma.catalog.api.hubservice.dbmodel.JOB;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.repository.JOBRepository;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.ConfigurationUtility;

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
    public JOBRepository jobtable;

    @Autowired
    public TalendHelperService talend;

    @Autowired
    private EmailService emailServer;

    public String jobCategory;
    public String sheets;
    public String reportTable;
    public String inpuTableKey;

    public abstract void startSyncProcessing(JobProperites properties) throws TalendException;

    public abstract void startASyncProcessing(JobProperites properties) throws TalendException;

    public String saveFile(String jobId, MultipartFile file) throws TalendException {
        String fileName = "" + jobCategory + "UploadFile_" + jobId + ".csv";
        talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileName);
        jobtable.save(new JOB(jobId, JOBKeywords.START, jobCategory,
                JOBKeywords.TASK_SUCCESS,
                file.getOriginalFilename() + " :: " + fileName));
        return fileName;
    }

    public void createEntity(JobProperites properties) throws TalendException {
        createEntity(properties, jobCategory, sheets);
    }

    public void createEntity(JobProperites properties, String category, String sheetList) throws TalendException {
        jobtable.save(
                new JOB(properties.jobId, JOBKeywords.CATALOG_REQUEST_CREATION, category,
                        JOBKeywords.TASK_STARTED,
                        properties.jobId + "_" + category + "_Entity"));
        HashMap<String, String> config = new HashMap<>();
        config.put("group", category);
        config.put("subSheets", sheetList);
        config.put("catalogStub", "false");
        talend.executeJob(properties.jobId, "eCAPICreateEntities", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + category + "_Entity");
        config.put("jobType", JOBKeywords.CATALOG_REQUEST_SUBMITION);
        config.put("jobCategory", category);
        config.put("keyName", "Name");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Error
        talend.checkForStatusError(properties.jobId, JOBKeywords.CATALOG_REQUEST_SUBMITION, category,
                "" + category + " Upload Failed JobId : " + properties.jobId + " Task : Entity Creation");
    }

    public void createAssoc(JobProperites properties) throws TalendException {
        jobtable.save(
                new JOB(properties.jobId, JOBKeywords.CATALOG_ASSOC_CREATION, jobCategory,
                        JOBKeywords.TASK_STARTED,
                        properties.jobId + "_" + jobCategory + "_Associations"));
        HashMap<String, String> config = new HashMap<>();
        config.put("group", jobCategory);
        config.put("catalogStub", "false");
        talend.executeJob(properties.jobId, "eCAPICreateAssoc", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + jobCategory + "_Associations");
        config.put("jobType", JOBKeywords.CATALOG_ASSOC_SUBMITION);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "Parent_Entity_Name");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Error
        talend.checkForStatusError(properties.jobId, JOBKeywords.CATALOG_ASSOC_SUBMITION, jobCategory,
                "" + jobCategory + " Upload Failed JobId : " + properties.jobId + " Task : Association Creation");
    }

    public void createRates(JobProperites properties) throws TalendException {
        createRates(properties, jobCategory);
    }

    public void createRates(JobProperites properties, String rateCategory) throws TalendException {
        jobtable.save(
                new JOB(properties.jobId, JOBKeywords.CATALOG_RATE_CREATION, jobCategory,
                        JOBKeywords.TASK_STARTED,
                        properties.jobId + "_" + rateCategory + "_Rates"));
        HashMap<String, String> config = new HashMap<>();
        config.put("group", rateCategory);
        config.put("catalogStub", "false");
        talend.executeJob(properties.jobId, "eCAPICreateRates", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + rateCategory + "_Rates");
        config.put("jobType", JOBKeywords.CATALOG_RATE_SUBMITION);
        config.put("jobCategory", rateCategory);
        config.put("keyName", "Parent_Entity_Name");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Error
        talend.checkForStatusError(properties.jobId, JOBKeywords.CATALOG_RATE_SUBMITION, jobCategory,
                "" + jobCategory + " Upload Failed JobId : " + properties.jobId + " Task : Association Creation");
    }

    public void deleteNonLiveEntityName(JobProperites properties) throws TalendException {
        deleteNonLiveEntityName(properties, properties.jobId + "_" + jobCategory + "_InputSheet", reportTable,
                inpuTableKey);
    }

    public void deleteNonLiveEntityName(JobProperites properties, String inputTable) throws TalendException {
        deleteNonLiveEntityName(properties, inputTable, reportTable, inpuTableKey);
    }

    public void deleteNonLiveEntityName(JobProperites properties, String inputTable, String eReportTable,
            String inpuTableKey)
            throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("catalogReportTable", eReportTable);
        config.put("entityNameKey", inpuTableKey);
        config.put("entityTable", inputTable);
        config.put("statusTable", properties.jobId + "_" + jobCategory + "_Entity_Status");
        talend.executeJob(properties.jobId, "eCAPIDeleteNonLiveEntityName", config);

        // Generate report for Delte
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + jobCategory + "_Entity_Status");
        config.put("jobType", JOBKeywords.ENTITY_DELETE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Delte
        talend.checkForStatusError(properties.jobId, JOBKeywords.ENTITY_DELETE, jobCategory,
                "" + jobCategory + " Delete Failed JobId : " + properties.jobId + " Task : Delete Entity", true);
    }

    public void editEntityName(JobProperites properties) throws TalendException {
        editEntityName(properties, properties.jobId + "_" + jobCategory + "_InputSheet", reportTable, inpuTableKey);
    }

    public void editEntityName(JobProperites properties, String inputTable) throws TalendException {
        editEntityName(properties, inputTable, reportTable, inpuTableKey);
    }

    public void editEntityName(JobProperites properties, String inputTable, String eReportTable, String inpuTableKey)
            throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("catalogReportTable", eReportTable);
        config.put("entityNameKey", inpuTableKey);
        config.put("entityTable", inputTable);
        config.put("statusTable", properties.jobId + "_" + jobCategory + "_Entity_Status");
        talend.executeJob(properties.jobId, "eCAPIEditEntityName", config);

        // Generate report for Edit
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + jobCategory + "_Entity_Status");
        config.put("jobType", JOBKeywords.ENTITY_EDIT);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Edit
        talend.checkForStatusError(properties.jobId, JOBKeywords.ENTITY_EDIT, jobCategory,
                "" + jobCategory + " Edit Failed JobId : " + properties.jobId + " Task : Edit Entity");
    }

    public void sendReconfile(JobProperites properties, String entityType) throws TalendException {
        sendReconfile(properties, properties.jobId + "_" + jobCategory + "_InputSheet", reportTable, inpuTableKey,
                entityType);
    }

    public void sendReconfile(JobProperites properties, String inputTable, String entityType) throws TalendException {
        sendReconfile(properties, inputTable, reportTable, inpuTableKey, entityType);
    }

    public void sendReconfile(JobProperites properties, String inputTable, String eReportTable, String inpuTableKey,
            String entityType)
            throws TalendException {
        if (!properties.isSendReconSheet() || !properties.isLaunchEntity()) {
            System.out.println("Recon file is skipped");
            return;
        }
        HashMap<String, String> config = new HashMap<>();
        config.put("catalogReportTable", eReportTable);
        config.put("entityNameKey", inpuTableKey);
        config.put("entityTable", inputTable);
        config.put("entityType", entityType);
        talend.executeJob(properties.jobId, "SendReconSheetToHub", config);

    }

    public void approveEntity(JobProperites properties) throws TalendException {
        approveEntity(properties, "select \"PublicID\" from \"" + properties.jobId + "_" + jobCategory + "_Entity\"");
    }

    public void approveEntity(JobProperites properties, String tableName, String tableKey) throws TalendException {

        approveEntity(properties, "select \"" + tableKey + "\" as \"PublicID\" from \"" + tableName + "\"");

    }

    public void approveEntity(JobProperites properties, String query) throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("query", query);
        config.put("statusTable", properties.jobId + "_" + jobCategory + "_Entity_Status");
        talend.executeJob(properties.jobId, "eCAPIApproveEntity", config);

        // Generate report for Approve
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + jobCategory + "_Entity_Status");
        config.put("jobType", JOBKeywords.ENTITY_APPROVE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Approve
        talend.checkForStatusError(properties.jobId, JOBKeywords.ENTITY_APPROVE, jobCategory,
                "" + jobCategory + " Approve Failed JobId : " + properties.jobId + " Task : Approve Entity");

    }

    public void stageEntity(JobProperites properties) throws TalendException {
        stageEntity(properties, "select \"PublicID\" from \"" + properties.jobId + "_" + jobCategory + "_Entity\"");
    }

    public void stageEntity(JobProperites properties, String tableName, String tableKey) throws TalendException {
        stageEntity(properties, "select \"" + tableKey + "\" as \"PublicID\" from \"" + tableName + "\"");
    }

    public void stageEntity(JobProperites properties, String query) throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("query", query);
        config.put("statusTable", properties.jobId + "_" + jobCategory + "_Entity_Status");
        talend.executeJob(properties.jobId, "eCAPIStageEntity", config);

        // Generate report for Stage
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + jobCategory + "_Entity_Status");
        config.put("jobType", JOBKeywords.ENTITY_STAGE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Stage
        talend.checkForStatusError(properties.jobId, JOBKeywords.ENTITY_STAGE, jobCategory,
                "" + jobCategory + " Stage Failed JobId : " + properties.jobId + " Task : Stage Entity");
    }

    public void liveEntity(JobProperites properties) throws TalendException {
        liveEntity(properties, "select \"PublicID\" from \"" + properties.jobId + "_" + jobCategory + "_Entity\"");
    }

    public void liveEntity(JobProperites properties, String tableName, String tableKey) throws TalendException {
        liveEntity(properties, "select \"" + tableKey + "\" as \"PublicID\" from \"" + tableName + "\"");

    }

    public void liveEntity(JobProperites properties, String query) throws TalendException {

        // Step 10 Change Stragy to Stub (catalgo only)

        HashMap<String, String> config = new HashMap<>();
        if (properties.isChangeStrategy()) {
            config.put("hubIntegration", "false");
            config.put("instanceId", ConfigurationUtility.getEnvConfigModel().getCatalogInstance());
            talend.executeJob(properties.jobId, "ChangeStrategy", config);
        }
        // Live Entity
        if (properties.isLaunchEntity()) {
            config = new HashMap<>();
            config.put("query", query);
            config.put("statusTable", properties.jobId + "_" + jobCategory + "_Entity_Status");
            talend.executeJob(properties.jobId, "eCAPILiveEntity", config);

            // Generate report for Live
            config = new HashMap<>();
            config.put("tableName", properties.jobId + "_" + jobCategory + "_Entity_Status");
            config.put("jobType", JOBKeywords.ENTITY_LIVE);
            config.put("jobCategory", jobCategory);
            config.put("keyName", "PublicID");
            config.put("jobStatus", JOBKeywords.TASK_END);
            talend.executeJob(properties.jobId, "getStatusCount", config);

            // Check And ReportAny Live
            talend.checkForStatusError(properties.jobId, JOBKeywords.ENTITY_LIVE, jobCategory,
                    "" + jobCategory + " Live Failed JobId : " + properties.jobId + " Task : Live Entity");
        }
        // Change Stragy to Stub (catalgo only)
        if (properties.isChangeStrategy()) {
            config = new HashMap<>();
            config.put("hubIntegration", "true");
            config.put("instanceId", ConfigurationUtility.getEnvConfigModel().getCatalogInstance());
            talend.executeJob(properties.jobId, "ChangeStrategy", config);
        }
    }

    public void startGenericSyncProcessing(JobProperites properites, String fileName) throws TalendException {

        // Step 1 SaveFile
        HashMap<String, String> config = new HashMap<>();
        config.put("fileName", TalendConstants.INPUT_FILE_LOCATION + fileName);
        talend.executeJob(properites.jobId, "Read" + jobCategory + "Sheet", config);
        jobtable.save(
                new JOB(properites.jobId, JOBKeywords.UPLOAD_FILE, jobCategory, JOBKeywords.TASK_SUCCESS,
                        properites.jobId + "_" + jobCategory + "_InputSheet"));

        // Step 2 ValidateFile
        talend.executeJob(properites.jobId, "Validate" + jobCategory + "Sheet", null);

        // Step 3 Check And ReportAny Error
        talend.checkForError(properites.jobId, JOBKeywords.FILE_VALIDATION, jobCategory);

        // Step 4 cretae CAPI input
        talend.executeJob(properites.jobId, "CAPIInput" + jobCategory + "Sheet", null);
        jobtable.save(
                new JOB(properites.jobId, JOBKeywords.CAPI_FILE_CREATION, jobCategory, JOBKeywords.TASK_SUCCESS,
                        properites.jobId + "_" + jobCategory + "_Entity"));

    }

    public ResponseEntity<Object> process(JobProperites properites, MultipartFile file) {
        try {
            String fileName = this.saveFile(properites.jobId, file);
            this.startGenericSyncProcessing(properites, fileName);
            this.startSyncProcessing(properites);

        } catch (TalendException e) {
            jobtable.save(new JOB(properites.jobId, JOBKeywords.STOP, jobCategory,
                    JOBKeywords.JOB_FAILED, e.getMessage()));
            emailServer.sendMail(properites, "JOB FAILED jobId : " + properites + " jobName : " + jobCategory + " Validation failed",
                    e.getCustomException(properites.jobId).toString());
            return talend.generateFailResponse(properites.jobId, e);
        }

        return talend.generateSuccessResponse(properites.jobId);
    }

    @Async("asyncExecutorService")
    public void processAsync(JobProperites properties) {
        try {
            this.startASyncProcessing(properties);
            jobtable.save(new JOB(properties.jobId, JOBKeywords.STOP, jobCategory,
                    JOBKeywords.JOB_SUCCESS, "eaam Enjoy"));
        } catch (TalendException e) {
            // Send Email
            jobtable.save(new JOB(properties.jobId, JOBKeywords.STOP, jobCategory,
                    JOBKeywords.JOB_FAILED, e.getRowOuptutException(properties.jobId).toString()));
            emailServer.sendMail(properties, e.getMessage(),
                    e.getRowOuptutException(properties.jobId).toString());
        }
    }
}
