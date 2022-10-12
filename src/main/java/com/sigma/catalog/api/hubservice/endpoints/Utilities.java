package com.sigma.catalog.api.hubservice.endpoints;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.sigma.catalog.api.authentication.JKSFileLoad;
import com.sigma.catalog.api.catalog.CatalogCommunicator;
import com.sigma.catalog.api.catalog.FactsForAttributes;
import com.sigma.catalog.api.configuration.CatalogConstants;
import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.services.EmailService;
import com.sigma.catalog.api.hubservice.services.JobService;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.ConfigurationUtility;
import com.sigma.catalog.api.utility.StringUtility;

@RestController
@RequestMapping("/utility")
public class Utilities {

    @Autowired
    private TalendHelperService talend;

    @Autowired
    private EmailService emailServer;

    @Autowired
    private JobService jobservice;

    @PostMapping("/sendEmail")
    public ResponseEntity<Object> sendMail(
            @RequestBody Map<String, String> request) {
        try {
            String jobId = talend.generateUniqJobId();
            if (!StringUtility.isEmpty(request.get("jobId"))) {
                jobId = request.get("jobId");
            }
            String subject = "TEST EMAIL";
            if (!StringUtility.isEmpty(request.get("subject"))) {
                subject = request.get("subject");
            }
            String body = "{}{}{}{}{}";
            if (!StringUtility.isEmpty(request.get("body"))) {
                body = request.get("body");
            }
            emailServer.sendMail(new JobProperites(jobId), subject, body);

            return new ResponseEntity<Object>("Sent", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/changeStrategy")
    public ResponseEntity<Object> changeStrategy(
            @RequestBody Map<String, String> request) {

        HashMap<String, String> config = new HashMap<>();
        String jobId = talend.generateUniqJobId();
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        try {

            config.put("hubIntegration",
                    StringUtility.isEmpty(request.get("hubIntegration")) ? "true" : request.get("hubIntegration"));
            config.put("instanceId", ConfigurationUtility.getEnvConfigModel().getCatalogInstanceID());
            talend.executeJob(jobId, "ChangeStrategy", config);
            return talend.getSucessResponse(jobId);

        } catch (TalendException e) {
            return talend.generateFailResponse(jobId, e);
        }

    }

    @PostMapping("/executeJob")
    public ResponseEntity<Object> ExecuteJob(@RequestBody Map<String, String> request) {
        HashMap<String, String> config = new HashMap<String, String>(request);
        String jobId = talend.generateUniqJobId();
        jobservice.startJOB(jobId, JOBKeywords.ADD_HOOK);
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        try {
            talend.executeJob(jobId, config.get("jobName"), config);
            jobservice.stopJOB(jobId, JOBKeywords.ADD_HOOK);
            return talend.getSucessResponse(jobId);

        } catch (TalendException e) {
            jobservice.failedJOB(jobId, JOBKeywords.ADD_HOOK,String.valueOf(e.getCustomException(jobId)));
            return talend.generateFailResponse(jobId, e);
        }

    }

    @PostMapping("/executeJobAndFindResult")
    public ResponseEntity<Object> executeJobAndFindResult(@RequestBody Map<String, String> request) {
        HashMap<String, String> config = new HashMap<String, String>(request);
        String jobId = talend.generateUniqJobId();
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        try {
            jobservice.startJOB(jobId, JOBKeywords.ADD_HOOK);
            talend.executeJob(jobId, config.get("jobName"), config);

            HashMap<String, String> configNew = new HashMap<>();
            configNew.put("tableName", config.get("statusTable"));
            configNew.put("jobType", JOBKeywords.ADD_HOOK);
            configNew.put("jobCategory", config.get("jobName"));
            configNew.put("keyName", config.get("keyName"));
            configNew.put("jobStatus", JOBKeywords.TASK_END);
            talend.executeJob(jobId, "getStatusCount", configNew);

        } catch (TalendException e) {
            jobservice.failedJOB(jobId, JOBKeywords.ADD_HOOK, String.valueOf(e.getCustomException(jobId)));
            return talend.generateFailResponse(jobId, e);
        }

        try {
            jobservice.checkForStatusError(jobId, JOBKeywords.ADD_HOOK, config.get("jobName"),
                    config.get("jobName") + " Failed JobId : " + jobId);
        } catch (TalendException e) {
            jobservice.failedJOB(jobId, JOBKeywords.ADD_HOOK, String.valueOf(e.getRowOuptutException(jobId)));
            return talend.generateFailCountResponse(jobId, e);
        }
        jobservice.stopJOB(jobId, JOBKeywords.ADD_HOOK);
        return talend.getSucessResponse(jobId);
    }

    @PostMapping("/test")
    public ResponseEntity<Object> test(@RequestBody Map<String, String> request) {
        String input = "<ServerResponse xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schema.sigma-systems.com/catalog.api/models\">  <Succeeded>true</Succeeded>  <Response>    <References>      <ReferenceItem>        <Name>Winter</Name>        <PublicID>164ac8ea-d7d0-4eea-adcf-211ca5411657</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Summer Peak Demand</Name>        <PublicID>1f5dd773-a4a0-405a-8a76-23247620d4ea</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Shoulder</Name>        <PublicID>eb008131-d23d-44c3-99e7-2df4150c040a</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Climate Saver</Name>        <PublicID>20556430-91b3-4f8d-88ae-2e0c473af497</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>High Season Demand</Name>        <PublicID>74db1e58-4815-070f-bf66-38edec0ad2d3</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Winter Peak</Name>        <PublicID>e182dc94-94e8-43a4-8333-413945da0682</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Off Peak Demand</Name>        <PublicID>0d183754-29e1-4cc8-813b-45a484403311</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Non-summer Peak Demand</Name>        <PublicID>746a97a1-46c8-4593-bcfb-5716b581c1d7</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Summer Peak</Name>        <PublicID>fde3e74e-80ed-4f85-abc8-5eeefb1066b6</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Additional Demand</Name>        <PublicID>f1724ae4-92f8-4d0c-8201-6222188e735c</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Summer</Name>        <PublicID>a039112b-86bb-42b8-8220-648802cb3b0e</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Controlled Load Off-peak</Name>        <PublicID>5665ac10-e605-11ea-98e4-6728648391dc</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Controlled Peak</Name>        <PublicID>d224195b-941a-49c2-933a-6a53078d55b6</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Shoulder Demand</Name>        <PublicID>91a38e52-f460-4cae-b833-6bdf5bc30d95</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Winter Off Peak</Name>        <PublicID>9d9ac243-7a54-416c-bb6e-6f6a83cd7a0e</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Feed-In</Name>        <PublicID>59d1649e-0b0d-4d8e-ae9c-71bcf7f6fdea</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Ancillary 2</Name>        <PublicID>a034e93b-b0fe-49f7-aa68-7312bdc0c74e</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Premium Solar Feed-In</Name>        <PublicID>5409097a-bbb4-4cc3-be46-7527325d3c6a</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Peak Demand</Name>        <PublicID>035bdf42-2be4-46e4-950d-7ec0858592d6</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Ancillary</Name>        <PublicID>d0a473b1-290e-4435-9705-847bed8fe33c</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Demand</Name>        <PublicID>91b7cf53-e833-4fea-a39a-9d7c221153a6</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Summer Shoulder</Name>        <PublicID>25480910-d6f9-4bca-b5da-9f1faa59b1fc</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Low Season Demand</Name>        <PublicID>15e1969d-c362-6091-4449-a2fe8347781d</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Saver</Name>        <PublicID>4d3dcb9b-e3f8-453e-a080-a3fae7798780</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Week-end Peak</Name>        <PublicID>58744d54-387e-4110-ae6a-a8679c78f067</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Winter Shoulder</Name>        <PublicID>1484efb7-b0b8-41dd-a4b2-add573a1a1d5</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Solar Sponge</Name>        <PublicID>56a1f1c3-e605-11ea-98e4-b7bc7e584dc4</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Off-Peak</Name>        <PublicID>3141d12c-c09d-4a58-8e71-ce4a304ff1ed</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Peak</Name>        <PublicID>2d8ddd18-52e6-4ea4-bd4f-d2b161ee6ade</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Controlled Load Solar Sponge</Name>        <PublicID>567f2781-e605-11ea-98e4-d58571edbd06</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Controlled Load Peak</Name>        <PublicID>5690b3b2-e605-11ea-98e4-d9913e4a28a0</PublicID>      </ReferenceItem>      <ReferenceItem>        <Name>Summer Off Peak</Name>        <PublicID>dd12272d-566e-45cf-99d8-f8eb3d432250</PublicID>      </ReferenceItem>    </References>  </Response></ServerResponse>";
        System.out.println(FactsForAttributes.parseFile(input));

        return talend.getSucessResponse("jobId");
    }

    @PostMapping("/callCSAPI")
    public ResponseEntity<Object> callCSAPI(@RequestBody Map<String, String> request) {

        String url = ConfigurationUtility.getEnvConfigModel().getSigmaCatalogServicesApiURL();
        if (!StringUtility.isEmpty(request.get("url"))) {
            url = request.get("url");
        }
        String sixthUrlString = url + request.get("basepath");
        HttpHeaders headers = new HttpHeaders();
        headers.add(CatalogConstants.ACCEPT, CatalogConstants.APPLICATION_XML);
        headers.add(CatalogConstants.CONTENT_TYPE, CatalogConstants.APPLICATION_XML);
        HttpEntity<String> requestTemp = new HttpEntity<>(headers);
        RestTemplate rs;
        if (StringUtility.equalsAnyIgnoreCase("true", request.get("isSSL"))) {
            rs = JKSFileLoad.getNonSSLTemplate();
        } else {
            rs = JKSFileLoad.getRestTemplate();
        }
        byte[] createEntityResponse = CatalogCommunicator.getAPIResponseByte(rs,
                sixthUrlString,
                HttpMethod.GET, requestTemp);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", sixthUrlString);
        map.put("body", new String(createEntityResponse, StandardCharsets.UTF_8));
        return new ResponseEntity<Object>(map, HttpStatus.OK);
    }

}
