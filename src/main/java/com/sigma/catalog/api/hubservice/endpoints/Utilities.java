package com.sigma.catalog.api.hubservice.endpoints;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.services.EmailService;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.ConfigurationUtility;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/utility")
public class Utilities {

    @Autowired
    private TalendHelperService talend;

    @Autowired
    private EmailService emailServer;

    @PostMapping("/sendEmail")
    public ResponseEntity<Object> sendMail(
            @RequestBody Map<String, String> request) {
        try {
            String jobId = talend.generateUniqJobId();
            if (!StringUtility.isEmpty(request.get("jobId"))) {
                jobId = request.get("jobId");
            }
            String subject = "TEST EMAIL";
            if (!StringUtility.isEmpty(request.get("subject"))) {
                subject = request.get("subject");
            }
            String body = "{}{}{}{}{}";
            if (!StringUtility.isEmpty(request.get("body"))) {
                body = request.get("body");
            }
            emailServer.sendMail(new JobProperites(jobId), subject, body);

            return new ResponseEntity<Object>("Sent", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/changeStrategy")
    public ResponseEntity<Object> changeStrategy(
            @RequestBody Map<String, String> request) {

        HashMap<String, String> config = new HashMap<>();
        String jobId = talend.generateUniqJobId();
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        try {

            config.put("hubIntegration",
                    StringUtility.isEmpty(request.get("hubIntegration")) ? "true" : request.get("hubIntegration"));
            config.put("instanceId", ConfigurationUtility.getEnvConfigModel().getCatalogInstance());
            talend.executeJob(jobId, "ChangeStrategy", config);
            return talend.getSucessResponse(jobId);

        } catch (TalendException e) {
            return talend.generateFailResponse(jobId, e);
        }

    }

    @PostMapping("/executeJob")
    public ResponseEntity<Object> ExecuteJob(@RequestBody Map<String, String> request) {
        HashMap<String, String> config = new HashMap<String, String>(request);
        String jobId = talend.generateUniqJobId();
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        try {
            talend.executeJob(jobId, config.get("jobName"), config);
            return talend.getSucessResponse(jobId);

        } catch (TalendException e) {
            return talend.generateFailResponse(jobId, e);
        }

    }

    @PostMapping("/executeJobAndFindResult")
    public ResponseEntity<Object> executeJobAndFindResult(@RequestBody Map<String, String> request) {
        HashMap<String, String> config = new HashMap<String, String>(request);
        String jobId = talend.generateUniqJobId();
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        try {
            talend.executeJob(jobId, config.get("jobName"), config);

            HashMap<String, String> configNew = new HashMap<>();
            configNew.put("tableName", config.get("statusTable"));
            configNew.put("jobType", JOBKeywords.ADD_HOOK);
            configNew.put("jobCategory", config.get("jobName"));
            configNew.put("keyName", config.get("keyName"));
            configNew.put("jobStatus", JOBKeywords.TASK_END);
            talend.executeJob(jobId, "getStatusCount", configNew);

        } catch (TalendException e) {
            return talend.generateFailResponse(jobId, e);
        }

        try {
            talend.checkForStatusError(jobId, JOBKeywords.ADD_HOOK, config.get("jobName"),
                    config.get("jobName") + " Failed JobId : " + jobId);
        } catch (TalendException e) {
            return talend.generateFailCountResponse(jobId, e);
        }
        return talend.getSucessResponse(jobId);
    }

}
