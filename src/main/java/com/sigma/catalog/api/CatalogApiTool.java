/*
 * 
 */
package com.sigma.catalog.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.sigma.catalog.api.authentication.CatalogAuthentication;
import com.sigma.catalog.api.talendService.TalendHelperService;
import com.sigma.catalog.api.utility.FileUtility;

/**
 * The Class CatalogApiTool.
 */
@SpringBootApplication
public class CatalogApiTool extends SpringBootServletInitializer implements ApplicationRunner {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(CatalogApiTool.class);

	@Autowired
	TalendHelperService talendservice;

	public static void main(String[] args) {
		
		SpringApplication.run(CatalogApiTool.class, args);

	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// TODO Auto-generated method stub
		CatalogAuthentication.getTokenAuthorized();
		FileUtility.createCatalogAPIFilesAndFolders();
		talendservice.createJobsFolders();
	}

}
