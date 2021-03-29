package com.test.phone.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

public class SwaggerConfigTest {

	private SwaggerConfig config;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);

		config = new SwaggerConfig();
	}

	@Test
	public void testProductApi() {
		Docket apiInfo = config.productApi();
		assertNotNull(apiInfo);
		assertTrue(apiInfo.getDocumentationType().equals(DocumentationType.SWAGGER_2));
	}

}
