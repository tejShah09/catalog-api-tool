package com.sigma.catalog.api.hubservice.endpoints;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.repository.QueryRepository;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/uijobs")
public class ToolUI {

    @Autowired
    public QueryRepository tables;

    @CrossOrigin
    @GetMapping("/getAllJob")
    public ResponseEntity<Object> getAllJobLists() {

        String result = tables.query("select getAllJobs()");

        return new ResponseEntity<Object>(result, HttpStatus.OK);

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

        String query = "select * from jobs where job_id=''" + jobId + "'' order by done_at desc";
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


            query =query+" union all "+ "select ''" + "ProductComponent"
            + "_Rates'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Parent_Entity_ClassType\" as \"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
            + jobId + "_" + "ProductComponent" + "_Rates" + "\"";

            query =query+" union all "+ "select ''" + "ProductComponent"
            + "_Associations'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
            + jobId + "_" + "ProductComponent" + "_Associations" + "\"";
        }else if (StringUtility.equalsIgnoreCase(category, JOBKeywords.RATEPLANRATE)) {
            query = "select ''" + "RatePlanDetail"
            + "_Entity'' as \"type\",\"id\",\"PublicID\",\"Class_Type\",\"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
            + jobId + "_" + "RatePlanDetail" + "_Entity" + "\"";         

            query =query+" union all "+ "select ''" + "RatePlanRate"
            + "_Associations'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
            + jobId + "_" + "RatePlanRate" + "_Associations" + "\"";

            query =query+" union all "+ "select ''" + "RatePlanRate"
            + "_Rates'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Parent_Entity_ClassType\" as \"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
            + jobId + "_" + "RatePlanRate" + "_Rates" + "\"";
        }else if (StringUtility.equalsIgnoreCase(category, JOBKeywords.IMPORTPRICECHANGE)) {
                 

          query ="select ''" + "ProductComponent"
            + "_Rates'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Parent_Entity_ClassType\" as \"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
            + jobId + "_" + "ProductComponent" + "_Rates" + "\"";

            query =query+" union all "+ "select ''" + "HUBRatePlanRate"
            + "_Rates'' as \"type\",\"id\",\"Parent_Entity_GUID\" as \"PublicID\",\"Parent_Entity_ClassType\" as \"Class_Type\",\"Parent_Entity_Name\" as \"Name\",\"status\",\"status_reason\",\"response\",\"payloads\",\"EntityJson\" from \""
            + jobId + "_" + "HUBRatePlanRate" + "_Rates" + "\"";
        }

        String result = tables.query("select executeQuery('" + query + "')");

        return new ResponseEntity<Object>(result, HttpStatus.OK);

    }

}
