package com.sigma.catalog.api.hubservice.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.boot.spi.MetadataImplementor;
import org.springframework.stereotype.Repository;

@Repository
public class QueryRepository {

    @PersistenceContext
    EntityManager entityManager;

    public String query(String queryString) {
        Query query = entityManager.createNativeQuery(queryString);
        String data = "[]";
        try {
            System.out.println("query ::  " + queryString);
            data = (String) query.getSingleResult();
        } catch (Exception e) {
            // e.printStackTrace();
           
            System.out.println(e.getMessage());
        }

        // System.out.println(data);

        return data;

    }



}