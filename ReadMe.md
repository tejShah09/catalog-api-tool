# ------------------------------------------------------------------------------------------------------------------------------------------
## CATALOG API TOOL
# ------------------------------------------------------------------------------------------------------------------------------------------

## PREREQUISITES:

- JAVA 8.x should be installed and JAVA_HOME should be set in environment variables.
- MAVEN 3.5.x should be installed and MAVEN_HOME should be set in environment variables.
- GIT bash (Optional).

## BUILD:
 
- Build the project using command

  		cd catalog-api-tool/
  		mvn clean install 

## ENVIRONMENT PROPERTIES FILE FORMAT :

- Edit file config/EnvironmentDetail.xml.

   Update Sigma Identity Manager, Catalog Services API, Catalog Management and Catalog API URL's of running catalog instance
		
  		<IdentityURL>https://identity.catvm.local:3443/Issue/WebApi</IdentityURL>
  		<SigmaCatalogServicesApiURL>http://catalogapi.catvm.local/SigmaCatalogServices</SigmaCatalogServicesApiURL>
  		<ManagementURL>https://management.catvm.local:4443</ManagementURL>
  		<CatalogAPIURL>https://catalogapi.catvm.local:6443</CatalogAPIURL>
   
   Update running Catalog instance details, Token management, security certificates , User name/password and connection related details of running catalog instance
   
   		<CatalogInstance>Default_01</CatalogInstance>
   		<catalogInstanceID>8376b5f4-760e-7835-354f-bb12ec05ddf9</catalogInstanceID>
   		<TokenType>urn:oasis:names:tc:SAML:1.0:assertion</TokenType>
  	
   If running catalog instance requires some certificates to connect to it , set this flag to true else false , use  "ReadMe-ImportCatalogCertificates.md" to get the certificates.
		<jksLoad>true</jksLoad>
   		

   Update user name and Base 64 encoded password of running catalog instance  	

   		<Username>Administrator</Username>
		<Password>S3amuscat</Password>
		
   Update JDBC URL, user name and Base 64 encoded password of running Database instance
   
   		<dbURL>jdbc:jtds:sqlserver://catalogapi.catvm.local:1433;databaseName=CPQ_Training;</dbURL>
   		<dbUserName>Test User</dbUserName>
   		<dbPassword>VGVzdDEyMw==</dbPassword>
	
   Default properties
	
 		<TrustStore>./config/catalogStore/catalog.jks</TrustStore>
   		<trustPassword>changeit</trustPassword>
		<ConnectTimeout>1200000</ConnectTimeout>
   		<ReadTimeout>1200000</ReadTimeout>

## RUN :
	
- Update required environment properties in the file - config/EnvironmentDetail.xml

- Import the certificates from running catalog instance for identity and catalog API URL in config/catalog.jks file by following steps given in the file "ReadMe-ImportCatalogCertificates.md", if required [if running catalog instance is secured with   certificates, we need this step].

- Check Sigma Catalog Services are up and running, hit <SigmaCatalogServicesApiURL> given in config/EnvironmentDetail.xml file from chrome browser. 
		
  		e.g.	Sample Sigma Catalog Services API URL : "http://catalogapi.catvm.local/SigmaCatalogServices"
		
- Update catalog.properties, if required.
 		
- Update config/application.properties, to change log location and server port, if required.

   To change default values of below properties, uncomment (remove '#') them and update the values :

   		#logging.file=logs/catalog-api-tool.log  
   		#server.port=8080 

- Open cmd.exe and run the tool using below commands 

   		cd catalog-api-tool/
   		java -Xms2G -Xmx2G -jar target/CatalogApiTool-1.0.0.1.jar	

- Access the catalog tool with below URL
 		Local/Single Node Machine : http://localhost:<server.port>/   
 		Remote/Multi Node Machine : http://<ip-addr-of-machine-where-catalog-api-tool-is-running>:<server.port>/  
 		Note - server port is 8080 only if default port value is unchanged in config/application.properties file.

- Logs are captured at below location.
		
 		logs/catalog-api-tool.log 
 		Note - only if default location is unchanged in config/application.properties file. 
		
- Uploaded (to imported) and exported (to download) files are stored in /toImportFiles and /exportedFiles folders respectively.


NOTE-1 Mandatory : Please provide sheet names as per the catalog operation in case of import.

 	Below are the list of import operations and required/associated sheet names for the same.

 		Rates :  "Rates" (if you have multiple rates sheet, add "Rates_" at the start of each sheet name e.g. "Rates_sheet1","Rates_sheet2"). 
	 	Notifications :  "Entity"
	 	Associations :   "Associations"
	 	Allowance Definition :  "TCharValue"
	 	Grandfather of an entity :  "Entity"
	 	Allowance Assignment Rule :  "Mapping Rules"
		Change Status :  "Change Status"

NOTE-2 Optional : Provide 'entitiesCountToExport' value in config/catalog.properties file to limit data for the number of entities in export, if required.
		
 		e.g entitiesCountToExport = 100
 		
Note-3 Provide correct column names as per the entity type(Prepaid or Post-paid)

 		Payment_Methods
		Payment_Methods_Postpaid
		 
 		Reminder
		Reminder_postpaid
		
 		Reporting_Level
		Reporting_Level_Postpaid
 		
 		Price_Password
		Price_Password_Postpaid
		
 		Price_ID
		Price_ID_Postpaid
		
 		Counter_Type
		Counter_Type_Postpaid
# ---------------------------------------------------------------------------------------------------------------------------------------------
