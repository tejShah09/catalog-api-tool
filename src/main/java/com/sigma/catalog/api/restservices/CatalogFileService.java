/*
 * 
 */
package com.sigma.catalog.api.restservices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sigma.catalog.api.exception.CatalogAPIException;

/**
 * The Class CatalogFileService.
 */
@Service
public class CatalogFileService {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(CatalogFileService.class);

	/** The Constant UPLOADEDFILES. */
	public static final String UPLOADEDFILES = "./toImportFiles/";

	/** The Constant DOWNLODEDFILES. */
	public static final String DOWNLODEDFILES = "./exportedFiles/";

	/**
	 * Store file.
	 *
	 * @param file
	 *            the file
	 * @param fileName
	 *            the file name
	 * @throws CatalogAPIException
	 *             the catalog API exception
	 */
	public void storeFile(MultipartFile file, String fileName) throws CatalogAPIException {
		try {
			Files.copy(file.getInputStream(), Paths.get(UPLOADEDFILES).resolve(fileName),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			throw new CatalogAPIException("Unable to upload file!", e);
		}
	}

	/**
	 * Load file.
	 *
	 * @param filename
	 *            the filename
	 * @return the resource
	 * @throws CatalogAPIException
	 *             the catalog API exception
	 */
	public Resource loadFile(String filename) throws CatalogAPIException {
		try {
			Path file = Paths.get(DOWNLODEDFILES).resolve(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new CatalogAPIException("No data found fo this template!");
			}
		} catch (MalformedURLException e) {
			throw new CatalogAPIException("File name is not valid !", e);
		}
	}

	/**
	 * Delete file.
	 *
	 * @param filename
	 *            the filename
	 */
	public void deleteFile(String filename) {
		try {
			Files.deleteIfExists(Paths.get(CatalogFileService.DOWNLODEDFILES + filename));
		} catch (IOException e) {
			LOG.error("Unable to delete file : {}", filename, e);
		}
	}
}