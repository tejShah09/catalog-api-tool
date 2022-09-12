package com.sigma.catalog.api.hubservice.services;

import java.nio.file.Paths;
import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JOB;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendConstants;

@Service
public class BundleService extends AbstractShellProcessService {

        BundleService() {
                jobCategory = JOBKeywords.BUNDLE;
        }

        public void startSyncProcessing(String jobId, String fileName) throws TalendException {

                // Step 1 SaveFile
                HashMap<String, String> config = new HashMap<>();
                config.put("fileName", TalendConstants.INPUT_FILE_LOCATION + fileName);
                talend.executeJob(jobId, "ReadBundleSheet", config);
                jobtable.save(
                                new JOB(jobId, JOBKeywords.UPLOAD_FILE, jobCategory, JOBKeywords.TASK_SUCCESS,
                                                jobId + "_Bundle_InputSheet"));

                // Step 2 ValidateFile
                talend.executeJob(jobId, "ValidateBundleSheet", null);

                // Step 3 Check And ReportAny Error
                talend.checkForError(jobId, JOBKeywords.FILE_VALIDATION, jobCategory);

                // Step 4 cretae CAPI input
                talend.executeJob(jobId, "CAPIInputBundleSheet", null);
                jobtable.save(
                                new JOB(jobId, JOBKeywords.CAPI_FILE_CREATION, jobCategory, JOBKeywords.TASK_SUCCESS,
                                                jobId + "_Bundle_Entity"));

        }

        public void startASyncProcessing(String jobId) throws TalendException {
                jobtable.save(
                                new JOB(jobId, JOBKeywords.CATALOG_REQUEST_CREATION, jobCategory,
                                                JOBKeywords.TASK_STARTED,
                                                jobId + "_Bundle_Entity"));

                // Step 1 create Catalog request and send
                HashMap<String, String> config = new HashMap<>();
                config.put("group", "Bundle");
                config.put("subSheets", "'Entity','Vintage'");
                config.put("catalogStub", "false");
                talend.executeJob(jobId, "eCAPICreateEntities", config);

                // Step 2 generate Report
                config = new HashMap<>();
                config.put("tableName", jobId + "_Bundle_Entity");
                config.put("jobType", JOBKeywords.CATALOG_REQUEST_SUBMITION);
                config.put("jobCategory", jobCategory);
                config.put("keyName", "Name");
                config.put("jobStatus", JOBKeywords.TASK_END);
                talend.executeJob(jobId, "getStatusCount", config);

                // Step 3 Check And ReportAny Error
                talend.checkForStatusError(jobId, JOBKeywords.CATALOG_REQUEST_SUBMITION, jobCategory,
                                "Bundle Upload Failed JobId : " + jobId + " Task : Entity Creation");

                // step 4 Aporve Bundle
                config = new HashMap<>();
                config.put("query", "select \"PublicID\" from \"" + jobId + "_Bundle_Entity\"");
                config.put("statusTable", jobId + "_Bundle_Entity_Status");
                talend.executeJob(jobId, "eCAPIApproveEntity", config);

                // step 5 Generate report for Approve
                config = new HashMap<>();
                config.put("tableName", jobId + "_Bundle_Entity_Status");
                config.put("jobType", JOBKeywords.ENTITY_APPROVE);
                config.put("jobCategory", jobCategory);
                config.put("keyName", "PublicID");
                config.put("jobStatus", JOBKeywords.TASK_END);
                talend.executeJob(jobId, "getStatusCount", config);

                // Step 6 Check And ReportAny Approve
                talend.checkForStatusError(jobId, JOBKeywords.ENTITY_APPROVE, jobCategory,
                                "Bundle Approve Failed JobId : " + jobId + " Task : Approve Entity");

                // step 7 Stage Bundle
                config = new HashMap<>();
                config.put("query", "select \"PublicID\" from \"" + jobId + "_Bundle_Entity\"");
                config.put("statusTable", jobId + "_Bundle_Entity_Status");
                talend.executeJob(jobId, "eCAPIStageEntity", config);

                // step 8 Generate report for Stage
                config = new HashMap<>();
                config.put("tableName", jobId + "_Bundle_Entity_Status");
                config.put("jobType", JOBKeywords.ENTITY_STAGE);
                config.put("jobCategory", jobCategory);
                config.put("keyName", "PublicID");
                config.put("jobStatus", JOBKeywords.TASK_END);
                talend.executeJob(jobId, "getStatusCount", config);

                // Step 9 Check And ReportAny Stage
                talend.checkForStatusError(jobId, JOBKeywords.ENTITY_STAGE, jobCategory,
                                "Bundle Stage Failed JobId : " + jobId + " Task : Stage Entity");

                // Step 10 Change Stragy to Stub (catalgo only)
                config = new HashMap<>();
                config.put("hubIntegration", "false");
                talend.executeJob(jobId, "ChangeStrategy", config);

                // step 11 Live Bundle
                config = new HashMap<>();
                config.put("query", "select \"PublicID\" from \"" + jobId + "_Bundle_Entity\"");
                config.put("statusTable", jobId + "_Bundle_Entity_Status");
                talend.executeJob(jobId, "eCAPILiveEntity", config);

                // step 12 Generate report for Live
                config = new HashMap<>();
                config.put("tableName", jobId + "_Bundle_Entity_Status");
                config.put("jobType", JOBKeywords.ENTITY_LIVE);
                config.put("jobCategory", jobCategory);
                config.put("keyName", "PublicID");
                config.put("jobStatus", JOBKeywords.TASK_END);
                talend.executeJob(jobId, "getStatusCount", config);

                // Step 13 Check And ReportAny Live
                talend.checkForStatusError(jobId, JOBKeywords.ENTITY_LIVE, jobCategory,
                                "Bundle Live Failed JobId : " + jobId + " Task : Live Entity");

                // Step 14 Change Stragy to Stub (catalgo only)
                config = new HashMap<>();
                config.put("hubIntegration", "true");
                talend.executeJob(jobId, "ChangeStrategy", config);

        }

        public String saveFile(String jobId, MultipartFile file) throws TalendException {
                String fileName = "BundleUploadFile_" + jobId + ".csv";
                talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileName);
                jobtable.save(new JOB(jobId, JOBKeywords.START, jobCategory,
                                JOBKeywords.TASK_SUCCESS,
                                file.getOriginalFilename() + " :: " + fileName));
                return fileName;
        }

}
