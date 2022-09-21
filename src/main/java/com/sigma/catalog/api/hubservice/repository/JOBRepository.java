package com.sigma.catalog.api.hubservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sigma.catalog.api.hubservice.dbmodel.JOB;

@Repository
public interface JOBRepository extends JpaRepository<JOB, String> {

    @Query("SELECT j FROM #{#entityName} j WHERE j.jobId = ?1 and j.jobType = ?2 and j.jobCategory = ?3")
    List<JOB> findJobValidationStatus(String jobId, String jobType, String jobCategory);

    @Query("SELECT j FROM #{#entityName} j WHERE j.jobId = ?1 ")
    List<JOB> findJobEvents(String jobId);

}

