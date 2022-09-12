package com.sigma.catalog.api.hubservice.endpoints;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JOB;
import com.sigma.catalog.api.hubservice.repository.JOBRepository;
import com.sigma.catalog.api.hubservice.services.BundleService;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/jobs")
public class RatingPlanShells {

        @Autowired
        private TalendHelperService talend;

        @Autowired
        private JOBRepository jobtable;

        @Autowired
        private BundleService bundleService;

        private static final Logger LOG = LoggerFactory.getLogger(RatingPlanShells.class);

        @PostMapping("/Bundle/upload")
        public ResponseEntity<Object> BundleUploadFile(
                        @RequestParam(value = "JOB_ID", required = false) String jobId,
                        @RequestParam(value = "BundleUploadFile", required = true) MultipartFile BundleUploadFile) {
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }
                ResponseEntity<Object> resp =  bundleService.process(jobId, BundleUploadFile);
                bundleService.processAsync(jobId);
                return resp;

        }

        @PostMapping("/Component/upload")
        public ResponseEntity<Object> ComponentUploadFile(
                        @RequestParam(value = "JOB_ID", required = false) String jobId,
                        @RequestParam(value = "ComponentUploadFile", required = true) MultipartFile ComponentUploadFile) {
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }
                LOG.info("JOB_ID recrvied " + jobId);
                String fileName = "ComponentUploadFile_" + jobId + ".csv";

                talend.writeFile(ComponentUploadFile, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileName);

                jobtable.save(new JOB(jobId, JOBKeywords.START, JOBKeywords.COMPONENT,
                                JOBKeywords.TASK_SUCCESS,
                                ComponentUploadFile.getOriginalFilename() + " :: " + fileName));

                return talend.generateSuccessResponse(jobId);
        }

        @PostMapping("/RatingPlanDetail/upload")
        public ResponseEntity<Object> RatingPlanDetailUploadFile(
                        @RequestParam(value = "JOB_ID", required = false) String jobId,
                        @RequestParam(value = "RatingPlanDetailUploadFile", required = true) MultipartFile RatingPlanDetailUploadFile) {
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }
                LOG.info("JOB_ID recrvied " + jobId);
                String fileName = "RatingPlanDetailUploadFile_" + jobId + ".csv";

                talend.writeFile(RatingPlanDetailUploadFile, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileName);

                jobtable.save(new JOB(jobId, JOBKeywords.START, JOBKeywords.RATEPLANDETAIL,
                                JOBKeywords.TASK_SUCCESS,
                                RatingPlanDetailUploadFile.getOriginalFilename() + " :: " + fileName));

                return talend.generateSuccessResponse(jobId);
        }

        @PostMapping("/RatingPlanRate/upload")
        public ResponseEntity<Object> RatingPlanRateUploadFile(
                        @RequestParam(value = "JOB_ID", required = false) String jobId,
                        @RequestParam(value = "RatingPlanRateUploadFile", required = true) MultipartFile RatingPlanRateUploadFile) {
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }
                LOG.info("JOB_ID recrvied " + jobId);
                String fileName = "RatingPlanRateUploadFile_" + jobId + ".csv";

                talend.writeFile(RatingPlanRateUploadFile, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileName);

                jobtable.save(new JOB(jobId, JOBKeywords.START, JOBKeywords.RATEPLANRATE,
                                JOBKeywords.TASK_SUCCESS,
                                RatingPlanRateUploadFile.getOriginalFilename() + " :: " + fileName));

                return talend.generateSuccessResponse(jobId);
        }

}
