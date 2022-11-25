package com.sigma.catalog.api.hubservice.dbmodel;

import java.util.ArrayList;
import java.util.List;

import com.sigma.catalog.api.utility.StringUtility;

public class JobProperites {
    private boolean launchEntity;
    private boolean sendReconSheet;
    private boolean sendEmail;
    private boolean changeStrategy;
    private boolean onlySync;
    private String jobInputTable;

    public String jobId;
    private List<String> inputFiles;

    public JobProperites(String jobId) {
        launchEntity = true;
        sendReconSheet = true;
        sendEmail = true;
        changeStrategy = true;
        onlySync = false;
        this.jobId = jobId;
        inputFiles = new ArrayList<>();
    }

    public String getJobInputTable() {
        return jobInputTable;
    }

    public void setJobInputTable(String jobInputTable) {
        this.jobInputTable = jobInputTable;
    }

    public void addInputFileNAme(String fileName) {
        inputFiles.add(fileName);
    }

    public List<String> getInputFileNames() {
        return inputFiles;
    }

    public JobProperites(String launchEntity, String sendReconSheet, String sendEmail, String changeStrategy,
            String onlySync, String jobId) {
        this.launchEntity = covertToBoolean(launchEntity);
        this.sendReconSheet = covertToBoolean(sendReconSheet);
        this.sendEmail = covertToBoolean(sendEmail);
        this.changeStrategy = covertToBoolean(changeStrategy);
        this.onlySync = covertToBoolean(onlySync);
        this.jobId = jobId;
        inputFiles = new ArrayList<>();
    }

    private boolean covertToBoolean(String property) {
        return !StringUtility.equalsIgnoreCase(property, "false");
    }

    public boolean isLaunchEntity() {
        return launchEntity;
    }

    public boolean isSendReconSheet() {
        return sendReconSheet;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public boolean isChangeStrategy() {
        return changeStrategy;
    }

    public boolean isOnlySync() {
        return onlySync;
    }

    public void setOnlySync(boolean onlySync) {
        this.onlySync = onlySync;
    }

    public void setChangeStretegy(boolean changeStrategy) {
        this.changeStrategy = changeStrategy;
    }

    public void setChangeStretegy(String changeStrategy) {
        this.changeStrategy = !StringUtility.equalsIgnoreCase(changeStrategy, "false");
    }

}
