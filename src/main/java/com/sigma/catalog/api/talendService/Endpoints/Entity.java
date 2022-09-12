package com.sigma.catalog.api.talendService.Endpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.sigma.catalog.api.drools.ExecutionEngine;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.restservices.CatalogHelperService;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.talendService.model.JobRequest;
import com.sigma.catalog.api.utility.StringUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Node;

@RestController
@RequestMapping("/jobs/Entity")
public class Entity {
    private static final Logger LOG = LoggerFactory.getLogger(Entity.class);
    @Autowired
    private TalendHelperService talend;

    @PostMapping("/Delete")
    public ResponseEntity<Object> CreateEntity(@RequestBody JobRequest request)throws TalendException  {
        String message = "created entity successfully ";
        String jobId = request.getJobId();
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }

        HashMap<String, String> config = new HashMap<>();
        config.put("query", request.getQuery());
        config.put("statusTable", request.getStatusTable());
        message = talend.executeAsyncJob(request.getJobId(), "CAPIDeleteEntity", config);

        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/Approve")
    public ResponseEntity<Object> ApproveEntity(@RequestBody JobRequest request)throws TalendException  {
        String message = "created entity successfully ";
        String jobId = request.getJobId();
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }

        HashMap<String, String> config = new HashMap<>();
        config.put("query", request.getQuery());
        config.put("statusTable", request.getStatusTable());
        message = talend.executeAsyncJob(request.getJobId(), "CAPIApproveEntity", config);

        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/Stage")
    public ResponseEntity<Object> StageEntity(@RequestBody JobRequest request) throws TalendException {
        String message = "created entity successfully ";
        String jobId = request.getJobId();
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }
        HashMap<String, String> config = new HashMap<>();
        config.put("query", request.getQuery());
        config.put("statusTable", request.getStatusTable());
        message = talend.executeAsyncJob(request.getJobId(), "CAPIStageEntity", config);
        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/Live")
    public ResponseEntity<Object> LiveEntity(@RequestBody JobRequest request) throws TalendException {
        String message = "created entity successfully ";
        String jobId = request.getJobId();
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }
        HashMap<String, String> config = new HashMap<>();
        config.put("query", request.getQuery());
        config.put("statusTable", request.getStatusTable());
        message = talend.executeAsyncJob(request.getJobId(), "CAPILiveEntity", config);
        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/Edit")
    public ResponseEntity<Object> editEntity(@RequestBody JobRequest request) throws TalendException {
        String message = "created entity successfully ";
        String jobId = request.getJobId();
        if (StringUtility.isEmpty(jobId)) {
            jobId = talend.generateUniqJobId();
        }
        HashMap<String, String> config = new HashMap<>();
        config.put("query", request.getQuery());
        config.put("statusTable", request.getStatusTable());
        message = talend.executeAsyncJob(request.getJobId(), "CAPIEditEntity", config);
        return talend.generateResponse(jobId, message);
    }

    @PostMapping("/BatchAction")
    public ResponseEntity<Object> BatchAction(@RequestBody Map<String, String> request)throws TalendException  {

        String jobId = talend.generateUniqJobId();
        HashMap<String, String> config = new HashMap<String, String>(request);
        if (StringUtility.isEmpty(config.get("jobId"))) {
            config.put("jobId", jobId);
        } else {
            jobId = config.get("jobId");
        }

        List<String> responseses = new ArrayList<>();
        String query = config.get("query");
        String batchSize = config.get("batchSize");
        String repetaion = config.get("repetaion");

        if (StringUtility.isNotEmpty(repetaion) && StringUtility.isNotEmpty(batchSize)
                && StringUtility.isNumeric(repetaion) && StringUtility.isNumeric(batchSize)) {
            int batchsizeInt = Integer.valueOf(batchSize);
            int repetaionInt = Integer.valueOf(repetaion);

            for (int i = 0; i < repetaionInt; i++) {
                String subquery = " order by \"PublicID\" LIMIT " + batchSize + " OFFSET "
                        + String.valueOf(i * batchsizeInt);
                HashMap<String, String> newConfig = (HashMap<String, String>) config.clone();
                newConfig.put("query", query + subquery);
                LOG.info(newConfig.toString());
                String message = query + subquery;
                message = message + "_" + talend.executeAsyncJob(jobId, config.get("jobName"), newConfig);
                responseses.add(message);
            }

        } else {
            LOG.info(config.toString());
            String message = talend.executeAsyncJob(jobId, config.get("jobName"), config);
            responseses.add("message");
        }

        return new ResponseEntity<Object>(responseses.toString(), HttpStatus.OK);
    }

    // @PostMapping("/ConvertXML")
    @RequestMapping(value = "/ConvertXML", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Object> ConvertXML(@RequestBody Map<String, Object> request) {

        List<String> sheets = (List) request.get("sheets");
        String category = (String) request.get("category");
        String json = new Gson().toJson(request.get("payload"), LinkedHashMap.class);

        ExecutionEngine engin = CatalogHelperService.startEngine();
        LinkedHashMap excludeUpdates = null;
        if (request.get("excludeUpdates") instanceof LinkedHashMap) {
            excludeUpdates = (LinkedHashMap) request.get("excludeUpdates");
        }
        if (request.get("excludeUpdates") instanceof String) {
            String exludestring = (String) request.get("excludeUpdates");
            Gson gson = new Gson();
            excludeUpdates = gson.fromJson(exludestring, LinkedHashMap.class);
        }

        String response = CatalogHelperService.generateXMLForEntity(category, json, sheets, engin, excludeUpdates);
        return new ResponseEntity<Object>(response, HttpStatus.OK);
    }

}
