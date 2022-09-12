package com.sigma.catalog.api.hubservice.services;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JOB;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.hubservice.repository.JOBRepository;
import com.sigma.catalog.api.talendService.TalendHelperService;

@Configuration
@EnableAsync
class AsynchConfigurationService {
    @Bean(name = "asyncExecutorService")
    public Executor asyncExecutorService() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("asyncExecutorService-");
        executor.initialize();
        return executor;
    }
}

@Service
public abstract class AbstractShellProcessService {
    @Autowired
    public JOBRepository jobtable;

    @Autowired
    public TalendHelperService talend;

    @Autowired
    private EmailService emailServer;

    public String jobCategory;

    public abstract void startSyncProcessing(String jobId, String fileName) throws TalendException;

    public abstract void startASyncProcessing(String jobId) throws TalendException;

    public abstract String saveFile(String jobId, MultipartFile file) throws TalendException;

    public ResponseEntity<Object> process(String jobId, MultipartFile file) {
        try {
            String fileName = this.saveFile(jobId, file);
            this.startSyncProcessing(jobId, fileName);

        } catch (TalendException e) {
            jobtable.save(new JOB(jobId, JOBKeywords.STOP, jobCategory,
                    JOBKeywords.JOB_FAILED, e.getMessage()));
            return talend.generateFailResponse(jobId, e);
        }

        return talend.generateSuccessResponse(jobId);
    }

    @Async("asyncExecutorService")
    public void processAsync(String jobId) {
        try {
            this.startASyncProcessing(jobId);
            jobtable.save(new JOB(jobId, JOBKeywords.STOP, jobCategory,
                    JOBKeywords.JOB_SUCCESS, "eaam Enjoy"));
        } catch (TalendException e) {
            // Send Email
            jobtable.save(new JOB(jobId, JOBKeywords.STOP, jobCategory,
                    JOBKeywords.JOB_FAILED, e.getRowOuptutException(jobId).toString()));
            // emailServer.sendMail(e.talendErrorMessage,
            // e.getRowOuptutException(jobId).toString());
        }
    }
}
