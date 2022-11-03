package com.sigma.catalog.api.hubservice.endpoints;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.repository.QueryRepository;
import com.sigma.catalog.api.hubservice.services.JobService;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/uijobs")
public class ToolUI {

    @Autowired
    public QueryRepository tables;
    @Autowired
    private JobService jobservice;

    @Autowired
    private TalendHelperService talend;

    @CrossOrigin
    @GetMapping("/getAllJob")
    public ResponseEntity<Object> getAllJobLists() {

        String result = tables.query("select getAllJobs()");

        return new ResponseEntity<Object>(result, HttpStatus.OK);

    }

    @CrossOrigin
    @PostMapping("/createCorrection")
    public ResponseEntity<Object> createCorrection(
            @RequestBody Map<String, String> request) {
        String jobId = talend.generateUniqJobId();
        String targetJobId;
        JobProperites properties = new JobProperites(jobId);

        if (!StringUtility.isEmpty(request.get("targetJobId"))) {
            targetJobId = request.get("targetJobId");
        } else {
            return new ResponseEntity<Object>("targetJobId_not_found", HttpStatus.BAD_REQUEST);
        }

        String categoryQuery = "select job_category from jobs where job_id='" + targetJobId + "' order by id limit 1";
        String jobCategory = tables.query(categoryQuery);
        String createTableQuery = "CREATE TABLE \"" + jobId + "_" + jobCategory + "_Entity\" AS   SELECT *  FROM \""
                + targetJobId + "_" + jobCategory + "_Entity\"";
        tables.query(createTableQuery);
        jobservice.startJOB(properties.jobId, jobCategory, targetJobId);
        try {
            jobservice.createEntityReport(properties, jobId + "_" + jobCategory + "_Entity",
                    jobId + "_" + jobCategory + "_Report");
        } catch (Exception e) {
            System.out.println("unable to create rport " + targetJobId);
        }

        HashMap<String, Object> ouptut = new HashMap<String, Object>();
        ouptut.put("jobId", jobId);
        ouptut.put("targetJobId", targetJobId);
        ouptut.put("jobCategory", jobCategory);
        return new ResponseEntity<Object>(ouptut, HttpStatus.OK);

    }

    @CrossOrigin
    @PostMapping("/getJob")
    public ResponseEntity<Object> getJob(
            @RequestBody Map<String, String> request) {
        String jobId;
        if (!StringUtility.isEmpty(request.get("jobId"))) {
            jobId = request.get("jobId");
        } else {
            return new ResponseEntity<Object>("jobId_not_found", HttpStatus.BAD_REQUEST);
        }

        String query = "select * from jobs where job_id=''" + jobId + "'' order by id desc";
        String result = tables.query("select executeQuery('" + query + "')");

        return new ResponseEntity<Object>(result, HttpStatus.OK);

    }

    @CrossOrigin
    @PostMapping("/getEntityReport")
    public ResponseEntity<Object> getEntityReport(
            @RequestBody Map<String, String> request) {
        String jobId;
        if (!StringUtility.isEmpty(request.get("jobId"))) {
            jobId = request.get("jobId");
        } else {
            return new ResponseEntity<Object>("jobId_not_found", HttpStatus.BAD_REQUEST);
        }

        String categoryQuery = "select job_category from jobs where job_id='" + jobId + "' order by id limit 1";
        String category = tables.query(categoryQuery);

        String query = "select \"catalogGuid\",\"entityType\",\"entityName\",\"hubid\",\"status\",\"NonLiveGuid\" from \""
                + jobId + "_" + category + "_Report" + "\"";
        String result = tables.query("select executeQuery('" + query + "')");

        return new ResponseEntity<Object>(result, HttpStatus.OK);

    }

