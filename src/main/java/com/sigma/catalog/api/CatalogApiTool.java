/*
 * 
 */
package com.sigma.catalog.api;

import com.sigma.catalog.api.authentication.CatalogAuthentication;
import com.sigma.catalog.api.utility.FileUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The Class CatalogApiTool.
 */
@SpringBootApplication
public class CatalogApiTool implements CommandLineRunner {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(CatalogApiTool.class);

	
	public static void main(String[] args) {
		SpringApplication.run(CatalogApiTool.class, args);

	}

	@Override
	public void run(String... args) throws Exception {
		CatalogAuthentication.getTokenAuthorized();
		FileUtility.createCatalogAPIFilesAndFolders();
		LOG.info("{}", "Application started..");
	}
}
