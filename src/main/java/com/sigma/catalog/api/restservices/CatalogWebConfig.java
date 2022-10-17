/*
 * 
 */
package com.sigma.catalog.api.restservices;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * The Class CatalogWebConfig.
 */
@Configuration
public class CatalogWebConfig implements  WebMvcConfigurer  {

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.defaultContentType(MediaType.APPLICATION_JSON).favorPathExtension(true);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	  registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
	}
}
