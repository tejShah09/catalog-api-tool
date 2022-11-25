package com.sigma.catalog.api.hubservice.endpoints;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.sigma.catalog.api.authentication.JKSFileLoad;
import com.sigma.catalog.api.catalog.CatalogCommunicator;
import com.sigma.catalog.api.catalog.FactsForAttributes;
import com.sigma.catalog.api.configuration.CatalogConstants;
import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.services.EmailService;
import com.sigma.catalog.api.hubservice.services.JobService;
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

    @Autowired
    private JobService jobservice;

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
            config.put("instanceId", ConfigurationUtility.getEnvConfigModel().getCatalogInstanceID());
            talend.executeJob(jobId, "ChangeStrategy", config);
            return talend.getSucessResponse(jobId);

        } catch (TalendException e) {
            return talend.generateFailResponse(jobId, e);
        }

    }

    @PostMapping("/covertToCAPISheet")
    public ResponseEntity<Object> covertToCAPISheet(
            @RequestBody Map<String, String> request) {

        HashMap<String, String> config = new HashMap<>();
        String jobId = talend.generateUniqJobId();
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        String targetJobId;
        if (!StringUtility.isEmpty(request.get("targetJobId"))) {
            targetJobId = request.get("targetJobId");
        } else {
            return new ResponseEntity<Object>("targetJobId_not_found", HttpStatus.BAD_REQUEST);
        }
        String jobCategory;
        if (!StringUtility.isEmpty(request.get("jobCategory"))) {
            jobCategory = request.get("jobCategory");
        } else {
            return new ResponseEntity<Object>("jobCategory_not_found", HttpStatus.BAD_REQUEST);
        }

        try {

            
            talend.executeJob(targetJobId, "ConvertExcel"+jobCategory+"Sheet", config);
            return talend.getSucessResponse(targetJobId);

        } catch (TalendException e) {
            return talend.generateFailResponse(targetJobId, e);
        }

    }

    @PostMapping("/executeJob")
    public ResponseEntity<Object> ExecuteJob(@RequestBody Map<String, String> request) {
        HashMap<String, String> config = new HashMap<String, String>(request);
        String jobId = talend.generateUniqJobId();
        jobservice.startJOB(jobId, JOBKeywords.ADD_HOOK);
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        try {
            talend.executeJob(jobId, config.get("jobName"), config);
            jobservice.stopJOB(jobId, JOBKeywords.ADD_HOOK);
            return talend.getSucessResponse(jobId);

        } catch (TalendException e) {
            jobservice.failedJOB(jobId, JOBKeywords.ADD_HOOK, String.valueOf(e.getCustomException(jobId)));
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
            jobservice.startJOB(jobId, JOBKeywords.ADD_HOOK);
            talend.executeJob(jobId, config.get("jobName"), config);

            HashMap<String, String> configNew = new HashMap<>();
            configNew.put("tableName", config.get("statusTable"));
            configNew.put("jobType", JOBKeywords.ADD_HOOK);
            configNew.put("jobCategory", config.get("jobName"));
            configNew.put("keyName", config.get("keyName"));
            configNew.put("jobStatus", JOBKeywords.TASK_END);
            talend.executeJob(jobId, "getStatusCount", configNew);

        } catch (TalendException e) {
            jobservice.failedJOB(jobId, JOBKeywords.ADD_HOOK, String.valueOf(e.getCustomException(jobId)));
            return talend.generateFailResponse(jobId, e);
        }

        try {
            jobservice.checkForStatusError(jobId, JOBKeywords.ADD_HOOK, config.get("jobName"),
                    config.get("jobName") + " Failed JobId : " + jobId);
        } catch (TalendException e) {
            jobservice.failedJOB(jobId, JOBKeywords.ADD_HOOK, String.valueOf(e.getRowOuptutException(jobId)));
            return talend.generateFailCountResponse(jobId, e);
        }
        jobservice.stopJOB(jobId, JOBKeywords.ADD_HOOK);
        return talend.getSucessResponse(jobId);
    }

    @PostMapping("/callCSAPI")
    public ResponseEntity<Object> callCSAPI(@RequestBody Map<String, String> request) {

        String url = ConfigurationUtility.getEnvConfigModel().getSigmaCatalogServicesApiURL();
        if (!StringUtility.isEmpty(request.get("url"))) {
            url = request.get("url");
        }
        String sixthUrlString = url + request.get("basepath");
        HttpHeaders headers = new HttpHeaders();
        headers.add(CatalogConstants.ACCEPT, CatalogConstants.APPLICATION_XML);
        headers.add(CatalogConstants.CONTENT_TYPE, CatalogConstants.APPLICATION_XML);
        HttpEntity<String> requestTemp = new HttpEntity<>(headers);
        RestTemplate rs;
        if (StringUtility.equalsAnyIgnoreCase("true", request.get("isSSL"))) {
            rs = JKSFileLoad.getNonSSLTemplate();
        } else {
            rs = JKSFileLoad.getRestTemplate();
        }
        byte[] createEntityResponse = CatalogCommunicator.getAPIResponseByte(rs,
                sixthUrlString,
                HttpMethod.GET, requestTemp);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", sixthUrlString);
        map.put("body", new String(createEntityResponse, StandardCharsets.UTF_8));
        return new ResponseEntity<Object>(map, HttpStatus.OK);
    }

}
