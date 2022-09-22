package com.sigma.catalog.api.hubservice.services;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JOB;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.repository.JOBRepository;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.ConfigurationUtility;
import com.sigma.catalog.api.utility.StringUtility;

@Service
public class JobService {

    @Autowired
    public JOBRepository jobtable;

    @Autowired
    public TalendHelperService talend;

    @Autowired
    private EmailService emailServer;

    @Autowired
    private JobService jobService;

    public void stageEntity(JobProperites properties, String query, String statusTable, String jobCategory)
            throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("query", query);
        config.put("statusTable", statusTable);
        talend.executeJob(properties.jobId, "eCAPIStageEntity", config);

        // Generate report for Stage
        config = new HashMap<>();
        config.put("tableName", statusTable);
        config.put("jobType", JOBKeywords.ENTITY_STAGE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Stage
        checkForStatusError(properties.jobId, JOBKeywords.ENTITY_STAGE, jobCategory,
                "" + jobCategory + " Stage Failed JobId : " + properties.jobId + " Task : Stage Entity");
    }

    public void changeStrategy(JobProperites properties, boolean hubIntegration) throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        if (properties.isChangeStrategy() && properties.isLaunchEntity()) {
            config.put("hubIntegration", String.valueOf(hubIntegration));
            config.put("instanceId", ConfigurationUtility.getEnvConfigModel().getCatalogInstance());
            talend.executeJob(properties.jobId, "ChangeStrategy", config);
        }
    }

    public void liveEntity(JobProperites properties, String query, String statusTable, String jobCategory)
            throws TalendException {

        HashMap<String, String> config = new HashMap<>();

        // Live Entity
        if (properties.isLaunchEntity()) {
            config = new HashMap<>();
            config.put("query", query);
            config.put("statusTable", statusTable);
            talend.executeJob(properties.jobId, "eCAPILiveEntity", config);

            // Generate report for Live
            config = new HashMap<>();
            config.put("tableName", statusTable);
            config.put("jobType", JOBKeywords.ENTITY_LIVE);
            config.put("jobCategory", jobCategory);
            config.put("keyName", "PublicID");
            config.put("jobStatus", JOBKeywords.TASK_END);
            talend.executeJob(properties.jobId, "getStatusCount", config);

            // Check And ReportAny Live
            checkForStatusError(properties.jobId, JOBKeywords.ENTITY_LIVE, jobCategory,
                    "" + jobCategory + " Live Failed JobId : " + properties.jobId + " Task : Live Entity");

        }

    }

    public void waitLiveTobeCompleted(JobProperites properites, String reportLiveTable, String inputLiveTable,
            String inputLiveKey, String jobCategory) throws TalendException {

        if (properites.isLaunchEntity()) {
            Integer previosCount = 0;
            Integer currentCount = 0;
            Integer itteration = 0;
            do {
                itteration++;
                previosCount = currentCount;
                System.out.println(properites.jobId + " Waiting for Launches to complete try " + itteration);
                HashMap<String, String> config = new HashMap<>();
                config.put("catalogReportTable", reportLiveTable);
                config.put("entityNameKey", inputLiveKey);
                config.put("entityTable", inputLiveTable);
                config.put("jobType", JOBKeywords.ENTITY_LIVE_CHECK_TRY_ + String.valueOf(itteration));
                config.put("jobCategory", jobCategory);
                talend.executeJob(properites.jobId, "eCAPICheckLiveStatusEntityName", config);
                currentCount = getLiveCountFromJobs(properites.jobId,
                        JOBKeywords.ENTITY_LIVE_CHECK_TRY_ + String.valueOf(itteration), jobCategory);
                System.out.println(
                        properites.jobId + " Waiting for Launches to complete. Launch pending " + currentCount);
            } while (previosCount >= 0 && previosCount != currentCount);

            if (currentCount != 0) {
                throw new TalendException("JOB ID: " + properites.jobId + " JOBS Lauches stuck");
            }
        }

    }

    public Integer getLiveCountFromJobs(String jobId, String jobType, String jobCategory) throws TalendException {
        List<JOB> statusJob = jobtable.findJobValidationStatus(jobId, jobType, jobCategory);
        Integer count = -1;
        if (statusJob != null && statusJob.size() > 0) {

            try {

                if (statusJob.get(0).getMessage() == null) {
                    throw new TalendException("NO COUNT FOUND for LIVE STATUS CHECK");
                } else {
                    count = Integer.valueOf(statusJob.get(0).getMessage());
                }

            } catch (TalendException e) {
                throw e;
            } catch (Exception e) {
                throw new TalendException("Cannot covert Entity LIVE count " + e.getMessage());
            }

        } else {
            throw new TalendException("NO COUNT FOUND for LIVE STATUS CHECK");
        }
        return count;
    }

    public void approveEntity(JobProperites properties, String query, String statusTable, String jobCategory)
            throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("query", query);
        config.put("statusTable", statusTable);
        talend.executeJob(properties.jobId, "eCAPIApproveEntity", config);

        // Generate report for Approve
        config = new HashMap<>();
        config.put("tableName", statusTable);
        config.put("jobType", JOBKeywords.ENTITY_APPROVE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Approve
        checkForStatusError(properties.jobId, JOBKeywords.ENTITY_APPROVE, jobCategory,
                "" + jobCategory + " Approve Failed JobId : " + properties.jobId + " Task : Approve Entity");

    }

    public void sendReconfile(JobProperites properties, String inputTable, String inpuTableKey, String eReportTable,
            String entityType, String jobCategory)
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
        config.put("jobCategory", jobCategory);
        talend.executeJob(properties.jobId, "SendReconSheetToHub", config);

        checkTextError(properties.jobId, JOBKeywords.HUB_RECON_SUBMITION, jobCategory);

    }

    public void editEntityName(JobProperites properties, String inputTable, String inpuTableKey, String eReportTable,
            String jobCategory, String statusTable)
            throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("catalogReportTable", eReportTable);
        config.put("entityNameKey", inpuTableKey);
        config.put("entityTable", inputTable);
        config.put("statusTable", statusTable);
        talend.executeJob(properties.jobId, "eCAPIEditEntityName", config);

        // Generate report for Edit
        config = new HashMap<>();
        config.put("tableName", statusTable);
        config.put("jobType", JOBKeywords.ENTITY_EDIT);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Edit
        checkForStatusError(properties.jobId, JOBKeywords.ENTITY_EDIT, jobCategory,
                "" + jobCategory + " Edit Failed JobId : " + properties.jobId + " Task : Edit Entity");
    }

    public void deleteNonLiveEntityName(JobProperites properties, String inputTable,
            String inpuTableKey, String eReportTable, String jobCategory, String statusTable)
            throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("catalogReportTable", eReportTable);
        config.put("entityNameKey", inpuTableKey);
        config.put("entityTable", inputTable);
        config.put("statusTable", statusTable);
        talend.executeJob(properties.jobId, "eCAPIDeleteNonLiveEntityName", config);

        // Generate report for Delte
        config = new HashMap<>();
        config.put("tableName", statusTable);
        config.put("jobType", JOBKeywords.ENTITY_DELETE);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Delte
        checkForStatusError(properties.jobId, JOBKeywords.ENTITY_DELETE, jobCategory,
                "" + jobCategory + " Delete Failed JobId : " + properties.jobId + " Task : Delete Entity", true);
    }

    public void createRates(JobProperites properties, String group, String jobCategory) throws TalendException {
        jobtable.save(
                new JOB(properties.jobId, JOBKeywords.CATALOG_RATE_CREATION, jobCategory,
                        JOBKeywords.TASK_STARTED,
                        properties.jobId + "_" + group + "_Rates"));
        HashMap<String, String> config = new HashMap<>();
        config.put("group", group);
        config.put("catalogStub", "false");
        talend.executeJob(properties.jobId, "eCAPICreateRates", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + group + "_Rates");
        config.put("jobType", JOBKeywords.CATALOG_RATE_SUBMITION);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "Parent_Entity_Name");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Error
        checkForStatusError(properties.jobId, JOBKeywords.CATALOG_RATE_SUBMITION, jobCategory,
                "" + jobCategory + " Upload Failed JobId : " + properties.jobId + " Task : Association Creation");
    }

    public void createAssoc(JobProperites properties, String group, String jobCategory) throws TalendException {
        jobtable.save(
                new JOB(properties.jobId, JOBKeywords.CATALOG_ASSOC_CREATION, jobCategory,
                        JOBKeywords.TASK_STARTED,
                        properties.jobId + "_" + group + "_Associations"));
        HashMap<String, String> config = new HashMap<>();
        config.put("group", group);
        config.put("catalogStub", "false");
        talend.executeJob(properties.jobId, "eCAPICreateAssoc", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + group + "_Associations");
        config.put("jobType", JOBKeywords.CATALOG_ASSOC_SUBMITION);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "Parent_Entity_Name");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Error
        checkForStatusError(properties.jobId, JOBKeywords.CATALOG_ASSOC_SUBMITION, jobCategory,
                "" + jobCategory + " Upload Failed JobId : " + properties.jobId + " Task : Association Creation");
    }

    public void createEntity(JobProperites properties, String group, String jobCategory, String sheetList)
            throws TalendException {
        jobtable.save(
                new JOB(properties.jobId, JOBKeywords.CATALOG_REQUEST_CREATION, jobCategory,
                        JOBKeywords.TASK_STARTED,
                        properties.jobId + "_" + group + "_Entity"));
        HashMap<String, String> config = new HashMap<>();
        config.put("group", group);
        config.put("subSheets", sheetList);
        config.put("catalogStub", "false");
        talend.executeJob(properties.jobId, "eCAPICreateEntities", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + group + "_Entity");
        config.put("jobType", JOBKeywords.CATALOG_REQUEST_SUBMITION);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "Name");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        // Check And ReportAny Error
        checkForStatusError(properties.jobId, JOBKeywords.CATALOG_REQUEST_SUBMITION, jobCategory,
                "" + jobCategory + " Upload Failed JobId : " + properties.jobId + " Task : Entity Creation");
    }

    public void validateForEmptyInput(JobProperites properties, String tableName, String jobCategory)
            throws TalendException {

        HashMap<String, String> config = new HashMap<>();
        config.put("tableName", tableName);
        config.put("jobType", JOBKeywords.FILE_ROW_COUNT);
        config.put("jobCategory", jobCategory);

        talend.executeJob(properties.jobId, "RowCountFromTable", config);

        // Check And ReportAny Error
        checkForError(properties.jobId, JOBKeywords.FILE_ROW_COUNT, jobCategory);
    }

    public void readFileJob(JobProperites properties, String fileName, String jobCategory) throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        config.put("fileName", TalendConstants.INPUT_FILE_LOCATION + fileName);
        talend.executeJob(properties.jobId, "Read" + jobCategory + "Sheet", config);
        jobtable.save(
                new JOB(properties.jobId, JOBKeywords.UPLOAD_FILE, jobCategory, JOBKeywords.TASK_SUCCESS,
                        properties.jobId + "_" + jobCategory + "_InputSheet"));
    }

    public void validateFileJob(JobProperites properties, String jobCategory) throws TalendException {
        talend.executeJob(properties.jobId, "Validate" + jobCategory + "Sheet", null);
        checkForError(properties.jobId, JOBKeywords.FILE_VALIDATION, jobCategory);
    }

    public void createCAPIFileJob(JobProperites properties, String jobCategory) throws TalendException {
        talend.executeJob(properties.jobId, "CAPIInput" + jobCategory + "Sheet", null);
        jobtable.save(
                new JOB(properties.jobId, JOBKeywords.CAPI_FILE_CREATION, jobCategory, JOBKeywords.TASK_SUCCESS,
                        properties.jobId + "_" + jobCategory + "_Entity"));
    }

    public String saveCSVFile(String jobId, String jobCategory, MultipartFile file) throws TalendException {
        String fileName = "" + jobCategory + "UploadFile_" + jobId + ".csv";
        talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileName);
        jobtable.save(new JOB(jobId, JOBKeywords.START, jobCategory,
                JOBKeywords.TASK_SUCCESS,
                file.getOriginalFilename() + " :: " + fileName));
        return fileName;
    }

    public void checkForError(String jobId, String jobType, String jobCategory) throws TalendException {
        List<JOB> statusJob = jobtable.findJobValidationStatus(jobId, jobType, jobCategory);
        if (statusJob != null && statusJob.size() > 0
                && !StringUtility.equals(statusJob.get(0).getStatus(), JOBKeywords.TASK_SUCCESS)) {
            Map<String, List<String>> map;
            try {
                ObjectMapper mapper = new ObjectMapper();
                map = mapper.readValue(statusJob.get(0).getMessage(), Map.class);
            } catch (Exception e) {
                throw new TalendException(e.getMessage());
            }
            TalendException e = new TalendException(map);
            throw e;

        }
    }

    public void checkTextError(String jobId, String jobType, String jobCategory) throws TalendException {
        List<JOB> statusJob = jobtable.findJobValidationStatus(jobId, jobType, jobCategory);
        if (statusJob != null && statusJob.size() > 0
                && !StringUtility.equals(statusJob.get(0).getStatus(), JOBKeywords.TASK_SUCCESS)) {

            throw new TalendException("JOB ID " + jobId + " Failed");

        }
    }

    public void jobValidation(String jobId) throws TalendException {
        List<JOB> statusJob = jobtable.findJobEvents(jobId);
        if (statusJob != null && statusJob.size() > 0) {

            throw new TalendException("JOB ID " + jobId + " is already Present");

        }
    }

    public void saveErrorAndSendErrorEmail(JobProperites properties, String category, String errorMsg) {
        jobtable.save(new JOB(properties.jobId, JOBKeywords.STOP, category,
                JOBKeywords.JOB_FAILED, errorMsg));

        List<JOB> jobs = jobtable.findJobEvents(properties.jobId);

        errorMsg = errorMsg + "\n\n\n\n JOB Events Status ****************************\n" + new GsonBuilder().setPrettyPrinting().create().toJson(jobs);
        emailServer.sendMail(properties, "[" +
                ConfigurationUtility.getEnvConfigModel().getEnvironment() + "]JOB failed Id : " + properties.jobId
                + " jobName : " + category,
                errorMsg);
    }

    public void saveSucessJobs(JobProperites properties, String category) {
        jobtable.save(new JOB(properties.jobId, JOBKeywords.STOP, category,
                JOBKeywords.JOB_SUCCESS, "eaam Enjoy JOB Success"));
    }

    public void checkForStatusError(String jobId, String jobType, String jobCategory, String CustomError)
            throws TalendException {
        checkForStatusError(jobId, jobType, jobCategory, CustomError, false);
    }

    public void checkForStatusError(String jobId, String jobType, String jobCategory, String CustomError,
            boolean isNullReportAllowed)
            throws TalendException {
        List<JOB> statusJob = jobtable.findJobValidationStatus(jobId, jobType, jobCategory);
        if (statusJob != null && statusJob.size() > 0) {
            List<Map<String, Object>> countList = new ArrayList<>();
            try {
                ObjectMapper mapper = new ObjectMapper();
                if (statusJob.get(0).getMessage() == null) {
                    if (!isNullReportAllowed) {
                        throw new TalendException("NO REPORT FOUND TASK");
                    }
                } else {
                    countList = mapper.readValue(statusJob.get(0).getMessage(), List.class);
                }

            } catch (TalendException e) {
                throw e;
            } catch (Exception e) {
                throw new TalendException(e.getMessage());
            }

            boolean failedRecordfound = false;

            for (Map<String, Object> ctObj : countList) {
                if (!StringUtility.contains((String) ctObj.get("response"), "success")) {
                    failedRecordfound = true;
                }

            }

            if (failedRecordfound) {
                TalendException e = new TalendException(CustomError, countList);
                throw e;
            }

        } else {
            throw new TalendException("NO COUNT FOUND");
        }
    }
}
