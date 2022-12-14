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

import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.services.JobService;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/uploadUtility")
public class UplodUtility {

    @Autowired
    private TalendHelperService talend;

    @Autowired
    private JobService jobservice;

    @PostMapping("/sendReconciliation")
    public ResponseEntity<Object> sendReconciliation(
            @RequestBody Map<String, String> request) {

        String jobId = talend.generateUniqJobId();
        if (!StringUtility.isEmpty(request.get("jobId"))) {
            jobId = request.get("jobId");
        }
        JobProperites properties = new JobProperites(jobId);
        String jobCategory = "";

        try {

           
            String targetJobId;
            if (!StringUtility.isEmpty(request.get("targetJobId"))) {
                targetJobId = request.get("targetJobId");
            } else {
                return new ResponseEntity<Object>("targetJobId_not_found", HttpStatus.BAD_REQUEST);
            }

            if (!StringUtility.isEmpty(request.get("jobCategory"))) {
                jobCategory = request.get("jobCategory");
            } else {
                return new ResponseEntity<Object>("jobCategory_not_found", HttpStatus.BAD_REQUEST);
            }

            String inputTable = targetJobId + "_" + jobCategory + "_Entity";
            if (!StringUtility.isEmpty(request.get("inputTable"))) {
                inputTable = request.get("inputTable");
            }

            jobservice.sendEntityReportToHUB(properties, jobCategory, inputTable,
                    properties.jobId + "_" + jobCategory + "_Reconciliation");
            HashMap<String, Object> ouptut = new HashMap<String, Object>();
            ouptut.put("jobId", jobId);
            jobservice.stopJOB(properties.jobId, jobCategory);
            return new ResponseEntity<Object>(ouptut, HttpStatus.OK);

        } catch (Exception e) {
            jobservice.failedJOB(properties.jobId, jobCategory, null);
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/ChangeStatus")
    public ResponseEntity<Object> ChangeStatus(
            @RequestBody Map<String, String> request) {
        String jobId = talend.generateUniqJobId();
        if (!StringUtility.isEmpty(request.get("jobId"))) {
            jobId = request.get("jobId");
        }
        JobProperites properties = new JobProperites(jobId);
        String jobCategory = "";
        try {

            String targetJobId;
            if (!StringUtility.isEmpty(request.get("targetJobId"))) {
                targetJobId = request.get("targetJobId");
            } else {
                return new ResponseEntity<Object>("targetJobId_not_found", HttpStatus.BAD_REQUEST);
            }

            if (!StringUtility.isEmpty(request.get("jobCategory"))) {
                jobCategory = request.get("jobCategory");
            } else {
                return new ResponseEntity<Object>("jobCategory_not_found", HttpStatus.BAD_REQUEST);
            }

            String targetStatus = "";
            if (!StringUtility.isEmpty(request.get("targetStatus"))) {
                targetStatus = request.get("targetStatus");
            } else {
                return new ResponseEntity<Object>("targetStatusy_not_found", HttpStatus.BAD_REQUEST);
            }

            String transactionId = "NA";
            if (!StringUtility.isEmpty(request.get("transactionId"))) {
                transactionId = request.get("transactionId");
            } 

            jobservice.changeWorkFlowWith_103Retry(properties, targetJobId + "_" + jobCategory + "_Entity",
                    properties.jobId + "_" + jobCategory + "_Report",
                    properties.jobId + "_" + jobCategory + "_Entity_Status", targetStatus, jobCategory,transactionId);
            jobservice.stopJOB(properties.jobId, jobCategory);
            return talend.getSucessResponse(jobId);

        } catch (TalendException e) {
            jobservice.failedJOB(properties.jobId, jobCategory, null);
            return talend.generateFailCountResponse(jobId, e);
        } catch (Exception e) {
            jobservice.failedJOB(properties.jobId, jobCategory, null);
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/LiveWithStatusCheck")
    public ResponseEntity<Object> LiveWithStatusCheck(
            @RequestBody Map<String, String> request) {
        String jobId = talend.generateUniqJobId();
        if (!StringUtility.isEmpty(request.get("jobId"))) {
            jobId = request.get("jobId");
        }
        JobProperites properties = new JobProperites(jobId);
        String jobCategory = "";
        try {

            String targetJobId;
            if (!StringUtility.isEmpty(request.get("targetJobId"))) {
                targetJobId = request.get("targetJobId");
            } else {
                return new ResponseEntity<Object>("targetJobId_not_found", HttpStatus.BAD_REQUEST);
            }

            if (!StringUtility.isEmpty(request.get("jobCategory"))) {
                jobCategory = request.get("jobCategory");
            } else {
                return new ResponseEntity<Object>("jobCategory_not_found", HttpStatus.BAD_REQUEST);
            }
            String transactionId = "NA";
            if (!StringUtility.isEmpty(request.get("transactionId"))) {
                transactionId = request.get("transactionId");
            } 
            jobservice.startJOB(properties.jobId, jobCategory);
            jobservice.liveEntityAndWaitToComplete(properties, targetJobId, jobCategory,transactionId);
            jobservice.stopJOB(properties.jobId, jobCategory);
            return talend.getSucessResponse(jobId);

        } catch (TalendException e) {
            jobservice.failedJOB(properties.jobId, jobCategory, null);
            return talend.generateFailCountResponse(jobId, e);
        } catch (Exception e) {
            jobservice.failedJOB(properties.jobId, jobCategory, null);
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
