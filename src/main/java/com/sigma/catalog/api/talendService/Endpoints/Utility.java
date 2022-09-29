package com.sigma.catalog.api.talendService.Endpoints;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/jobs/utility")
public class Utility {

    @Autowired
    private TalendHelperService talend;

    @GetMapping("/status")
    public ResponseEntity<Object> status() {

        String message = "Connected ";
        String jobId = talend.generateUniqJobId();
        message = "Live";

        return talend.generateResponse(jobId, message);
    }

    
    @GetMapping("/TestAllConnection")
    public ResponseEntity<Object> TestAllConnection() throws TalendException {

        String message = "Connected ";
        String jobId = talend.generateUniqJobId();
        message = talend.executeJob(jobId, "TestAllConnection", null);

        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/rateXMLtoTable")
    public ResponseEntity<Object> rateXMLtoTable   (
            @RequestParam(value = "RatePlanDetail", required = true) MultipartFile RatePlanDetail) throws TalendException{

        String message = "Connected ";

        String jobId = talend.generateUniqJobId();
        String fileName = RatePlanDetail.getOriginalFilename() + "_" + jobId;
        HashMap<String, String> config = new HashMap<>();
        config.put("fileName", fileName);
        config.put("location", Paths.get(TalendConstants.INPUT_FILE_LOCATION).toString());
        talend.writeFile(RatePlanDetail, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileName + ".xml");
        // message = talend.executeJob(jobId, .class,config);

        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/IntialDataSetup")
    public ResponseEntity<Object> creteInitData()  throws TalendException {
        String message = "Connected ";
        String jobId = talend.generateUniqJobId();
        HashMap<String, String> config = new HashMap<>();
        config.put("InputFile", TalendConstants.CONFIG_FILE_LOCATION+"initialData.json");
        message = talend.executeJob(jobId, "IntialDataSetup", config);
        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/MetaSetup")
    public ResponseEntity<Object> MetaSetup()  throws TalendException {
        String message = "Connected ";
        String jobId = talend.generateUniqJobId();
        message = talend.executeJob(jobId, "MetaDataSetup", null);
        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/findGuid")
    public ResponseEntity<Object> aprove(@RequestBody Map<String, String> request) throws TalendException  {

        String message = "FindGuids ";
        String jobId = talend.generateUniqJobId();
        HashMap<String, String> config = new HashMap<>();
        config.put("tempalteName", request.get("tempalteName"));
        config.put("entityNames", request.get("entityNames"));
        config.put("outputTable", request.get("outputTable"));
        message = talend.executeJob(jobId, "FindGuids", config);
        return talend.generateResponse(jobId, message);

    }

    @PostMapping("/ExecuteJob")
    public ResponseEntity<Object> ExecuteJob(@RequestBody Map<String, String> request)  throws TalendException {

        String message = "FindGuids ";
        String jobId = talend.generateUniqJobId();
        HashMap<String, String> config = new HashMap<String, String>(request);
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        if (StringUtility.equals(config.get("AsyncJob"), "true")) {
            message = talend.executeAsyncJob(jobId, config.get("jobName"), config);
        } else {
            message = talend.executeJob(jobId, config.get("jobName"), config);
        }

        return talend.generateResponse(jobId, message);

    }

}
