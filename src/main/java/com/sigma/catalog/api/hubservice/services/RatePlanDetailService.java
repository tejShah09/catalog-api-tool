package com.sigma.catalog.api.hubservice.services;

import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.dbmodel.JobProperites;
import com.sigma.catalog.api.hubservice.exception.TalendException;

@Service
public class RatePlanDetailService extends AbstractShellProcessService {

        RatePlanDetailService() {
                jobCategory = JOBKeywords.RATEPLANDETAIL;
                sheets = "'Consumption Units','Inclusive Loss Factors','Use Daylight Saving Time','Consumption Value','Demand Units','Entity'";
                reportTable = "CAPI_RatePlanDetail_AllStatus";
                inpuTableKey = "Description";
        }

        public void startASyncProcessing(JobProperites properites) throws TalendException {

           
                createEntity(properites);

            
                approveEntity(properites);

                
                stageEntity(properites);

               
                makeLiveWithStatusCheck(properites);

                
                sendReconfile(properites, JOBKeywords.RATEPLAN_TYPE);

        }

        public void startSyncProcessing(JobProperites properites) throws TalendException {

        }

}
