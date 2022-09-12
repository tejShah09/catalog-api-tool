package com.sigma.catalog.api.talendService.Endpoints;

import java.nio.file.Paths;
import java.util.HashMap;

import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.talendService.model.JobRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/jobs/bcrr")
public class BCRRService {

        @Autowired
        private TalendHelperService talend;
        private static final Logger LOG = LoggerFactory.getLogger(BCRRService.class);

        @PostMapping("/upload")
        public ResponseEntity<Object> uploadFile(
                        @RequestParam(value = "JOB_ID", required = false) String jobId,
                        @RequestParam(value = "BundleUploadFile", required = true) MultipartFile BundleUploadFile,
                        @RequestParam(value = "ComponentUploadFile", required = true) MultipartFile ComponentUploadFile,
                        @RequestParam(value = "RatingPlan_Rates", required = true) MultipartFile RatingPlan_Rates,
                        @RequestParam(value = "RatingPlan_Details", required = true) MultipartFile RatingPlan_Details,
                        @RequestParam(value = "ImportPriceChange", required = false) MultipartFile ImportPriceChange) throws TalendException{
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }
                LOG.info("JOB_ID recrvied " + jobId);
                String message = "Uploaded the file(s) successfully ";

                talend.writeFile(BundleUploadFile, Paths.get(TalendConstants.INPUT_FILE_LOCATION),
                                "BundleUploadFile_" + jobId + ".xlsx");
                talend.writeFile(ComponentUploadFile, Paths.get(TalendConstants.INPUT_FILE_LOCATION),
                                "ComponentUploadFile_" + jobId + ".xlsx");
                talend.writeFile(RatingPlan_Rates, Paths.get(TalendConstants.INPUT_FILE_LOCATION),
                                "RatingPlan_Rates_" + jobId + ".xlsx");
                talend.writeFile(RatingPlan_Details, Paths.get(TalendConstants.INPUT_FILE_LOCATION),
                                "RatingPlan_Details_" + jobId + ".xlsx");
                talend.writeFile(ImportPriceChange, Paths.get(TalendConstants.INPUT_FILE_LOCATION),
                                "ImportPriceChange_" + jobId + ".xlsx");

                HashMap<String, String> config = new HashMap<>();
                config.put("sheetInputPostFix", "_" + jobId);

                message = talend.executeJob(jobId, "ReadBCRRSheet", config);