    @CrossOrigin
    @PostMapping("/getEntityStatusReport")
    public ResponseEntity<Object> getEntityStatusReport(
            @RequestBody Map<String, String> request) {
        String jobId;
        if (!StringUtility.isEmpty(request.get("jobId"))) {
            jobId = request.get("jobId");
        } else {
            return new ResponseEntity<Object>("jobId_not_found", HttpStatus.BAD_REQUEST);
        }
        String jobCategory;
        if (!StringUtility.isEmpty(request.get("jobCategory"))) {
            jobCategory = request.get("jobCategory");
        } else {
            return new ResponseEntity<Object>("jobCategory_not_found", HttpStatus.BAD_REQUEST);
        }
        String transactionId = "";
        if (!StringUtility.isEmpty(request.get("transactionId"))) {
            transactionId = request.get("transactionId");
        } else {
            return new ResponseEntity<Object>("transactionId_not_found", HttpStatus.BAD_REQUEST);
        }

        String query = "select *  from  \"" + jobId + "_" + jobCategory
                + "_Entity_Status\"  where \"transactionId\"=''" + transactionId + "''";
        String result = tables.query("select executeQuery('" + query + "')");
        if (result == null) {
            result = "[]";
        }
        return new ResponseEntity<Object>(result, HttpStatus.OK);

    }

    @CrossOrigin
    @PostMapping("/getPayloadReport")
    public ResponseEntity<Object> getPayloadReport(
            @RequestBody Map<String, String> request) {
        String jobId;
        if (!StringUtility.isEmpty(request.get("jobId"))) {
            jobId = request.get("jobId");
        } else {
            return new ResponseEntity<Object>("jobId_not_found", HttpStatus.BAD_REQUEST);
        }

        String categoryQuery = "select job_category from jobs where job_id='" + jobId + "' order by id limit 1";
        String category = tables.query(categoryQuery);
        String query = "";
        if (StringUtility.equalsIgnoreCase(category, JOBKeywords.BUNDLE)
                || StringUtility.equalsIgnoreCase(category, JOBKeywords.RATEPLANDETAIL)) {
            query = "select ''" + category
                    + "_Entity'' as \"type\",\"id\",\"PublicID\",\"Class_Type\",\"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
                    + jobId + "_" + category + "_Entity" + "\"";
        } else if (StringUtility.equalsIgnoreCase(category, JOBKeywords.COMPONENT)) {
            query = "select ''" + "Bundle"
                    + "_Entity'' as \"type\",\"id\",\"PublicID\",\"Class_Type\",\"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
                    + jobId + "_" + "Bundle" + "_Entity" + "\"";

            query = query + " union all " + "select ''" + "ProductComponent"
                    + "_Rates'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Parent_Entity_ClassType\" as \"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
                    + jobId + "_" + "ProductComponent" + "_Rates" + "\"";

            query = query + " union all " + "select ''" + "ProductComponent"
                    + "_Associations'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
                    + jobId + "_" + "ProductComponent" + "_Associations" + "\"";
        } else if (StringUtility.equalsIgnoreCase(category, JOBKeywords.RATEPLANRATE)) {
            query = "select ''" + "RatePlanDetail"
                    + "_Entity'' as \"type\",\"id\",\"PublicID\",\"Class_Type\",\"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
                    + jobId + "_" + "RatePlanDetail" + "_Entity" + "\"";

            query = query + " union all " + "select ''" + "RatePlanRate"
                    + "_Associations'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
                    + jobId + "_" + "RatePlanRate" + "_Associations" + "\"";

            query = query + " union all " + "select ''" + "RatePlanRate"
                    + "_Rates'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Parent_Entity_ClassType\" as \"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
                    + jobId + "_" + "RatePlanRate" + "_Rates" + "\"";
        } else if (StringUtility.equalsIgnoreCase(category, JOBKeywords.IMPORTPRICECHANGE)) {

            query = "select ''" + "ProductComponent"
                    + "_Rates'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Parent_Entity_ClassType\" as \"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
                    + jobId + "_" + "ProductComponent" + "_Rates" + "\"";

            query = query + " union all " + "select ''" + "HUBRatePlanRate"
                    + "_Rates'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Parent_Entity_ClassType\" as \"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
                    + jobId + "_" + "HUBRatePlanRate" + "_Rates" + "\"";
        }

        String result = tables.query("select executeQuery('" + query + "')");

        return new ResponseEntity<Object>(result, HttpStatus.OK);

    }

