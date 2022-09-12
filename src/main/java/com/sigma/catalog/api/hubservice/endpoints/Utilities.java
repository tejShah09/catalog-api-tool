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
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.repository.JOBRepository;
import com.sigma.catalog.api.hubservice.services.BundleService;
import com.sigma.catalog.api.hubservice.services.EmailService;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/utility")
public class Utilities {
   

    @Autowired
    private TalendHelperService talend;


    @PostMapping("/sendEmail")
    public ResponseEntity<Object> sendMail(
            @RequestBody Map<String, String> request) {
        // emailServer.sendMail();

        return new ResponseEntity<Object>("Sent", HttpStatus.OK);
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
            if (StringUtility.equals(config.get("AsyncJob"), "true")) {
                talend.executeAsyncJob(jobId, config.get("jobName"), config);
            } else {
                talend.executeJob(jobId, config.get("jobName"), config);
            }

        } catch (TalendException e) {
            return talend.generateFailResponse(jobId, e);
        }

        return talend.generateSuccessResponse(jobId);

    }

}
