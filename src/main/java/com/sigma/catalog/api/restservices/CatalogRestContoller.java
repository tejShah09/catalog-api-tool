/*
 * 
 */
package com.sigma.catalog.api.restservices;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.catalog.Decorator;
import com.sigma.catalog.api.drools.CatalogSheetsProcessing;
import com.sigma.catalog.api.export.ExportConstants;
import com.sigma.catalog.api.export.ExportLookUp;
import com.sigma.catalog.api.export.ProcessExport;
import com.sigma.catalog.api.model.configuration.ConfigArgumentsModel;
import com.sigma.catalog.api.utility.StringUtility;

/**
 * The Class CatalogRestContoller.
 */
@Controller
public class CatalogRestContoller {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(CatalogRestContoller.class);

	/** The storage service. */
	@Autowired
	private CatalogFileService storageService;


	/**
	 * Export excel.
	 *
	 * @param templateName
	 *            the template name
	 * @param catalogOperation
	 *            the catalog operation
	 * @param entityNames
	 *            the entity names
	 * @return the response entity
	 */
	@GetMapping(value = "/export")
	public ResponseEntity<Resource> exportExcel(@RequestParam String templateName,
			@RequestParam String catalogOperation,
			@RequestParam(value = "entityNames", required = false) String entityNames) {
		LOG.info("exporting data for templateName : {}, catalogOperation : {}, entityNames : {}", templateName,
				catalogOperation, entityNames);
		templateName = StringUtility.removeSpaceWithUnderScore(templateName);
		String filename = String.join("-", "Export", catalogOperation, "for", templateName,
				LocalDateTime.now().toString().replace(':', '-'), ".xlsx");
		ConfigArgumentsModel configArgs = new ConfigArgumentsModel();
		configArgs.setTemplateName(templateName);
		configArgs.setDestinationFile(CatalogFileService.DOWNLODEDFILES + filename);
		configArgs.setIntent(ExportConstants.EXPORT);
		configArgs.setResponseReportType("Both");
		configArgs.setCatalogOperation(catalogOperation);
		configArgs.setEntityNames(entityNames);
		Resource file = null;
		try {
			ExportLookUp processLookup = new ExportLookUp();
			ProcessExport processExport = new ProcessExport();
			if (catalogOperation.equalsIgnoreCase("TCharValue")) {
				processLookup.buildTCharValueExport(configArgs, catalogOperation);
			} else if (catalogOperation.equalsIgnoreCase("TChar") || catalogOperation.equalsIgnoreCase("TTreeLeaf")
					|| catalogOperation.equalsIgnoreCase("TTreeBranch")
					|| catalogOperation.equalsIgnoreCase("Lookup")) {
				processLookup.buildFactExport(configArgs, catalogOperation);
			} else {
			
				processExport.buildEntityExport(configArgs);
			}
			file = storageService.loadFile(filename);
			// String jobId = templateName + "_"+Integer.toString(ThreadLocalRandom.current().nextInt(1000, 9999));
			// try {
			// 	new PythonHelper(jobId, filename).insertJob();
			// } catch (PythonExecutionException e) {
			// 	LOG.error("Error while executing python script : " , e);
			// 	return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			// }
		} catch (Exception e) {
			LOG.error("Error while exporting data for the template name :" + configArgs.getTemplateName(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		LOG.info("Instances of a Template Type {} exported in an Excel file successfully",
				configArgs.getTemplateName());
		
		
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename).body(file);
	}

	/**
	 * Import excel.
	 *
	 * @param inputExcelsheet
	 *            the input excelsheet
	 * @param changesetFile
	 *            the changeset file
	 * @param templateName
	 *            the template name
	 * @param catalogOperation
	 *            the catalog operation
	 * @param changesetName
	 *            the changeset name
	 * @return the response entity
	 */
	@PostMapping("/import")
	public ResponseEntity<Resource> importExcel(@RequestParam(value ="file", required = false) MultipartFile inputExcelsheet,
			@RequestParam(value = "changesetFile", required = false) MultipartFile changesetFile,
			@RequestParam String templateName, @RequestParam String catalogOperation,
			@RequestParam(value = "changesetName", required = false) String changesetName
			) {
	
		String srcFileName = "";	
		String destFileName	= "";	
	
			srcFileName = inputExcelsheet.getOriginalFilename();
		LOG.info("importing excel sheet fileName : {}, templateName : {}, catalogOperation : {}",
				inputExcelsheet.getOriginalFilename(), templateName, catalogOperation);
		srcFileName = String.join("-", LocalDateTime.now().toString().replace(':', '-'), srcFileName);
			destFileName = String.join("-", ExportConstants.IMPORT, catalogOperation, templateName,
				LocalDateTime.now().toString().replace(':', '-'), ".xlsx");
			
		Resource file = null;
		String typeOfImport = "template";
		try {
			CatalogSheetsProcessing catalogSheetsProcessing = new CatalogSheetsProcessing();
			ConfigArgumentsModel configArgs = new ConfigArgumentsModel();
			configArgs.setTemplateName(StringUtility.removeSpaceWithUnderScore(templateName));
			configArgs.setDestinationFile(CatalogFileService.DOWNLODEDFILES + destFileName);
			configArgs.setIntent(ExportConstants.IMPORT);
			configArgs.setSourceFile(CatalogFileService.UPLOADEDFILES + srcFileName);
			configArgs.setResponseReportType("Both");
			configArgs.setCatalogOperation(catalogOperation);
			configArgs.setJobId("test");
			
			storageService.storeFile(inputExcelsheet, srcFileName);
			
			if (catalogOperation.equalsIgnoreCase(ExportConstants.OP_ALLOWANCE_DEFINITION)
					|| (catalogOperation.equalsIgnoreCase(ExportConstants.OP_LOOKUP))) {
				typeOfImport = "fact";
			}
			catalogSheetsProcessing.sheetsReadAndProcess(configArgs, typeOfImport);
			if (changesetFile != null) {
				srcFileName = changesetFile.getOriginalFilename();
				LOG.info("importing excel sheet for changeset fileName :{}", srcFileName);
				srcFileName = String.join("-", LocalDateTime.now().toString().replace(':', '-'), srcFileName);
				storageService.storeFile(changesetFile, srcFileName);
				configArgs.setSourceFile(CatalogFileService.UPLOADEDFILES + srcFileName);
				configArgs.setCatalogOperation(ExportConstants.CHANGESET);
				catalogSheetsProcessing.sheetsReadAndProcess(configArgs, "template");
			}
			file = storageService.loadFile(destFileName);
		} catch (Exception e) {
			LOG.error("Error while importing excel sheet {}", srcFileName, e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Decorator.decoratorMap.clear();
		
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + destFileName).body(file);
	}
}
