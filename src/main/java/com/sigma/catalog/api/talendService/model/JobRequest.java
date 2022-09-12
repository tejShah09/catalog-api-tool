package com.sigma.catalog.api.talendService.model;

import java.util.List;

public class JobRequest {

    private String jobId;
    private String query;
    private String inputSheetJob;
    private String statusTable;
    private String intent;

  

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getStatusTable() {
        return statusTable;
    }

    public void setStatusTable(String statusTable) {
        this.statusTable = statusTable;
    }

    public String getInputSheetJob() {
        return inputSheetJob;
    }

    public void setInputSheetJob(String inputSheetJob) {
        this.inputSheetJob = inputSheetJob;
    }

    public String getJobId() {
        return jobId;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "JobRequest [jobId=" + jobId + ", query=" + query + "]";
    }

}
