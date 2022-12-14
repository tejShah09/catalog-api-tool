package com.sigma.catalog.api.talendService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.repository.JOBRepository;
import com.sigma.catalog.api.talendService.model.JobBuilder;

import routines.system.api.TalendJob;

@Configuration
@EnableAsync
class AsynchConfiguration {
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsynchThread-");
        executor.initialize();
        return executor;
    }
}

@Service
public class TalendHelperService {

    private static final Logger LOG = LoggerFactory.getLogger(TalendHelperService.class);
    Properties talendProperties;

    @Autowired
    private JobBuilder builder;

    public void loadDbProperties() {

        try (InputStream input = new FileInputStream("config/talend.properties");) {
            talendProperties = new Properties();
            talendProperties.load(input);
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }

    HashMap<String, String> initContext(String jobId) {
        HashMap<String, String> config = new HashMap<String, String>();
        loadDbProperties();
        Enumeration<String> enums = (Enumeration<String>) talendProperties.propertyNames();
        while (enums.hasMoreElements()) {
            String key = enums.nextElement();
            String value = talendProperties.getProperty(key);
            config.put(key, value);
        }
        config.put("JobId", jobId);
        config.put("syncMeta", "false");
        config.put("inputExcelFileLocation", TalendConstants.INPUT_FILE_LOCATION);
        config.put("outputExcelFileLocation", TalendConstants.OUTPUT_FILE_LOCATION);
        config.put("initialRefData", TalendConstants.CONFIG_FILE_LOCATION + "initialData.json");
        config.put("sheetInputPostFix", "_" + jobId);
        return config;
    }

    public <T extends TalendJob> String executeJob(String jobId,
            String jobName, HashMap<String, String> inputConfig) throws TalendException {
        long startTime = System.nanoTime();

        HashMap<String, String> config = initContext(jobId);
        if (inputConfig != null) {
            config.putAll(inputConfig);
        }
        Class<T> jobClass = builder.getJobClass(jobName);
        String[][] result = null;
        try {
            T newObj = jobClass.getDeclaredConstructor().newInstance();
           LOG.info("JOB ID::"+jobId+" Job Name::"+ jobName+"--- Param ------------------------------- " );
            result = jobClass.getDeclaredConstructor().newInstance().runJob(buildContext(config));

        } catch (Exception e) {
            throw new TalendException(e.getMessage());
        }
        long endTime = System.nanoTime();

        if (result != null && result[0] != null && result[0][0] != null && "0".equals(result[0][0])) {
            LOG.info("JOB ID::"+jobId+" Job Name::"+ jobName+" Execution Completed ID " + jobId + "_" + jobName + ", Timings "
                    + ((endTime - startTime) / 1000000)+"--------------------------");

        } else {
            LOG.error("JOB ID::"+jobId+" Job Name::"+ jobName+" Execution Failed ID " + jobId + "_" + jobName + ", Timings "
            + ((endTime - startTime) / 1000000)+"--------------------------");
            throw new TalendException("JOB FAILED jobId : " + jobId + " jobName : " + jobName );
        }

        return "eaam";
    }

    @Async("asyncExecutor")
    public <T extends TalendJob> String executeAsyncJob(String jobId, String jobName,
            HashMap<String, String> inputConfig) throws TalendException {
        return executeJob(jobId, jobName, inputConfig);
    }

    public static String[] buildContext(HashMap<String, String> config) {
        ArrayList<String> contextString = new ArrayList<String>();
        Iterator hmIterator = config.entrySet().iterator();
        while (hmIterator.hasNext()) {

            Map.Entry mapElement = (Map.Entry) hmIterator.next();
            LOG.info(mapElement.getKey() + "::" + mapElement.getValue());
            contextString.add("--context_param " + mapElement.getKey() + "=" + mapElement.getValue());
        }

        String k[] = contextString.toArray(new String[contextString.size()]);

        return k;
    }

    public void writeFile(MultipartFile file, Path dir, String fileName) {
        if (file != null) {
            Path filepath = Paths.get(dir.toString(), fileName);

            try (OutputStream os = Files.newOutputStream(filepath)) {
                os.write(file.getBytes());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void createJobsFolders() {

        try {
            FileUtils.forceMkdir(new File(TalendConstants.INPUT_FILE_LOCATION));
            FileUtils.forceMkdir(new File(TalendConstants.OUTPUT_FILE_LOCATION));
            FileUtils.forceMkdir(new File(TalendConstants.EMAIL_FILE_LOCATION));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String generateUniqJobId() {
        String uniqueKey = String.valueOf(System.currentTimeMillis());
        return uniqueKey.toString();
    }

    public ResponseEntity<Object> generateResponse(String uniqkey, String message) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", message);
        map.put("status", HttpStatus.OK.value());
        map.put("jobId", uniqkey);

        return new ResponseEntity<Object>(map, HttpStatus.OK);
    }

    public ResponseEntity<Object> generateSuccessResponse(String jobId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jobId", jobId);
        return new ResponseEntity<Object>(map, HttpStatus.OK);
    }

    public ResponseEntity<Object> generateFailResponse(String jobId, TalendException e) {

        return new ResponseEntity<Object>(e.getCustomException(jobId), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> generateFailCountResponse(String jobId, TalendException e) {

        return new ResponseEntity<Object>(e.getRowOuptutException(jobId), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Object> getSucessResponse(String jobId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jobId", jobId);
        return new ResponseEntity<Object>(map, HttpStatus.OK);
    }

}
