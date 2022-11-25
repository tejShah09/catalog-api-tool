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

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.services.OfferAssociationService;
import com.sigma.catalog.api.hubservice.services.OfferDiscountService;
import com.sigma.catalog.api.hubservice.services.OfferService;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;
import com.sigma.catalog.api.hubservice.services.OfferSalesChannel;

@RestController
@RequestMapping("/jobs")
public class OfferUpload {
    @Autowired
    private TalendHelperService talend;

    @Autowired
    private OfferAssociationService offerAssociationService;

    @Autowired
    private OfferDiscountService offerDiscountService;

    @Autowired
    private OfferService offerService;

    @Autowired
    private OfferSalesChannel offerSalesChannel;

    @PostMapping("/OfferDiscount/upload")
    public ResponseEntity<Object> OfferDiscount(
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "OfferDiscountFile", required = true) MultipartFile OfferDiscountFile,
            @RequestParam(value = "launchEntity", required = false, defaultValue = "true") String launchEntity,
            @RequestParam(value = "sendReconSheet", required = false, defaultValue = "false") String sendReconSheet,
            @RequestParam(value = "sendEmail", required = false, defaultValue = "true") String sendEmail,
            @RequestParam(value = "changeStrategy", required = false, defaultValue = "false") String changeStrategy,
            @RequestParam(value = "onlySync", required = false, defaultValue = "false") String onlySync) {
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }

        JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy, onlySync,
                jobId);
        properties.setJobInputTable(jobId + "_" + JOBKeywords.OFFERDISCOUNT + "_Associations");

        ResponseEntity<Object> resp = offerDiscountService.process(properties, OfferDiscountFile, true);

        if (resp.getStatusCode() == HttpStatus.OK) {

            offerDiscountService.processAsync(properties);
        }
        return resp;

    }

    @PostMapping("/OfferAssociation/upload")
    public ResponseEntity<Object> OfferAssociation(
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "OfferAssociationFile", required = true) MultipartFile OfferAssociationFile,
            @RequestParam(value = "launchEntity", required = false, defaultValue = "true") String launchEntity,
            @RequestParam(value = "sendReconSheet", required = false, defaultValue = "false") String sendReconSheet,
            @RequestParam(value = "sendEmail", required = false, defaultValue = "false") String sendEmail,
            @RequestParam(value = "changeStrategy", required = false, defaultValue = "false") String changeStrategy,
            @RequestParam(value = "onlySync", required = false, defaultValue = "false") String onlySync) {
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }

        JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy,
                onlySync, jobId);
        properties.setJobInputTable(jobId + "_" + JOBKeywords.OFFERASSOC + "_Associations");
        ResponseEntity<Object> resp = offerAssociationService.process(properties, OfferAssociationFile, true);

        if (resp.getStatusCode() == HttpStatus.OK) {

            offerAssociationService.processAsync(properties);
        }
        return resp;

    }

    @PostMapping("/Offer/upload")
    public ResponseEntity<Object> Offer(
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "OfferFile", required = true) MultipartFile OfferFile,
            @RequestParam(value = "launchEntity", required = false, defaultValue = "false") String launchEntity,
            @RequestParam(value = "sendReconSheet", required = false, defaultValue = "false") String sendReconSheet,
            @RequestParam(value = "sendEmail", required = false, defaultValue = "false") String sendEmail,
            @RequestParam(value = "changeStrategy", required = false, defaultValue = "false") String changeStrategy,
            @RequestParam(value = "onlySync", required = false, defaultValue = "false") String onlySync) {
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }

        JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy,
                onlySync, jobId);
        properties.setJobInputTable(jobId + "_" + JOBKeywords.OFFER + "_Entity");
        ResponseEntity<Object> resp = offerService.process(properties, OfferFile, true);

        if (resp.getStatusCode() == HttpStatus.OK) {

            offerService.processAsync(properties);
        }
        return resp;

    }

    @PostMapping("/OfferSalesChannel/upload")
    public ResponseEntity<Object> OfferSalesChannel(
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "OfferSalesChannelFile", required = true) MultipartFile OfferSalesChannelFile,
            @RequestParam(value = "launchEntity", required = false, defaultValue = "false") String launchEntity,
            @RequestParam(value = "sendReconSheet", required = false, defaultValue = "false") String sendReconSheet,
            @RequestParam(value = "sendEmail", required = false, defaultValue = "false") String sendEmail,
            @RequestParam(value = "changeStrategy", required = false, defaultValue = "false") String changeStrategy,
            @RequestParam(value = "onlySync", required = false, defaultValue = "false") String onlySync) {
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }

        JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy,
                onlySync, jobId);
        properties.setJobInputTable(jobId + "_" + JOBKeywords.OFFERSALESCHANNEL + "_SalesChannel");
        ResponseEntity<Object> resp = offerSalesChannel.process(properties, OfferSalesChannelFile, true);

        if (resp.getStatusCode() == HttpStatus.OK) {

            offerSalesChannel.processAsync(properties);
        }
        return resp;

    }

}