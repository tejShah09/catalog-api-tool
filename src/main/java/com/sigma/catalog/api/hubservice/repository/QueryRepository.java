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
        String data = (String) query.getSingleResult();

       // System.out.println(data);

        return data;

    }

}