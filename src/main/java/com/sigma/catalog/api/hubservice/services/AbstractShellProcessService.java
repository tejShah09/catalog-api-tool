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
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.repository.JOBRepository;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;

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
    public String reportTableKey;

    public abstract void startSyncProcessing(String jobId, String fileName) throws TalendException;

    public abstract void startASyncProcessing(String jobId) throws TalendException;

    public String saveFile(String jobId, MultipartFile file) throws TalendException {
        String fileName = "" + jobCategory + "UploadFile_" + jobId + ".csv";
        talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileName);
        jobtable.save(new JOB(jobId, JOBKeywords.START, jobCategory,
                JOBKeywords.TASK_SUCCESS,
                file.getOriginalFilename() + " :: " + fileName));
        return fileName;
    }

    public void createEntity(String jobId) throws TalendException {
        createEntity(jobId, jobCategory, sheets);
    }

    public void createEntity(String jobId, String category, String sheetList) throws TalendException {
        jobtable.save(
                new JOB(jobId, JOBKeywords.CATALOG_REQUEST_CREATION, category,
                        JOBKeywords.TASK_STARTED,
                        jobId + "_" + category + "_Entity"));
        HashMap<String, String> config = new HashMap<>();
        config.put("group", category);
        config.put("subSheets", sheetList);
        config.put("catalogStub", "false");
        talend.executeJob(jobId, "eCAPICreateEntities", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", jobId + "_" + category + "_Entity");
        config.put("jobType", JOBKeywords.CATALOG_REQUEST_SUBMITION);
        config.put("jobCategory", category);
        config.put("keyName", "Name");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(jobId, "getStatusCount", config);

        // Check And ReportAny Error
        talend.checkForStatusError(jobId, JOBKeywords.CATALOG_REQUEST_SUBMITION, category,
                "" + category + " Upload Failed JobId : " + jobId + " Task : Entity Creation");
    }

    public void createAssoc(String jobId) throws TalendException {
        jobtable.save(
                new JOB(jobId, JOBKeywords.CATALOG_ASSOC_CREATION, jobCategory,
                        JOBKeywords.TASK_STARTED,
                        jobId + "_" + jobCategory + "_Associations"));
        HashMap<String, String> config = new HashMap<>();
        config.put("group", jobCategory);
        config.put("catalogStub", "false");
        talend.executeJob(jobId, "eCAPICreateAssoc", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", jobId + "_" + jobCategory + "_Associations");
        config.put("jobType", JOBKeywords.CATALOG_ASSOC_SUBMITION);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "Parent_Entity_Name");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(jobId, "getStatusCount", config);

        // Check And ReportAny Error
        talend.checkForStatusError(jobId, JOBKeywords.CATALOG_ASSOC_SUBMITION, jobCategory,
                "" + jobCategory + " Upload Failed JobId : " + jobId + " Task : Association Creation");
    }

    public void createRates(String jobId) throws TalendException {
        jobtable.save(
                new JOB(jobId, JOBKeywords.CATALOG_RATE_CREATION, jobCategory,
                        JOBKeywords.TASK_STARTED,
                        jobId + "_" + jobCategory + "_Rates"));
        HashMap<String, String> config = new HashMap<>();
        config.put("group", jobCategory);
        config.put("catalogStub", "false");
        talend.executeJob(jobId, "eCAPICreateRates", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", jobId + "_" + jobCategory + "_Rates");
        config.put("jobType", JOBKeywords.CATALOG_RATE_SUBMITION);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "Parent_Entity_Name");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(jobId, "getStatusCount", config);

        // Check And ReportAny Error
        talend.checkForStatusError(jobId, JOBKeywords.CATALOG_RATE_SUBMITION, jobCategory,
                "" + jobCategory + " Upload Failed JobId : " + jobId + " Task : Association Creation");
    }

    public void deleteNonLiveEntityName(String jobId) throws TalendException {
        deleteNonLiveEntityName(jobId, jobId + "_" + jobCategory + "_InputSheet", reportTable, reportTableKey);
    }

    public void deleteNonLiveEntityName(String jobId, String inputTable) throws TalendException {
        deleteNonLiveEntityName(jobId, inputTable, reportTable, reportTableKey);
    }

    public void deleteNonLiveEntityName(String jobId, String inputTable, String eReportTable, String eReportTableKey)
            throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("catalogReportTable", eReportTable);
        config.put("entityNameKey", eReportTableKey);
        config.put("entityTable", inputTable);
        config.put("statusTable", jobId + "_" + jobCategory + "_Entity_Status");
        talend.executeJob(jobId, "eCAPIDeleteNonLiveEntityName", config);

        // Generate report for Delte
        config = new HashMap<>();
        config.put("tableName", jobId + "_" + jobCategory + "_Entity_Status");
        config.put("jobType", JOBKeywords.ENTITY_DELETE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(jobId, "getStatusCount", config);

        // Check And ReportAny Delte
        talend.checkForStatusError(jobId, JOBKeywords.ENTITY_DELETE, jobCategory,
                "" + jobCategory + " Delete Failed JobId : " + jobId + " Task : Delete Entity", true);
    }

    public void editEntityName(String jobId) throws TalendException {
        editEntityName(jobId, jobId + "_" + jobCategory + "_InputSheet", reportTable, reportTableKey);
    }

    public void editEntityName(String jobId, String inputTable) throws TalendException {
        editEntityName(jobId, inputTable, reportTable, reportTableKey);
    }

    public void editEntityName(String jobId, String inputTable, String eReportTable, String eReportTableKey)
            throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("catalogReportTable", eReportTable);
        config.put("entityNameKey", eReportTableKey);
        config.put("entityTable", inputTable);
        config.put("statusTable", jobId + "_" + jobCategory + "_Entity_Status");
        talend.executeJob(jobId, "eCAPIEditEntityName", config);

        // Generate report for Edit
        config = new HashMap<>();
        config.put("tableName", jobId + "_" + jobCategory + "_Entity_Status");
        config.put("jobType", JOBKeywords.ENTITY_EDIT);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(jobId, "getStatusCount", config);

        // Check And ReportAny Edit
        talend.checkForStatusError(jobId, JOBKeywords.ENTITY_EDIT, jobCategory,
                "" + jobCategory + " Edit Failed JobId : " + jobId + " Task : Edit Entity");
    }

    public void approveEntity(String jobId) throws TalendException {
        approveEntity(jobId, "select \"PublicID\" from \"" + jobId + "_" + jobCategory + "_Entity\"");
    }

    public void approveEntity(String jobId, String tableName, String tableKey) throws TalendException {
       
        approveEntity(jobId, "select \"" + tableKey + "\" as \"PublicID\" from \"" + tableName + "\"");
        
    }

    public void approveEntity(String jobId, String query) throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("query", query);
        config.put("statusTable", jobId + "_" + jobCategory + "_Entity_Status");
        talend.executeJob(jobId, "eCAPIApproveEntity", config);

        // Generate report for Approve
        config = new HashMap<>();
        config.put("tableName", jobId + "_" + jobCategory + "_Entity_Status");
        config.put("jobType", JOBKeywords.ENTITY_APPROVE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(jobId, "getStatusCount", config);

        // Check And ReportAny Approve
        talend.checkForStatusError(jobId, JOBKeywords.ENTITY_APPROVE, jobCategory,
                "" + jobCategory + " Approve Failed JobId : " + jobId + " Task : Approve Entity");

    }

    public void stageEntity(String jobId) throws TalendException {
        stageEntity(jobId, "select \"PublicID\" from \"" + jobId + "_" + jobCategory + "_Entity\"");
    }

    public void stageEntity(String jobId, String tableName, String tableKey) throws TalendException {
        stageEntity(jobId, "select \"" + tableKey + "\" as \"PublicID\" from \"" + tableName + "\"");
    }

    public void stageEntity(String jobId, String query) throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("query", query);
        config.put("statusTable", jobId + "_" + jobCategory + "_Entity_Status");
        talend.executeJob(jobId, "eCAPIStageEntity", config);

        // Generate report for Stage
        config = new HashMap<>();
        config.put("tableName", jobId + "_" + jobCategory + "_Entity_Status");
        config.put("jobType", JOBKeywords.ENTITY_STAGE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(jobId, "getStatusCount", config);

        // Check And ReportAny Stage
        talend.checkForStatusError(jobId, JOBKeywords.ENTITY_STAGE, jobCategory,
                "" + jobCategory + " Stage Failed JobId : " + jobId + " Task : Stage Entity");
    }

    public void liveEntity(String jobId) throws TalendException {
        liveEntity(jobId, "select \"PublicID\" from \"" + jobId + "_" + jobCategory + "_Entity\"");
    }

    public void liveEntity(String jobId, String tableName, String tableKey) throws TalendException {
        liveEntity(jobId, "select \"" + tableKey + "\" as \"PublicID\" from \"" + tableName + "\"");
        
    }

    public void liveEntity(String jobId, String query) throws TalendException {

        // Step 10 Change Stragy to Stub (catalgo only)
        HashMap<String, String> config = new HashMap<>();
        config.put("hubIntegration", "false");
        talend.executeJob(jobId, "ChangeStrategy", config);

        // Live Entity
        config = new HashMap<>();
        config.put("query", query);
        config.put("statusTable", jobId + "_" + jobCategory + "_Entity_Status");
        talend.executeJob(jobId, "eCAPILiveEntity", config);

        // Generate report for Live
        config = new HashMap<>();
        config.put("tableName", jobId + "_" + jobCategory + "_Entity_Status");
        config.put("jobType", JOBKeywords.ENTITY_LIVE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(jobId, "getStatusCount", config);

        // Check And ReportAny Live
        talend.checkForStatusError(jobId, JOBKeywords.ENTITY_LIVE, jobCategory,
                "" + jobCategory + " Live Failed JobId : " + jobId + " Task : Live Entity");

        // Change Stragy to Stub (catalgo only)
        config = new HashMap<>();
        config.put("hubIntegration", "true");
        talend.executeJob(jobId, "ChangeStrategy", config);
    }

    public void startGenericSyncProcessing(String jobId, String fileName) throws TalendException {

        // Step 1 SaveFile
        HashMap<String, String> config = new HashMap<>();
        config.put("fileName", TalendConstants.INPUT_FILE_LOCATION + fileName);
        talend.executeJob(jobId, "Read" + jobCategory + "Sheet", config);
        jobtable.save(
                new JOB(jobId, JOBKeywords.UPLOAD_FILE, jobCategory, JOBKeywords.TASK_SUCCESS,
                        jobId + "_" + jobCategory + "_InputSheet"));

        // Step 2 ValidateFile
        talend.executeJob(jobId, "Validate" + jobCategory + "Sheet", null);

        // Step 3 Check And ReportAny Error
        talend.checkForError(jobId, JOBKeywords.FILE_VALIDATION, jobCategory);

        // Step 4 cretae CAPI input
        talend.executeJob(jobId, "CAPIInput" + jobCategory + "Sheet", null);
        jobtable.save(
                new JOB(jobId, JOBKeywords.CAPI_FILE_CREATION, jobCategory, JOBKeywords.TASK_SUCCESS,
                        jobId + "_" + jobCategory + "_Entity"));

    }

    public ResponseEntity<Object> process(String jobId, MultipartFile file) {
        try {
            String fileName = this.saveFile(jobId, file);
            this.startGenericSyncProcessing(jobId, fileName);
            this.startSyncProcessing(jobId, fileName);

        } catch (TalendException e) {
            jobtable.save(new JOB(jobId, JOBKeywords.STOP, jobCategory,
                    JOBKeywords.JOB_FAILED, e.getMessage()));
            return talend.generateFailResponse(jobId, e);
        }

        return talend.generateSuccessResponse(jobId);
    }

    @Async("asyncExecutorService")
    public void processAsync(String jobId) {
        try {
            this.startASyncProcessing(jobId);
            jobtable.save(new JOB(jobId, JOBKeywords.STOP, jobCategory,
                    JOBKeywords.JOB_SUCCESS, "eaam Enjoy"));
        } catch (TalendException e) {
            // Send Email
            jobtable.save(new JOB(jobId, JOBKeywords.STOP, jobCategory,
                    JOBKeywords.JOB_FAILED, e.getRowOuptutException(jobId).toString()));
            // emailServer.sendMail(e.talendErrorMessage,
            // e.getRowOuptutException(jobId).toString());
        }
    }
}
