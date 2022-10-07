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
    public ResponseEntity<Object> sendMail(
            @RequestBody Map<String, String> request) {
        try {
            String jobId = talend.generateUniqJobId();
            if (!StringUtility.isEmpty(request.get("jobId"))) {
                jobId = request.get("jobId");
            }
            String targetJobId;
            if (!StringUtility.isEmpty(request.get("targetJobId"))) {
                targetJobId = request.get("targetJobId");
            } else {
                return new ResponseEntity<Object>("targetJobId_not_found", HttpStatus.BAD_REQUEST);
            }

            String jobCategory = "";
            if (!StringUtility.isEmpty(request.get("jobCategory"))) {
                jobCategory = request.get("jobCategory");
            } else {
                return new ResponseEntity<Object>("jobCategory_not_found", HttpStatus.BAD_REQUEST);
            }

       
            String reportTable = "CAPI_" + jobCategory + "_AllStatus";
            if (!StringUtility.isEmpty(request.get("reportTable"))) {
                reportTable = request.get("reportTable");
            }

            String inputTable = targetJobId + "_" + jobCategory + "_Entity";
            if (!StringUtility.isEmpty(request.get("inputTable"))) {
                reportTable = request.get("inputTable");
            }

            String inputTableNameKey = "Name";
            if (!StringUtility.isEmpty(request.get("inputTableNameKey"))) {
                inputTableNameKey = request.get("inputTableNameKey");
            }

            String inputTableGUIDKey = "PublicID";
            if (!StringUtility.isEmpty(request.get("inputTableNameKey"))) {
                inputTableNameKey = request.get("inputTableNameKey");
            }

            JobProperites properties = new JobProperites(jobId);
            jobservice.createEntityReport(properties, inputTable, inputTableNameKey,
                    inputTableGUIDKey, reportTable, properties.jobId + "_" + jobCategory + "_Reconciliation");
            jobservice.sendEntityReportToHUB(properties, jobCategory,
                    properties.jobId + "_" + jobCategory + "_Reconciliation");
            HashMap<String, Object> ouptut = new HashMap<String, Object>();
            ouptut.put("jobId", jobId);
            return new ResponseEntity<Object>(ouptut, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
