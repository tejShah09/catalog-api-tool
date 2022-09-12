package com.sigma.catalog.api.hubservice.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.utility.StringUtility;

public class TalendException extends Exception {

    public String talendErrorMessage;
    public Map<String, List<String>> dberrors;
    public List<Map<String, Object>> outputError;

    public TalendException(String errorMessage) {
        super();
        this.talendErrorMessage = errorMessage;
        dberrors = new HashMap<String, List<String>>();
    }

    public TalendException(String errorMessage, List<Map<String, Object>> errors) {
        super();
        this.talendErrorMessage = errorMessage;
        this.outputError = errors;
    }

    public TalendException(Map<String, List<String>> err) {
        super();
        this.dberrors = err;
    }

    public TalendException() {
        super();

        dberrors = new HashMap<String, List<String>>();
    }

    public Map<String, Object> getCustomException(String jobId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jobId", jobId);

        if (StringUtility.isNotEmpty(talendErrorMessage)) {
            addError(JOBKeywords.SYSTEM_ERROR, talendErrorMessage);
        }
        map.put("error", getError());

        return map;
    }

    public Map<String, Object> getRowOuptutException(String jobId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jobId", jobId);
        map.put("message", this.talendErrorMessage);
        map.put("error", this.outputError);
        
        return map;
    }

    public void addError(String category, String value) {

        List<String> values = dberrors.get(category);
        if (values == null) {
            values = new ArrayList<String>();
        }
        values.add(value);
        dberrors.put(category, values);
    }

    public List<Map<String, Object>> getError() {

        this.outputError = new ArrayList();
        for (Map.Entry<String, List<String>> err : this.dberrors.entrySet()) {

            String category = err.getKey();
            List<String> values = err.getValue();
            Map<String, Object> errorObject = new HashMap<String, Object>();
            errorObject.put("category", category);
            errorObject.put("values", values);
            this.outputError.add(errorObject);
        }

        return this.outputError;
    }

}
