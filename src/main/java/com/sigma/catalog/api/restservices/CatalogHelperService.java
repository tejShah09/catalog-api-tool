package com.sigma.catalog.api.restservices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sigma.catalog.api.configuration.CatalogConstants.Statuses;
import com.sigma.catalog.api.authentication.CatalogTokenGeneration;
import com.sigma.catalog.api.catalog.CatalogAPI;
import com.sigma.catalog.api.catalog.CatalogCommunicator;
import com.sigma.catalog.api.configuration.CatalogConstants;
import com.google.gson.Gson;
import com.sigma.catalog.api.authentication.CatalogAuthentication;
import com.sigma.catalog.api.drools.ExecutionEngine;
import com.sigma.catalog.api.model.configuration.ElementConfigModel;
import com.sigma.catalog.api.model.rules.Fact;
import com.sigma.catalog.api.parser.JsonParser;
import com.sigma.catalog.api.parser.XmlParser;
import com.sigma.catalog.api.utility.ConfigurationUtility;
import com.sigma.catalog.api.utility.StringUtility;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class CatalogHelperService {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogHelperService.class);

    public static ExecutionEngine startEngine() {
        CatalogAuthentication.setHeaders(CatalogTokenGeneration.getAccessToken());
        ExecutionEngine executionEngine = new ExecutionEngine();
        return executionEngine;
    }

    public static void refreshToken() {
        CatalogAuthentication.setHeaders(CatalogTokenGeneration.getAccessToken());
    }

    public static Fact generateProcesFactForEntity(String category, String input, List<String> nastedClass,
            ExecutionEngine engine, Map<String, ArrayList> excludeUpdates) {

        Fact fact = generateFactFromJson(category, input, nastedClass, excludeUpdates);
        String intnet = fact.getDataFromRow(CatalogConstants.INTENT);
        // Fact fact = generateFact();
        Node xmlNode = null;
        ElementConfigModel elementModel = null;
        xmlNode = XmlParser.getXmlDocumentNode(fact.getSheet(), intnet, fact);
        elementModel = JsonParser.getJsonConfiguration(fact.getSheet(), intnet, fact);
        Node finalRequest = engine.genrateXml(xmlNode, elementModel, fact);
        String request = XmlParser.nodeToString(finalRequest);
        fact.setDataToRow(request, "payloads");
        return fact;
    }

    public static Fact generateProcesFactForEntity(String category, String input, List<String> nastedClass,
            ExecutionEngine engine, String excludeUpdates) {
        Gson gson = new Gson();
        Map<String, ArrayList> excludeUpdatesMap = gson.fromJson(excludeUpdates, LinkedHashMap.class);
        return generateProcesFactForEntity(category, input, nastedClass,
                engine, excludeUpdatesMap);
    }

    public static Node generateXMLNodeForEntity(String category, String input, List<String> nastedClass,
            ExecutionEngine engine) {

        Fact fact = generateFactFromJson(category, input, nastedClass, null);
        String intnet = fact.getDataFromRow(CatalogConstants.INTENT);
        // Fact fact = generateFact();
        Node xmlNode = null;
        ElementConfigModel elementModel = null;
        xmlNode = XmlParser.getXmlDocumentNode(fact.getSheet(), intnet, fact);
        elementModel = JsonParser.getJsonConfiguration(fact.getSheet(), intnet, fact);
        Node finalRequest = engine.genrateXml(xmlNode, elementModel, fact);
        return finalRequest;
    }

    public static String generateXMLForEntity(String category, String input, List<String> nastedClass,
            ExecutionEngine engine) {
        return generateXMLForEntity(category, input, nastedClass, engine, null);
    }

    public static String generateXMLForEntity(String category, String input, List<String> nastedClass,
            ExecutionEngine engine, Map<String, ArrayList> excludeUpdates) {
        Fact fact = generateProcesFactForEntity(category, input, nastedClass, engine, excludeUpdates);
        return fact.getDataFromRow("payloads");
    }

    public static String approveEntity(String guid) {
        String response = "NotExecuted";
        response = CatalogAPI.getStatusResponse(Statuses.APPROVE, guid);
        return response;
    }

    public static String editEntity(String guid) {
        String response = "NotExecuted";
        response = CatalogAPI.getStatusResponse(Statuses.EDIT, guid);
        return response;
    }

    public static boolean isValidForStage(String previousStatus, String previousResponse) {
        boolean isValid = false;
        if (previousStatus == null && previousResponse == null) {
            isValid = true;
        } else if (previousStatus.equals("Stage") && !previousResponse.contains("success,200:success")) {
            isValid = true;
        } else if (previousStatus.equals("Approve") && previousResponse.contains("success,200:success")) {
            isValid = true;
        }

        return isValid;
    }

    public static boolean isValidForLive(String previousStatus, String previousResponse) {
        boolean isValid = false;
        if (previousStatus == null && previousResponse == null) {
            isValid = true;
        } else if (previousStatus.equals("Live") && !previousResponse.contains("success,200:success")) {
            isValid = true;
        } else if (previousStatus.equals("Stage") && previousResponse.contains("success,200:success")) {
            isValid = true;
        }
        return isValid;
    }

    public static String StageEntity(String guid) {
        String response = "NotExecuted";
        response = CatalogAPI.getStatusResponse(Statuses.STAGE, guid);
        return response;
    }

    public static String LaunchEntity(String guid) {
        String response = "NotExecuted";
        response = CatalogAPI.getStatusResponse(Statuses.LAUNCH, guid);
        return response;
    }

    public static String generateXMLForEntity(String category, String input, ExecutionEngine engine) {
        return generateXMLForEntity(category, input, new ArrayList<String>(), engine);
    }

    public static void main1(String[] args) {
        String input = "{\"Intent\":\"New\",\"Class_Type\":\"Product_Bundle_Property_Set\",\"Name\":\"ESBUECL0713S\",\"PublicID\":\"Generate Guid\",\"Effective_Start_Date\":\"2021-10-28\",\"Available_Start_Date\":\"2021-10-28\",\"Effective_End_Date\":null,\"Available_End_Date\":null,\"Description\":\"United SME + E + CL Jul2013\",\"Category_ID\":\"124\",\"psid\":\"210\",\"psKey\":\"PROD_BUNDLE\",\"Complete_Flag\":\"Y\",\"Product_ID\":\"ESBUECL0713S\",\"invPSDirtyFlg\":\"N\",\"Product_Class_and_Product_Subclass\":\"{Electricity Products.Energy Charges}\",\"PowerQuote_Product_ID\":null,\"GL_Charge_Type_and_Item\":null,\"GL_Product_Code\":null,\"Intrastat_Code\":null,\"Tangible_Indicator\":null,\"Product_Charge_and_Inventory_Type\":\"{Bundle Product.Not Applicable.Standard Bundle Product Property Set}\",\"Utility_Type\":\"Electricity\",\"Rule_Evaluation_Method\":null,\"GL_Account_Revenue_Code\":null,\"Document_Name\":null,\"GL_Account_Discount_Code\":null,\"Concession_Type_for_Admin_Fee\":null,\"Energy_Charge_Flag_for_Reward_Plans\":null,\"Conditional_Product_Rule\":null,\"Pro_Rata_Indicator\":null,\"Dependent_Indicator\":null,\"Energy_Related_Indicator\":null,\"Report_Key\":null,\"Contract_Term\":null,\"State_Product_Group_and_Feed_In_Type\":\"{Victoria.Victoria - SOR2013.Not Applicable}\",\"External_Tariff_Code\":null,\"Distributor\":\"United Energy Distribution Pty Ltd\",\"Bundle_Expansion_Invoice_Indicator\":\"No\",\"Market_Segment\":\"SME Tranche 0 - 20\",\"Component_Fuel\":null,\"Utility_Type_and_Retailer\":\"{Electricity.Not Applicable}\",\"Retail_Tariff_Code\":\"\",\"Product_Category\":null,\"Tariff_Zone_for_Post_Code\":null,\"Customer_Group\":null,\"Rate_Type\":\"Peak\",\"Retail_Charge_Method\":\"Bundled\",\"Tariff_Class\":null,\"Network_Tariff_Zone\":null,\"Quote_Type\":null,\"Package\":null,\"System_Size_kW\":null,\"Pricing_Condition_Category_and_Pricing_Condition\":null,\"Quantity\":null,\"Product_Line\":null,\"Tax_Code\":null,\"State\":null,\"vintagexsdname\":\"Vintage\",\"has_Vintage_Attribute\":\"True\",\"Vintage_Id\":\"1\",\"productDocumentxsdname\":null,\"has_Product_Document_Attribute\":null,\"Product_Document_Id\":null,\"warrantyTermxsdname\":null,\"has_Warranty_Term_Attribute\":null,\"Warranty_Term_Id\":null,\"Change_ID\":null,\"Reference_Id\":\"1\",\"has_Reference\":\"True\",\"referencexsdname\":\"Reference\",\"id\":15,\"status\":null,\"status_reason\":null,\"payloads\":null,\"Reference\":[{\"Intent\":\"New\",\"Reference_Id\":\"1\",\"Reference_Class\":\"Reference_Class\",\"Reference_Guid\":\"Generate Guid\",\"Base_Product_Reference\":\"EA SME ELECTRICITY\",\"baseclasstype\":\"Product_Base_Property_Set\",\"Rating_Plan_Reference\":\"ESBUECL0713S\",\"ratingplanclasstype\":\"Rating_Energy_Usage\",\"Start_Date\":\"2021-10-28\",\"End_Date\":null,\"Product_ID\":\"ESBUECL0713S\"}],\"Vintage\":[{\"Intent\":\"New\",\"Vintage_Id\":\"1\",\"Vintage_Attribute_Class\":\"Vintage_Class\",\"Vintage_Attribute_Guid\":\"Generate guid\",\"Start_Date\":\"2021-10-28\",\"End_Date\":null,\"Value\":\"SOR\",\"Product_ID\":\"ESBUECL0713S\"}]}";
        ExecutionEngine e = startEngine();
        String response = generateXMLForEntity("Entity", input, Arrays.asList("Reference", "Vintage"), e);
        System.out.println(response);
    }

    public static void main(String[] args) {
        CatalogHelperService.refreshToken();
        String PublicID = "2e271aa5-bee7-11ec-9016-e58ef22e790d";
        String response = CatalogHelperService.StageEntity(PublicID);
        System.out.println(response);
        if (response != null) {
            String[] sp = response.split(":");
            System.out.println("sp");
            System.out.println(sp[0]);
        }

    }

    public static Fact generateFactFromJson(String category, String input) {
        return generateFactFromJson(category, input, new ArrayList<String>(), null);
    }

    public static String createEntity(String request) {
        String entityResponse = "";
        HttpEntity<String> catalogRequest = new HttpEntity<>(request, CatalogAuthentication.getHeaders());
        entityResponse = CatalogCommunicator.sendCatalogRequest(
                ConfigurationUtility.getEnvConfigModel().getCatalogAPIURL() + "/api/Entity",
                HttpMethod.resolve("POST"), catalogRequest);
        return entityResponse;
    }

    public static String deleteEntity(String guid) {
        String entityResponse = "";
        HttpEntity<String> catalogRequest = new HttpEntity<>("", CatalogAuthentication.getHeaders());
        entityResponse = CatalogCommunicator.sendCatalogRequest(
                ConfigurationUtility.getEnvConfigModel().getCatalogAPIURL() + "/api/Entity/" + guid,
                HttpMethod.resolve("DELETE"), catalogRequest);
        return entityResponse;
    }

    public static Fact generateFactFromJson(String category, String input, List<String> nastedClass,
            Map<String, ArrayList> excludeUpdates) {

        Fact fact = new Fact(category);
        fact.createdWitoutExcel = true;
        JSONObject jsonObject = new JSONObject(input.trim());
        List<String> exlstuionList = null;
        if (excludeUpdates != null && excludeUpdates.get(category) != null
                && excludeUpdates.get(category) instanceof ArrayList) {
            exlstuionList = (ArrayList) excludeUpdates.get(category);
        }
        fact.setRow(generateDataRowFromJson(jsonObject, nastedClass, exlstuionList));
        String intnet = fact.getDataFromRow(CatalogConstants.INTENT);
        // if ("NEW".equalsIgnoreCase(intnet) && nastedClass.size() > 0) {
        for (int si = 0; si < nastedClass.size(); si++) {
            String sheetName = nastedClass.get(si);
            if (!jsonObject.isNull(sheetName) && jsonObject.get(sheetName) != null
                    && jsonObject.get(sheetName) instanceof JSONArray) {
                JSONArray sheetRows = (JSONArray) jsonObject.get(sheetName);
                if (!sheetRows.isEmpty() && sheetRows.length() > 0) {
                    List<LinkedHashMap<String, String>> dataRows = new ArrayList<LinkedHashMap<String, String>>();
                    for (int i = 0; i < sheetRows.length(); i++) {

                        try {
                            List<String> childExlcution = null;
                            if (excludeUpdates != null && excludeUpdates.get(sheetName) != null
                                    && excludeUpdates.get(sheetName) instanceof ArrayList) {
                                childExlcution = (ArrayList) excludeUpdates.get(sheetName);
                            }
                            LinkedHashMap<String, String> dataRow = generateDataRowFromJson(
                                    (JSONObject) sheetRows.get(i), childExlcution);
                            dataRows.add(dataRow);
                        } catch (Exception E) {
                            LOG.error("Cant get Sub sheet " + sheetName);
                        }

                    }
                    if (dataRows.size() > 0) {
                        fact.setRefranceSheet(sheetName, dataRows);
                    }

                }

            }

        }
        // }
        return fact;
    }

    public static LinkedHashMap<String, String> generateDataRowFromJson(JSONObject jsonObject) {
        return generateDataRowFromJson(jsonObject, new ArrayList<String>(), new ArrayList<String>());
    }

    public static LinkedHashMap<String, String> generateDataRowFromJson(JSONObject jsonObject,
            List<String> exclustionData) {
        return generateDataRowFromJson(jsonObject, new ArrayList<String>(), exclustionData);
    }

    public static LinkedHashMap<String, String> generateDataRowFromJson(JSONObject jsonObject,
            List<String> nastedClass, List<String> exclustionData) {
        if (exclustionData == null) {
            exclustionData = new ArrayList<>();
        }
        LinkedHashMap<String, String> dataRow = new LinkedHashMap<String, String>();

        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            if (nastedClass.contains(key)) {
                // This is netsted class dont add it in Datarow

            } else {
                if (jsonObject.get(key) != null && !jsonObject.isNull(key)) {
                    String value = String.valueOf(jsonObject.get(key));

                    if (!StringUtility.isEmpty(value)) {
                        dataRow.put(StringUtility.removeSpaceAnd_AndLowerCase(key), value);
                    }
                }

            }
        }

        if (!StringUtility.equalsIgnoreCase(dataRow.get("intent"), "New")) {
            for (String key : exclustionData) {
                dataRow.put(StringUtility.removeSpaceAnd_AndLowerCase(key), null);
            }

        }
        return dataRow;
    }

}
