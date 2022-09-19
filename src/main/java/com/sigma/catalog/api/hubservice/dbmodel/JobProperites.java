package com.sigma.catalog.api.hubservice.dbmodel;

import com.sigma.catalog.api.utility.StringUtility;

public class JobProperites {
    private boolean launchEntity;
    private boolean sendReconSheet;
    private boolean sendEmail;
    private boolean changeStrategy;
    public String jobId;

    public JobProperites(String jobId) {
        launchEntity = true;
        sendReconSheet = true;
        sendEmail = true;
        changeStrategy = true;
        this.jobId = jobId;
    }

    public JobProperites(String launchEntity, String sendReconSheet, String sendEmail, String changeStrategy,
            String jobId) {
        this.launchEntity = covertToBoolean(launchEntity);
        this.sendReconSheet = covertToBoolean(sendReconSheet);
        this.sendEmail = covertToBoolean(sendEmail);
        this.changeStrategy = covertToBoolean(changeStrategy);
        this.jobId = jobId;
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

}