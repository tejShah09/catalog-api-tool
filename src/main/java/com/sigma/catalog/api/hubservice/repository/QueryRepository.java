package com.sigma.catalog.api.hubservice.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository
public class QueryRepository {

    @PersistenceContext
    EntityManager entityManager;

    public String query(String queryString) {
        Query query = entityManager.createNativeQuery(queryString);
        String data = "[]";
        try {
            data = (String) query.getSingleResult();
        } catch (Exception e) {
           // e.printStackTrace();
           System.out.println("errror "+queryString);
        }

        // System.out.println(data);

        return data;

    }

}