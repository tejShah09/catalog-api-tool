package com.sigma.catalog.api.talendService.Endpoints;

import java.nio.file.Paths;
import java.util.HashMap;

import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/jobs/rateRecall")
public class RateRecall {
    @Autowired
    private TalendHelperService talend;
    private static final Logger LOG = LoggerFactory.getLogger(BCRRService.class);

    @PostMapping("/LoadInputSheet")
    public ResponseEntity<Object> stage(
            @RequestParam(value = "recallSheet", required = true) MultipartFile recalSheet,
            @RequestParam(value = "JOB_ID", required = false) String jobId) throws TalendException {

   

            String oringalFile = recalSheet.getOriginalFilename();
            
            String fileNAme = "RateRecall_"+  jobId+"_"+oringalFile;

            talend.writeFile(recalSheet, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme );
          

       

            HashMap<String, String> config = new HashMap<>();
            config.put("sheetName", fileNAme);
            String respose = talend.executeJob(jobId, "LoadRateRecallSheet", config);
            
    

        return new ResponseEntity<Object>(respose, HttpStatus.OK);

    }
}


