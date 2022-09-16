package com.sigma.catalog.api.hubservice.endpoints;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/jobs")
public class ImportPriceChange {

    @Autowired
    private TalendHelperService talend;

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

        List<String> ratefileNames = new ArrayList();
        List<String> bundlefileNames = new ArrayList();
        List<String> responseses = new ArrayList<>();

        if (rateXmls != null) {

            Arrays.asList(rateXmls).stream().forEach(file -> {

                String oringalFile = file.getOriginalFilename();
                if (StringUtility.contains(oringalFile, ".xml")) {
                    oringalFile = oringalFile.replace(".xml", "");
                }
                String fileNAme = "Rate_" + oringalFile + "_" + properties.jobId;

                talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xml");
                ratefileNames.add(fileNAme);

            });
        }

        if (bundlXmls != null) {
            Arrays.asList(bundlXmls).stream().forEach(file -> {

                String oringalFile = file.getOriginalFilename();
                if (StringUtility.contains(oringalFile, ".xml")) {
                    oringalFile = oringalFile.replace(".xml", "");
                }
                String fileNAme = "Bundle_" + oringalFile + "_" + properties.jobId;

                talend.writeFile(file, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xml");
                bundlefileNames.add(fileNAme);

            });

        }
        System.out.println(properties.jobId+" Received Rate filesss" + ratefileNames.toString());
        System.out.println(properties.jobId+ "Received Bundle filesss" + bundlefileNames.toString());
        return talend.generateSuccessResponse(properties.jobId);

    }

}