    @CrossOrigin
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
            jobservice.startTrascation(properties.jobId, jobCategory, transactionId);
            jobservice.changeWorkFlowAsync(properties, targetJobId + "_" + jobCategory + "_Entity",
                    properties.jobId + "_" + jobCategory + "_Report",
                    properties.jobId + "_" + jobCategory + "_Entity_Status", targetStatus, jobCategory, transactionId);

            return talend.getSucessResponse(jobId);

        } catch (TalendException e) {
            jobservice.failedJOB(properties.jobId, jobCategory, null);
            return talend.generateFailCountResponse(jobId, e);
        } catch (Exception e) {
            jobservice.failedJOB(properties.jobId, jobCategory, null);
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin
    @PostMapping("/checkStatus")
    public ResponseEntity<Object> checkStatus(
            @RequestBody Map<String, String> request) {
        String jobId = talend.generateUniqJobId();
        if (!StringUtility.isEmpty(request.get("jobId"))) {
            jobId = request.get("jobId");
        }
        JobProperites properties = new JobProperites(jobId);
        String jobCategory = "";

        if (!StringUtility.isEmpty(request.get("jobCategory"))) {
            jobCategory = request.get("jobCategory");
        } else {
            return new ResponseEntity<Object>("jobCategory_not_found", HttpStatus.BAD_REQUEST);
        }

        String transactionId = "";
        if (!StringUtility.isEmpty(request.get("transactionId"))) {
            transactionId = request.get("transactionId");
        } else {
            return new ResponseEntity<Object>("transactionId_not_found", HttpStatus.BAD_REQUEST);
        }

        int totalRows = 0;
        int proccessed = 0;
        int success = 0;
        int failed = 0;
        String result = tables
                .query("select CAST(count(*) as TEXT)  from  \"" + jobId + "_" + jobCategory + "_Report\"");
        try {
            totalRows = Integer.parseInt(result);
        } catch (NumberFormatException e) {

        }

        result = tables.query("select CAST(count(*) as TEXT)  from  \"" + jobId + "_" + jobCategory
                + "_Entity_Status\"  where \"transactionId\"='" + transactionId + "'");
        try {
            proccessed = Integer.parseInt(result);
        } catch (NumberFormatException e) {

        }

        result = tables.query("select CAST(count(*) as TEXT)  from  \"" + jobId + "_" + jobCategory
                + "_Entity_Status\"  where \"transactionId\"='" + transactionId
                + "' and status_reason  like '%success%'");
        try {
            success = Integer.parseInt(result);
        } catch (NumberFormatException e) {

        }

        if (proccessed <= success) {
            failed = 0;
        } else {
            failed = proccessed - success;
        }

        Map count = new HashMap<String, Integer>();

        count.put("totalRows", totalRows);
        count.put("proccessed", proccessed);
        count.put("success", success);
        count.put("failed", failed);
        return new ResponseEntity<Object>(count, HttpStatus.OK);
    }

    @CrossOrigin
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
            jobservice.startTrascation(targetJobId, jobCategory, "reconciliation");
            jobservice.sendEntityReportToHUB(properties, jobCategory, inputTable,
                    properties.jobId + "_" + jobCategory + "_Reconciliation");
            HashMap<String, Object> ouptut = new HashMap<String, Object>();
            ouptut.put("jobId", jobId);
            return new ResponseEntity<Object>(ouptut, HttpStatus.OK);

        } catch (Exception e) {
            jobservice.failedJOB(properties.jobId, jobCategory, null);
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin
    @PostMapping("/executeQuery")
    public ResponseEntity<Object> executeQuery(@RequestBody Map<String, String> request) {
        String query;
        if (!StringUtility.isEmpty(request.get("query"))) {
            query = request.get("query");
        } else {
            return new ResponseEntity<Object>("query_not_found", HttpStatus.BAD_REQUEST);
        }

        query = StringUtility.replace(query, "'", "''");
        String result = tables.query("select executeQuery('" + query + "')");

        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping("/Live")
    public ResponseEntity<Object> Live(
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

            if (!StringUtility.isEmpty(request.get("changeStrategy"))) {
                properties.setChangeStretegy(request.get("changeStrategy"));
            }
            jobservice.startTrascation(properties.jobId, jobCategory, transactionId);
            jobservice.LiveAsync(properties, targetJobId, jobCategory, transactionId);

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
