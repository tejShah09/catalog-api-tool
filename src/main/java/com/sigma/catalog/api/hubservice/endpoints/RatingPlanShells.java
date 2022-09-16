package com.sigma.catalog.api.hubservice.endpoints;

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

import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.repository.JOBRepository;
import com.sigma.catalog.api.hubservice.services.BundleService;
import com.sigma.catalog.api.hubservice.services.ComponentService;
import com.sigma.catalog.api.hubservice.services.RatePlanDetailService;
import com.sigma.catalog.api.hubservice.services.RatePlanRateService;
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

        @Autowired
        private RatePlanDetailService ratePlanDetailService;

        @Autowired
        private RatePlanRateService ratePlanRateService;

        @Autowired
        private ComponentService componentService;

        private static final Logger LOG = LoggerFactory.getLogger(RatingPlanShells.class);

        @PostMapping("/Bundle/upload")
        public ResponseEntity<Object> BundleUploadFile(
                        @RequestParam(value = "JOB_ID", required = false) String jobId,
                        @RequestParam(value = "BundleUploadFile", required = true) MultipartFile BundleUploadFile,
                        @RequestParam(value = "launchEntity", required = false, defaultValue = "true") String launchEntity,
                        @RequestParam(value = "sendReconSheet", required = false, defaultValue = "true") String sendReconSheet,
                        @RequestParam(value = "sendEmail", required = false, defaultValue = "true") String sendEmail,
                        @RequestParam(value = "changeStrategy", required = false, defaultValue = "true") String changeStrategy) {
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy,
                                jobId);

                ResponseEntity<Object> resp = bundleService.process(properties, BundleUploadFile);
                if (resp.getStatusCode() == HttpStatus.OK) {

                        bundleService.processAsync(properties);
                }
                return resp;

        }

        @PostMapping("/Component/upload")
        public ResponseEntity<Object> ComponentUploadFile(
                        @RequestParam(value = "JOB_ID", required = false) String jobId,
                        @RequestParam(value = "ComponentUploadFile", required = true) MultipartFile ComponentUploadFile,
                        @RequestParam(value = "launchEntity", required = false, defaultValue = "true") String launchEntity,
                        @RequestParam(value = "sendReconSheet", required = false, defaultValue = "true") String sendReconSheet,
                        @RequestParam(value = "sendEmail", required = false, defaultValue = "true") String sendEmail,
                        @RequestParam(value = "changeStrategy", required = false, defaultValue = "true") String changeStrategy) {
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy,
                                jobId);
                ResponseEntity<Object> resp = componentService.process(properties, ComponentUploadFile);

                if (resp.getStatusCode() == HttpStatus.OK) {
                        componentService.processAsync(properties);
                }
                return resp;

        }

        @PostMapping("/RatingPlanDetail/upload")
        public ResponseEntity<Object> RatingPlanDetailUploadFile(
                        @RequestParam(value = "JOB_ID", required = false) String jobId,
                        @RequestParam(value = "RatingPlanDetailUploadFile", required = true) MultipartFile RatingPlanDetailUploadFile,
                        @RequestParam(value = "launchEntity", required = false, defaultValue = "true") String launchEntity,
                        @RequestParam(value = "sendReconSheet", required = false, defaultValue = "true") String sendReconSheet,
                        @RequestParam(value = "sendEmail", required = false, defaultValue = "true") String sendEmail,
                        @RequestParam(value = "changeStrategy", required = false, defaultValue = "true") String changeStrategy) {
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy,
                                jobId);

                ResponseEntity<Object> resp = ratePlanDetailService.process(properties, RatingPlanDetailUploadFile);
                if (resp.getStatusCode() == HttpStatus.OK) {
                        ratePlanDetailService.processAsync(properties);
                }
                return resp;

        }

        @PostMapping("/RatingPlanRate/upload")
        public ResponseEntity<Object> RatingPlanRateUploadFile(
                        @RequestParam(value = "JOB_ID", required = false) String jobId,
                        @RequestParam(value = "RatingPlanRateUploadFile", required = true) MultipartFile RatingPlanRateUploadFile,
                        @RequestParam(value = "launchEntity", required = false, defaultValue = "true") String launchEntity,
                        @RequestParam(value = "sendReconSheet", required = false, defaultValue = "true") String sendReconSheet,
                        @RequestParam(value = "sendEmail", required = false, defaultValue = "true") String sendEmail,
                        @RequestParam(value = "changeStrategy", required = false, defaultValue = "true") String changeStrategy) {
                if (StringUtility.isEmpty(jobId)) {
                        jobId = talend.generateUniqJobId();
                }

                JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy,
                                jobId);
                ResponseEntity<Object> resp = ratePlanRateService.process(properties, RatingPlanRateUploadFile);

                if (resp.getStatusCode() == HttpStatus.OK) {
                        ratePlanRateService.processAsync(properties);
                }
                return resp;

        }

}