                return talend.generateResponse(jobId, message);
        }

        @PostMapping("/RatePlanDetail/CreateInput")
        public ResponseEntity<Object> createInput(@RequestBody JobRequest request) throws TalendException{
                String message = "CAPI input is created  successfully";
                String jobId = request.getJobId();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                HashMap<String, String> config = new HashMap<>();
                config.put("RatePlanDetailInput", request.getInputSheetJob() + "_RatePlanDetail_InputSheet");
                config.put("RatePlanRateInput", request.getInputSheetJob() + "_RatePlanRate_InputSheet");
                message = talend.executeJob(jobId, "RatingPlanDetails", config);

                return talend.generateResponse(jobId, message);
        }

        @PostMapping("/RatePlanDetail/CreateEntity")
        public ResponseEntity<Object> CreateEntity(@RequestBody JobRequest request)throws TalendException {
                String message = "created entity successfully ";
                String jobId = request.getJobId();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                HashMap<String, String> config = new HashMap<>();
                config.put("group", "RatePlanDetail");

                message = talend.executeAsyncJob(request.getInputSheetJob(), "CAPICreateEntities", config);

                return talend.generateResponse(jobId, message);
        }

        @PostMapping("/RatePlanDetail/CreateAssoc")
        public ResponseEntity<Object> CreateAssoc(@RequestBody JobRequest request)throws TalendException {
                String message = "created assoc successfully ";
                String jobId = request.getJobId();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                HashMap<String, String> config = new HashMap<>();
                config.put("group", "RatePlanDetail");

                message = talend.executeAsyncJob(request.getInputSheetJob(), "CAPICreateAssoc", config);

                return talend.generateResponse(jobId, message);
        }

        @PostMapping("/RatePlanRate/CreateInput")
        public ResponseEntity<Object> RatePlanRate(@RequestBody JobRequest request)throws TalendException {
                String message = "CAPI input is created  successfully";
                String jobId = request.getJobId();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                HashMap<String, String> config = new HashMap<>();
                config.put("RatePlanRateInput", request.getInputSheetJob() + "_RatePlanRate_InputSheet");
                message = talend.executeJob(jobId, "RatingPlanRates", config);

                return talend.generateResponse(jobId, message);
        }

        
        @PostMapping("/RatePlanRate/Standalone")
        public ResponseEntity<Object> RatePlanRate(  
                @RequestParam(value = "JOB_ID", required = false) String jobId,
                @RequestParam(value = "RatingPlan_Rates", required = true) MultipartFile RatingPlan_Rates) throws TalendException{
                String message = "CAPI input is created  successfully";
         

                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                String fileNAme = "RatingPlan_Rates_" + jobId+ ".xlsx";
                talend.writeFile(RatingPlan_Rates, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme );
                HashMap<String, String> config = new HashMap<>();
                config.put("sheetName", fileNAme);
          
             
                 message = talend.executeJob(jobId, "RatingPlanRateStandAlone", config);
        
                return talend.generateResponse(jobId, message);

        }


        
        @PostMapping("/RatePlanRate/CreateRate")
        public ResponseEntity<Object> CreateRate(@RequestBody JobRequest request)throws TalendException {
                String message = "CAPI input is created  successfully";
                String jobId = request.getJobId();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                HashMap<String, String> config = new HashMap<>();
                config.put("group", "RatePlanRate");      
                message = talend.executeAsyncJob(request.getInputSheetJob() , "CAPICreateRates", config);

                return talend.generateResponse(jobId, message);
        }


        @PostMapping("/Bundle/CreateInput")
        public ResponseEntity<Object> bundleCreateInput(@RequestBody JobRequest request) throws TalendException{
                String message = "CAPI input is created  successfully";
                String jobId = request.getJobId();
                String intent = request.getIntent();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }
                if (StringUtility.isEmpty(intent)) {
                        intent = "New";
                }
                HashMap<String, String> config = new HashMap<>();
                config.put("BundleInput", request.getInputSheetJob() + "_Bundle_InputSheet");
                config.put("ComponentInput", request.getInputSheetJob() + "_Component_InputSheet");
                config.put("Intent", intent);
                message = talend.executeJob(jobId, "Bundle", config);

                return talend.generateResponse(jobId, message);
        }

        
        @PostMapping("/Bundle/CreateEntity")
        public ResponseEntity<Object> BundleCreatEntity(@RequestBody JobRequest request)throws TalendException {
                String message = "created entity successfully ";
                String jobId = request.getJobId();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                HashMap<String, String> config = new HashMap<>();
                config.put("group", "Bundle");
                message = talend.executeAsyncJob(request.getInputSheetJob(), "CAPICreateEntities", config);

                return talend.generateResponse(jobId, message);
        }

        
  

        @PostMapping("/Component/CreateInput")
        public ResponseEntity<Object> componentCreateInput(@RequestBody JobRequest request)throws TalendException {
                String message = "CAPI input is created  successfully";
                String jobId = request.getJobId();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                HashMap<String, String> config = new HashMap<>();             
                config.put("ComponentInput", request.getInputSheetJob() + "_Component_InputSheet");
                message = talend.executeJob(jobId, "Component", config);

                return talend.generateResponse(jobId, message);
        }

        @PostMapping("/Component/CreateAssoc")
        public ResponseEntity<Object> componentCreateEntity(@RequestBody JobRequest request) throws TalendException{
                String message = "created assoc successfully ";
                String jobId = request.getJobId();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                HashMap<String, String> config = new HashMap<>();
                config.put("group", "ProductComponent");

                message = talend.executeAsyncJob(request.getInputSheetJob(), "CAPICreateAssoc", config);

                return talend.generateResponse(jobId, message);
        }

        @PostMapping("/Component/CreateRate")
        public ResponseEntity<Object> ComponentCreateRate(@RequestBody JobRequest request) throws TalendException{
                String message = "CAPI input is created  successfully";
                String jobId = request.getJobId();
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                HashMap<String, String> config = new HashMap<>();
                config.put("group", "ProductComponent");      
                message = talend.executeAsyncJob(request.getInputSheetJob() , "CAPICreateRates", config);

                return talend.generateResponse(jobId, message);
        }
}
