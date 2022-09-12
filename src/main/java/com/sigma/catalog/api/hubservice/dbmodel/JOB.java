package com.sigma.catalog.api.hubservice.dbmodel;

import java.util.Date;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "jobs")
public class JOB {
    private long id;
    private String jobId;
    private String jobCategory;

    private String jobType;

    private String status;

    @Column(length=2048)
    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date doneAt;

    public JOB() {

    }

    @PrePersist
    private void onCreate() {
        doneAt = new Date();
    }

    public JOB(String jobId, String jobType, String jobCategory, String status, String message) {

        this.jobId = jobId;
        this.status = status;
        this.message = message;
        this.jobType = jobType;
        this.jobCategory = jobCategory;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDoneAt() {
        return doneAt;
    }

    public void setDoneAt(Date doneAt) {
        this.doneAt = doneAt;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    @Override
    public String toString() {
        return "JOB [doneAt=" + doneAt + ", id=" + id + ", jobCategory=" + jobCategory + ", jobId=" + jobId
                + ", jobType=" + jobType + ", message=" + message + ", status=" + status + "]";
    }

}
