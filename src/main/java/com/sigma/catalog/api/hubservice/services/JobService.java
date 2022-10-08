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

    public void changeStrategy(JobProperites properties, boolean hubIntegration) throws TalendException {
        HashMap<String, String> config = new HashMap<>();
        if (properties.isChangeStrategy() && properties.isLaunchEntity()) {
            config.put("hubIntegration", String.valueOf(hubIntegration));
            config.put("instanceId", ConfigurationUtility.getEnvConfigModel().getCatalogInstanceID());
            talend.executeJob(properties.jobId, "ChangeStrategy", config);
        }
    }

    public void liveEntity(JobProperites properties, String inpuTable, String entityTable, String statusTable,
            String jobCategory)
            throws TalendException {
        if (properties.isLaunchEntity()) {
            try {
                changeWorkFlow(properties, inpuTable, entityTable, statusTable, "Live", jobCategory);
            } catch (TalendException e) {
                if (isStatusCheckErrorPresent(properties.jobId, JOBKeywords.ENTITY_LIVE,
                        jobCategory, "failure,103")) {
                    jobtable.save(
                            new JOB(properties.jobId, JOBKeywords.RESTART_IIS, jobCategory,
                                    JOBKeywords.TASK_STARTED,
                                    ConfigurationUtility.getEnvConfigModel().getCatalogDataAPIInstance()));

                    try {
                        System.out.println("Stoping WebSite "
                                + ConfigurationUtility.getEnvConfigModel().getCatalogDataAPIInstance());
                        routines.WindowService
                                .stopWebSite(ConfigurationUtility.getEnvConfigModel().getCatalogDataAPIInstance());
                        Thread.sleep(2000);
                        System.out.println("Starting WebSite "
                                + ConfigurationUtility.getEnvConfigModel().getCatalogDataAPIInstance());
                        routines.WindowService
                                .startWebSite(ConfigurationUtility.getEnvConfigModel().getCatalogDataAPIInstance());

                        Thread.sleep(2000);
                        System.out.println("restarting  application  pools "
                                + ConfigurationUtility.getEnvConfigModel().getCatalogDataAPIInstance());
                        routines.WindowService
                                .runCommand("powershell.exe Restart-WebAppPool "
                                        + ConfigurationUtility.getEnvConfigModel().getCatalogDataAPIInstance());
                        Thread.sleep(10000);
                        jobtable.save(
                                new JOB(properties.jobId, JOBKeywords.RESTART_IIS, jobCategory,
                                        JOBKeywords.TASK_END,
                                        ConfigurationUtility.getEnvConfigModel().getCatalogDataAPIInstance()));
                    } catch (Exception err) {
                        jobtable.save(
                                new JOB(properties.jobId, JOBKeywords.RESTART_IIS, jobCategory,
                                        JOBKeywords.TASK_FAILED,
                                        ConfigurationUtility.getEnvConfigModel().getCatalogDataAPIInstance()));
                        System.out.println("Unable to restart webservice");
                    }

                    changeWorkFlow(properties, inpuTable, entityTable, statusTable, "Live", jobCategory);

                } else {
                    throw e;
                }

            }

        }
    }

    public void liveEntityAndWaitToComplete(JobProperites properites, String targeJobId, String jobCategory)
            throws TalendException {
        try {
            changeStrategy(properites, false);
            liveEntity(properites, targeJobId + "_" + jobCategory + "_Entity",
                    properites.jobId + "_" + jobCategory + "_Report",
                    properites.jobId + "_" + jobCategory + "_Entity_Status", jobCategory);

            String response = waitLiveTobeCompleted(properites,
                    targeJobId + "_" + jobCategory + "_Entity", jobCategory);

            if (!StringUtility.equalsIgnoreCase(response, JOBKeywords.live)) {
                throw new TalendException("Launced failed " + response);
            }
        } catch (TalendException e) {
            throw e;
        } finally {
            changeStrategy(properites, true);
        }
    }

    public String waitLiveTobeCompleted(JobProperites properites, String inputLiveTable,
            String jobCategory) throws TalendException {
        String reponse = JOBKeywords.live;
        if (properites.isLaunchEntity()) {
            Integer previosCount = 0;
            Integer currentCount = 0;
            Integer itteration = 0;
            Integer failedCount = 0;
            Integer waitingToStart = 0;
            do {
                itteration++;
                previosCount = currentCount;
                System.out.println(properites.jobId + " Waiting for Launches to complete try " + itteration);
                HashMap<String, String> config = new HashMap<>();
                config.put("inputTable", inputLiveTable);

                config.put("entityTable", inputLiveTable);
                config.put("jobType", JOBKeywords.ENTITY_LIVE_CHECK_TRY_ + String.valueOf(itteration));
                config.put("jobCategory", jobCategory);
                talend.executeJob(properites.jobId, "CheckForLaunchProcess", config);
                Map<String, Integer> status = getCountFromJobs(properites.jobId,
                        JOBKeywords.ENTITY_LIVE_CHECK_TRY_ + String.valueOf(itteration), jobCategory);
                currentCount = status.get(JOBKeywords.nonlive);
                System.out.println(
                        properites.jobId + " Waiting for Launches to complete. Launch pending " + currentCount);
                failedCount = status.get(JOBKeywords.failed);
                waitingToStart = status.get(JOBKeywords.waitingToStart);
                if (currentCount != 0 && failedCount == currentCount && waitingToStart == 0) {
                    System.out.println("Launch failed " + failedCount);
                    reponse = JOBKeywords.failed;
                    break;
                }

            } while (previosCount >= 0 && previosCount != currentCount);

            if (currentCount != 0 && currentCount == waitingToStart) {
                System.out.println("Launch stuck " + waitingToStart);
                reponse = JOBKeywords.waitingToStart;
            } else if (currentCount != 0) {

                System.out.println("Launch nonlive " + currentCount);
                reponse = JOBKeywords.nonlive;
            }
        }
        return reponse;

    }

    public Map<String, Integer> getCountFromJobs(String jobId, String jobType, String jobCategory)
            throws TalendException {
        List<JOB> statusJob = jobtable.findJobValidationStatus(jobId, jobType, jobCategory);
        if (statusJob != null && statusJob.size() > 0) {
            try {
                if (StringUtility.equals(statusJob.get(0).getStatus(), JOBKeywords.TASK_FAILED)) {
                    throw new TalendException("LAUNCH_FAILED");
                } else if (statusJob.get(0).getMessage() == null) {
                    throw new TalendException("NO COUNT FOUND for LIVE STATUS CHECK");
                } else {

                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Integer>> listMap = mapper.readValue(statusJob.get(0).getMessage(), List.class);
                    if (listMap != null && listMap.size() > 0) {
                        return listMap.get(0);
                    } else {
                        throw new TalendException("UNABLE to FIND LAUCH STATUS");
                    }
                }
            } catch (TalendException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new TalendException("Cannot covert Entity LIVE count ");
            }
        } else {
            throw new TalendException("NO COUNT FOUND for LIVE STATUS CHECK");
        }
    }

    public void changeWorkFlow(JobProperites properties, String inputTable, String entityTable, String statusTable,
            String targetState,
            String jobCategory) throws TalendException {
        createEntityReport(properties, inputTable, entityTable);

        HashMap<String, String> config = new HashMap<>();
        config.put("entityTable", entityTable);
        config.put("targetStatus", targetState);
        config.put("statusTable", statusTable);
        talend.executeJob(properties.jobId, "ChangeWorkFlow", config);

        config = new HashMap<>();
        config.put("tableName", statusTable);
        config.put("jobType", targetState);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "PublicID");
        config.put("jobStatus", JOBKeywords.TASK_END);
        talend.executeJob(properties.jobId, "getStatusCount", config);

        createEntityReport(properties, inputTable, entityTable);
        checkForStatusError(properties.jobId, targetState, jobCategory,
                "" + jobCategory + " " + targetState + " Failed JobId : " + properties.jobId + " Task : " + targetState
                        + " Entity");
    }

    public void createEntityReport(JobProperites properties, String inputTable, String reportTable)
            throws TalendException {

        HashMap<String, String> config = new HashMap<>();
        config.put("inputTable", inputTable);
        config.put("reportTable", reportTable);
        talend.executeJob(properties.jobId, "CreateReconSheet", config);

    }

    public void sendEntityReportToHUB(JobProperites properties, String jobCategory, String inputTable,
            String reportTable)
            throws TalendException {
        createEntityReport(properties, inputTable, reportTable);
        if (!properties.isSendReconSheet() || !properties.isLaunchEntity()) {
            System.out.println("Recon file is skipped");
            return;
        }
        HashMap<String, String> config = new HashMap<>();

        config.put("jobCategory", jobCategory);
        config.put("reportTable", reportTable);
        talend.executeJob(properties.jobId, "SendReconSheet", config);
        checkTextError(properties.jobId, JOBKeywords.HUB_RECON_SUBMITION,
                jobCategory);
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

    public void createRateRecall(JobProperites properties, String group, String jobCategory) throws TalendException {
        jobtable.save(
                new JOB(properties.jobId, JOBKeywords.CATALOG_RATE_CREATION, jobCategory,
                        JOBKeywords.TASK_STARTED,
                        properties.jobId + "_" + group + "_Rates"));
        HashMap<String, String> config = new HashMap<>();
        config.put("catalogStub", "false");
        talend.executeJob(properties.jobId, "eCAPICreateRateRecall", config);

        // generate Report
        config = new HashMap<>();
        config.put("tableName", properties.jobId + "_" + group + "_Entity");
        config.put("jobType", JOBKeywords.CATALOG_RATE_SUBMITION);
        config.put("jobCategory", jobCategory);
        config.put("keyName", "id");
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

    public void validateForEmptyInput(JobProperites properties, String tableName, String jobCategory, String jobType)
            throws TalendException {

        HashMap<String, String> config = new HashMap<>();
        config.put("tableName", tableName);
        config.put("jobType", jobType);
        config.put("jobCategory", jobCategory);

        talend.executeJob(properties.jobId, "RowCountFromTable", config);

        // Check And ReportAny Error
        checkForError(properties.jobId, jobType, jobCategory);
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

    public void jobValidation(String jobId, String jobCategory) throws TalendException {
        List<JOB> statusJob = jobtable.findJobEvents(jobId);
        if (statusJob != null && statusJob.size() > 0) {

            throw new TalendException("JOB ID " + jobId + " is already Present");

        }
        jobtable.save(new JOB(jobId, JOBKeywords.JOB_ID_VALIDATION, jobCategory,
                JOBKeywords.TASK_SUCCESS, null));
    }

    public void saveErrorAndSendErrorEmail(JobProperites properties, String category, String errorMsg) {
        jobtable.save(new JOB(properties.jobId, JOBKeywords.STOP, category,
                JOBKeywords.JOB_FAILED, errorMsg));

        List<JOB> jobs = jobtable.findJobEvents(properties.jobId);

        errorMsg = errorMsg + "\n\n\n\n JOB Events Status ****************************\n"
                + new GsonBuilder().setPrettyPrinting().create().toJson(jobs);
        emailServer.sendMail(properties, "[" +
                ConfigurationUtility.getEnvConfigModel().getEnvironment() + "] JOB failed Id : " + properties.jobId
                + " jobName : " + category,
                errorMsg);
    }

    public void saveSucessJobs(JobProperites properties, String category) {
        jobtable.save(new JOB(properties.jobId, JOBKeywords.STOP, category,
                JOBKeywords.JOB_SUCCESS, "eaam Enjoy JOB Success"));
        emailServer.sendSuccessMail(properties, "[" +
                ConfigurationUtility.getEnvConfigModel().getEnvironment() + "] JOB Success Id : " + properties.jobId
                + " jobName : " + category + "");
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

    public boolean isStatusCheckErrorPresent(String jobId, String jobType, String jobCategory, String customError)
            throws TalendException {
        boolean failedRecordfound = false;
        List<JOB> statusJob = jobtable.findJobValidationStatus(jobId, jobType, jobCategory);
        if (statusJob != null && statusJob.size() > 0) {
            List<Map<String, Object>> countList = new ArrayList<>();
            try {
                ObjectMapper mapper = new ObjectMapper();
                if (statusJob.get(0).getMessage() != null) {

                    countList = mapper.readValue(statusJob.get(0).getMessage(), List.class);
                }

            } catch (Exception e) {
                throw new TalendException(e.getMessage());
            }

            for (Map<String, Object> ctObj : countList) {
                if (StringUtility.contains(StringUtility.trim((String) ctObj.get("response")), customError)) {
                    failedRecordfound = true;
                }

            }

        }

        return failedRecordfound;
    }
}
