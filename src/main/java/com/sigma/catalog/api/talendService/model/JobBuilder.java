package com.sigma.catalog.api.talendService.model;



import org.springframework.stereotype.Service;

import com.sigma.catalog.api.hubservice.constnats.JOBKeywords;
import com.sigma.catalog.api.hubservice.exception.TalendException;
import com.sigma.catalog.api.talendService.TalendConstants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JobBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(JobBuilder.class);

	/** The props. */
	private  static Properties props;
    public Class getJobClass(String jobName) throws TalendException {
        Class jobclass = null;     


        try {

            String joblocation = props.getProperty(jobName,JOBKeywords.JOB_NOT_FOUND);
            if(JOBKeywords.JOB_NOT_FOUND.equals(joblocation)){
                throw new TalendException("JOB Configuration not found. verify talendjars.properties :: "+jobName);
            }
            jobclass = Class.forName(joblocation);
        } catch (ClassNotFoundException e) {
            throw new TalendException("JOB Class not found:: "+jobName);
        }
        return jobclass;

    }

    public JobBuilder(){
        LOG.debug("Loading catalog properties");
		try (InputStream input = new FileInputStream(TalendConstants.CONFIG_FILE_LOCATION+"talendjars.properties");) {
			props = new Properties();
			props.load(input);
		} catch (IOException ex) {
			LOG.error("Error while loading propertiy file.", ex);
		}
    }
 


}

/*
switch (jobName) {
    case "IntialDataSetup":
        jobclass = IntialDataSetup.class;
        break;
    case "TestAllConnection":
        jobclass = TestAllConnection.class;
        break;
    case "MetaDataSetup":
        jobclass = MetaDataSetup.class;
        break;

    case "FoureFileJob":
        jobclass = eMainJob.class;
        break;
    case "ReadBCRRSheet":
        jobclass = ReadSheets.class;
        break;

    case "RatingPlanDetails":
        jobclass = RatingPlanDetails.class;
        break;

    case "RatingPlanRates":
        jobclass = RatingPlanRates.class;
        break;

    case "Bundle":
        jobclass = Bundle.class;
        break;

    case "ConvertHUBRateToCAPI":
        jobclass = ConvertHUBRateToCAPI.class;
        break;

    case "LoadHubXmlInputSheet":
        jobclass = LoadHubXmlInputSheet.class;
        break;

    case "Component":
        jobclass = Component.class;
        break;

    case "CAPICreateEntities":
        jobclass = CAPICreateEntities.class;
        break;

    case "CAPICreateRates":
        jobclass = CAPICreateRates.class;
        break;

    case "CAPICreateAssoc":
        jobclass = CAPICreateAssoc.class;
        break;

    case "CAPIDeleteEntity":
        jobclass = CAPIDeleteEntity.class;
        break;

    case "CAPIApproveEntity":
        jobclass = CAPIApproveEntity.class;
        break;

    case "CAPIStageEntity":
        jobclass = CAPIStageEntity.class;
        break;

    case "CAPILiveEntity":
        jobclass = CAPILiveEntity.class;
        break;

    case "CAPIEditEntity":
        jobclass = CAPIEditEntity.class;
        break;
    case "CAPICreateRateRecall":
        jobclass = CAPICreateRateRecall.class;
        break;

    case "CreateCAPIInputSheet":
        jobclass = CreateCAPIInputSheet.class;
        break;

    case "FindGuids":
        jobclass = FindGuids.class;
        break;

    case "MarketStatusUpdate":
        jobclass = MarketStatusUpdate.class;
        break;

    case "SaleChannelUpdate":
        jobclass = SaleChannelUpdate.class;
        break;

    case "RatingPlanRateStandAlone":
        jobclass = RatingPlanRateStandAlone.class;
        break;
    case "ReadOfferSheet":
        jobclass = ReadOfferSheet.class;
        break;

    case "ReadOfferAssociation":
        jobclass = ReadOfferAssociation.class;
        break;

    case "ConvertHUBProductRateToCAPI":
        jobclass = ConvertHUBProductRateToCAPI.class;
        break;

    case "LoadHubProductXmlInputSheet":
        jobclass = LoadHubProductXmlInputSheet.class;
        break;

    case "LoadRateRecallSheet":
        jobclass = LoadRateRecallSheet.class;
        break;

    case "CAPICreateDiscount":
        jobclass = CAPICreateDiscount.class;
        break;

    case "MarketingUpdate":
        jobclass = MarketingUpdate.class;
        break;

    case "ReadDiscount":
        jobclass = ReadDiscount.class;
        break;

    case "findoutNewOrModify":
        jobclass = findoutNewOrModify.class;
        break;

    case "GenerateReportForModifyPRod":
        jobclass = GenerateReportForModifyPRod.class;
        break;

    case "FindoutTypeOfUpdate":
        jobclass = FindoutTypeOfUpdate.class;
        break;

    case "testExcelLoad":
        jobclass = testExcelLoad.class;
        break;
    case "CreateMarketStatusRecall":
        jobclass = CreateMarketStatusRecall.class;
        break;

    case "ReadBundle":
        try {
            jobclass = Class.forName("ecapi.readbundlesheet_0_1.ReadBundleSheet");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        break;

    default:
        break;
}

 */