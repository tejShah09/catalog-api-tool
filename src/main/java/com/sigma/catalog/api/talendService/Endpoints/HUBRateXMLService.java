package com.sigma.catalog.api.talendService.Endpoints;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.talendService.model.JobRequest;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/jobs/hubxml/Rate")
public class HUBRateXMLService {
    @Autowired
    private TalendHelperService talend;
    private static final Logger LOG = LoggerFactory.getLogger(BCRRService.class);

    @PostMapping("/LoadIntoStageDB")
    public ResponseEntity<Object> stage(
            @RequestParam(value = "RateXMLs", required = true) MultipartFile[] rateXmls,
            @RequestParam(value = "JOB_ID", required = false) String jobId) throws TalendException{

        List<String> fileNames = new ArrayList<>();
        List<String> responseses = new ArrayList<>();
        Arrays.asList(rateXmls).stream().forEach(file -> {

            String oringalFile = file.getOriginalFilename();
            if (StringUtility.contains(oringalFile, ".xml")) {
                oringalFile = oringalFile.replace(".xml", "");
            }
            String fileNAme = "Rate_" + oringalFile + "_" + jobId;

            talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xml");
            fileNames.add(fileNAme);

        });

        for (int i = 0; i < fileNames.size(); i++) {
            HashMap<String, String> config = new HashMap<>();
            config.put("rateXmlFileName", fileNames.get(i));
            String respose = talend.executeJob(jobId, "LoadHubXmlInputSheet", config);
            responseses.add(respose);
        }

        return new ResponseEntity<Object>(responseses.toString(), HttpStatus.OK);

    }

    @PostMapping("/LoadXmlWithNewProcess")
    public ResponseEntity<Object> LoadXmlWithNewProcess(
            @RequestParam(value = "RateXMLs", required = true) MultipartFile[] rateXmls,
            @RequestParam(value = "JOB_ID", required = false) String jobId) throws TalendException{

        List<String> fileNames = new ArrayList<>();
        List<String> responseses = new ArrayList<>();
        Arrays.asList(rateXmls).stream().forEach(file -> {

            String oringalFile = file.getOriginalFilename();
            if (StringUtility.contains(oringalFile, ".xml")) {
                oringalFile = oringalFile.replace(".xml", "");
            }
            String fileNAme = "Rate_" + oringalFile + "_" + jobId;

            talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xml");
            fileNames.add(fileNAme);

        });

        for (int i = 0; i < fileNames.size(); i++) {
            HashMap<String, String> config = new HashMap<>();
            config.put("rateXmlFileName", fileNames.get(i));
            String respose = talend.executeJob(jobId, "testExcelLoad", config);
            responseses.add(respose);
        }

        return new ResponseEntity<Object>(responseses.toString(), HttpStatus.OK);

    }

    @PostMapping("/LoadProductXML")
    public ResponseEntity<Object> LoadProductXML(
            @RequestParam(value = "RateXMLs", required = true) MultipartFile[] rateXmls,
            @RequestParam(value = "JOB_ID", required = false) String jobId) throws TalendException {

        List<String> fileNames = new ArrayList<>();
        List<String> responseses = new ArrayList<>();
        Arrays.asList(rateXmls).stream().forEach(file -> {

            String oringalFile = file.getOriginalFilename();
            if (StringUtility.contains(oringalFile, ".xml")) {
                oringalFile = oringalFile.replace(".xml", "");
            }
            String fileNAme = "Product_" + oringalFile + "_" + jobId;

            talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xml");
            fileNames.add(fileNAme);

        });

        for (int i = 0; i < fileNames.size(); i++) {
            HashMap<String, String> config = new HashMap<>();
            config.put("rateXmlFileName", fileNames.get(i));
            String respose = talend.executeJob(jobId, "LoadHubProductXmlInputSheet", config);
            responseses.add(respose);
        }

        return new ResponseEntity<Object>(responseses.toString(), HttpStatus.OK);

    }

    @PostMapping("/CreateCAPIRates")
    public ResponseEntity<Object> CreateCAPIRates(@RequestBody Map<String, Object> request)throws TalendException {

        String prefix = (String) request.get("prefix");
        String message = "created entity successfully ";

        String jobId = (String) request.get("extractJobId");
        HashMap<String, String> config = new HashMap<>();
        config.put("jobId", jobId);
        if (StringUtility.isNotEmpty(prefix)) {
            config.put("prefix", prefix);
        }

        message = talend.executeJob(jobId, "ConvertHUBRateToCAPI", config);
        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/CreateProductCAPIRates")
    public ResponseEntity<Object> CreateProductCAPIRates(@RequestBody Map<String, Object> request)throws TalendException {

        String message = "created entity successfully ";

        String jobId = (String) request.get("extractJobId");
        HashMap<String, String> config = new HashMap<>();
        config.put("jobId", jobId);

        message = talend.executeJob(jobId, "ConvertHUBProductRateToCAPI", config);
        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/CreateRate")
    public ResponseEntity<Object> ComponentCreateRate(@RequestBody JobRequest request)throws TalendException {
        String message = "CAPI input is created  successfully";
        String jobId = request.getJobId();
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }

        HashMap<String, String> config = new HashMap<>();
        config.put("group", "HUBRatePlanRate");
        message = talend.executeAsyncJob(request.getInputSheetJob(), "CAPICreateRates", config);

        return talend.generateResponse(jobId, message);
    }

}
