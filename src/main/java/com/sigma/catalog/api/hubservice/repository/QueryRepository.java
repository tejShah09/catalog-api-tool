package com.sigma.catalog.api.hubservice.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class QueryRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final Logger LOG = LoggerFactory.getLogger(QueryRepository.class);
    public String query(String queryString) {
        Query query = entityManager.createNativeQuery(queryString);
        String data = "[]";
        try {
            LOG.info("CAPI UI :: query ::  " + queryString);
            data = (String) query.getSingleResult();
        } catch (Exception e) {
            // e.printStackTrace();
           
            LOG.info("CAPI UI :: query error :: "+e.getMessage());
        }

        // LOG.info(data);

        return data;

    }



}