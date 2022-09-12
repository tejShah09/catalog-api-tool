package com.sigma.catalog.api.talendService.Endpoints;

import java.nio.file.Paths;
import java.util.HashMap;

import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendConstants;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.StringUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/jobs/Offer")
public class Offer {

    @Autowired
    private TalendHelperService talend;
    private static final Logger LOG = LoggerFactory.getLogger(Offer.class);

    @PostMapping("/UpdateMarketStatus")
    public ResponseEntity<Object> UpdateMarketStatus(
            @RequestParam(value = "MarketStatusSheet", required = true) MultipartFile marketSTatusSheet,
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "sheetName", required = false) String sheetName) throws TalendException{
        String fileNAme = "MarketStatusSheet_" + jobId;
        talend.writeFile(marketSTatusSheet, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xlsx");
        HashMap<String, String> config = new HashMap<>();
        config.put("excelName", fileNAme);
        if (StringUtility.isEmpty(sheetName)) {
            sheetName = "Market_Status";
        }
        config.put("SheetName", "Market_Status");
        String message = talend.executeJob(jobId, "MarketStatusUpdate", config);

        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/UpdateMarketing")
    public ResponseEntity<Object> UpdateMarketing(
            @RequestParam(value = "MarketingSheet", required = true) MultipartFile marketSTatusSheet,
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "sheetName", required = false) String sheetName)throws TalendException {
        String fileNAme = "MarketingSheet_" + jobId;
        talend.writeFile(marketSTatusSheet, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xlsx");
        HashMap<String, String> config = new HashMap<>();
        config.put("excelName", fileNAme);
        if (StringUtility.isEmpty(sheetName)) {
            sheetName = "Marketing";
        }
        config.put("SheetName", "Marketing");
        String message = talend.executeJob(jobId, "MarketingUpdate", config);

        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/CreateOffer")
    public ResponseEntity<Object> CreateOffer(
            @RequestParam(value = "OfferSheet", required = true) MultipartFile marketSTatusSheet,
            @RequestParam(value = "JOB_ID", required = false) String jobId)throws TalendException {
        String fileNAme = "OfferSheet_" + jobId;
        talend.writeFile(marketSTatusSheet, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xlsx");
        HashMap<String, String> config = new HashMap<>();
        config.put("excelName", fileNAme);
    
        config.put("SheetName", "Market_Status");
        String message = talend.executeJob(jobId, "ReadOfferSheet", config);

        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/UpdateSalesChannleStatus")
    public ResponseEntity<Object> UpdateSalesChannleStatus(
            @RequestParam(value = "SalesChannelSheet", required = true) MultipartFile marketSTatusSheet,
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "sheetName", required = false) String sheetName) throws TalendException{
        String fileNAme = "SalesChannelSheet_" + jobId;
        talend.writeFile(marketSTatusSheet, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xlsx");
        HashMap<String, String> config = new HashMap<>();
        config.put("excelName", fileNAme);

        if (StringUtility.isEmpty(sheetName)) {
            sheetName = "Sheet1";
        }
        config.put("SheetName", sheetName);
        String message = talend.executeJob(jobId, "SaleChannelUpdate", config);

        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/LoadOfferAssoc")
    public ResponseEntity<Object> LoadOfferAssoc(
            @RequestParam(value = "AssocSheet", required = true) MultipartFile marketSTatusSheet,
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "sheetName", required = false) String sheetName) throws TalendException {
        String fileNAme = "AssocSheet_" + jobId;
        talend.writeFile(marketSTatusSheet, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xlsx");
        HashMap<String, String> config = new HashMap<>();
        config.put("excelName", fileNAme);

        if (StringUtility.isEmpty(sheetName)) {
            sheetName = "Associations";
        }
        config.put("SheetName", sheetName);
        String message = talend.executeJob(jobId, "ReadOfferAssociation", config);

        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/LoadDiscount")
    public ResponseEntity<Object> LoadDiscount(
            @RequestParam(value = "DiscountSheet", required = true) MultipartFile marketSTatusSheet,
            @RequestParam(value = "JOB_ID", required = false) String jobId,
            @RequestParam(value = "sheetName", required = false) String sheetName)throws TalendException {
        String fileNAme = "DiscountSheet_" + jobId;
        talend.writeFile(marketSTatusSheet, Paths.get(TalendConstants.INPUT_FILE_LOCATION), fileNAme + ".xlsx");
        HashMap<String, String> config = new HashMap<>();
        config.put("excelName", fileNAme);

        if (StringUtility.isEmpty(sheetName)) {
            sheetName = "DiscountRelations";
        }
        config.put("SheetName", sheetName);
        String message = talend.executeJob(jobId, "ReadDiscount", config);

        return talend.generateResponse(jobId, message);
    }

}
