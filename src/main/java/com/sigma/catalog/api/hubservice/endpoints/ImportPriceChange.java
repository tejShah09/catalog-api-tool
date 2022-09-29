package com.sigma.catalog.api.hubservice.endpoints;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.services.AbstractShellProcessService;
import com.sigma.catalog.api.hubservice.services.ImportPriceChageService;
import com.sigma.catalog.api.hubservice.services.ProductXMLRatesService;
import com.sigma.catalog.api.hubservice.services.RatePlanXMLRatesService;
import com.sigma.catalog.api.hubservice.services.RateRecallService;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/jobs")
public class ImportPriceChange {

    @Autowired
    private TalendHelperService talend;

    @Autowired
    ProductXMLRatesService productXmlService;

    @Autowired
    RatePlanXMLRatesService ratePlanXmlService;

    @Autowired
    ImportPriceChageService importPriceChangeService;

    @Autowired
    RateRecallService rateRecallService;

    @PostMapping("/RateRecall/upload")
    public ResponseEntity<Object> rateRecall(
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "RateRecallUploadFile", required = true) MultipartFile RateRecallUploadFile,
            @RequestParam(value = "launchEntity", required = false, defaultValue = "true") String launchEntity,
            @RequestParam(value = "sendReconSheet", required = false, defaultValue = "true") String sendReconSheet,
            @RequestParam(value = "sendEmail", required = false, defaultValue = "true") String sendEmail,
            @RequestParam(value = "changeStrategy", required = false, defaultValue = "true") String changeStrategy) {
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }

        JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy,
                jobId);
        ResponseEntity<Object> resp = rateRecallService.process(properties, RateRecallUploadFile);

        if (resp.getStatusCode() == HttpStatus.OK) {
            rateRecallService.processAsync(properties);
        }
        return resp;

    }

    @PostMapping("/importPrice/upload")
    public ResponseEntity<Object> stage(
            @RequestParam(value = "RatePlanRateXML", required = false) MultipartFile[] rateXmls,
            @RequestParam(value = "BundleRateXML", required = false) MultipartFile[] bundlXmls,
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "launchEntity", required = false, defaultValue = "true") String launchEntity,
            @RequestParam(value = "sendReconSheet", required = false, defaultValue = "true") String sendReconSheet,
            @RequestParam(value = "sendEmail", required = false, defaultValue = "true") String sendEmail,
            @RequestParam(value = "changeStrategy", required = false, defaultValue = "true") String changeStrategy) {
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();

        }

        JobProperites properties = new JobProperites(launchEntity, sendReconSheet, sendEmail, changeStrategy,
                jobId);

        ResponseEntity<Object> resp = importPriceChangeService.validateJobId(properties);
        List<AbstractShellProcessService> xmlServices = new ArrayList<>();
        if (resp != null) {
            return resp;
        }
        if (bundlXmls != null) {
            resp = productXmlService.processXML(properties, bundlXmls);
            if (resp.getStatusCode() == HttpStatus.OK) {
                xmlServices.add(productXmlService);
            } else {
                return resp;
            }
        }
        if (rateXmls != null) {

            resp = ratePlanXmlService.processXML(properties, rateXmls);
            if (resp.getStatusCode() == HttpStatus.OK) {
                xmlServices.add(ratePlanXmlService);
            } else {
                return resp;
            }

        }

        importPriceChangeService.processAsyncXML(properties, xmlServices);

        return resp;

    }

}
