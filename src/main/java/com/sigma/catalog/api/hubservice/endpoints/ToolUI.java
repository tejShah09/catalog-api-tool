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

        String query = "select * from jobs where job_id=''"+jobId+"'' order by done_at desc";
        String result = tables.query("select executeQuery('"+query+"')");

        return new ResponseEntity<Object>(result, HttpStatus.OK);

    }
}
