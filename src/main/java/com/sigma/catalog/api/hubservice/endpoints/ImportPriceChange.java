package com.sigma.catalog.api.hubservice.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.services.JobService;
import com.sigma.catalog.api.hubservice.services.ProductXMLRatesService;
import com.sigma.catalog.api.hubservice.services.RatePlanXMLRatesService;
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

    @PostMapping("/importPrice/upload")
    public ResponseEntity<Object> stage(
            @RequestParam(value = "RatePlanRateXML", required = false) MultipartFile[] rateXmls,
            @RequestParam(value = "BundleRateXML", required = false) MultipartFile[] bundlXmls,
            @RequestParam(value = "JOB_ID", required = false) String jobId) {
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }

        JobProperites properties = new JobProperites(
                jobId);

        ResponseEntity<Object> resp = productXmlService.validateJobId(properties);
        if (resp != null) {
            return resp;
        }
        if (rateXmls != null) {

            resp = ratePlanXmlService.processXML(properties, rateXmls);
            if (resp.getStatusCode() == HttpStatus.OK) {

                ratePlanXmlService.processAsync(properties);
            } else {
                return resp;
            }

        }
        if (bundlXmls != null) {
            resp = productXmlService.processXML(properties, bundlXmls);
            if (resp.getStatusCode() == HttpStatus.OK) {

                productXmlService.processAsync(properties);
            } else {
                return resp;
            }
        }

        return resp;

    }

}
