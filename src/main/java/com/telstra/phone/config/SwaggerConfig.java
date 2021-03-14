package com.telstra.phone.config;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Bean
	public Docket productApi() {

		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(getApiInfo())
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.telstra.phone")).paths(PathSelectors.any())
				.build();

	}

	private ApiInfo getApiInfo() {

		ApiInfo apiInfo = new ApiInfo(
				"Telstra REST API documentation",
				"Telstra Phone REST API",
				"1.0",
				"Terms of service",
				new Contact("sdevanat", "telstra.com", "sdevanat@gmail.com"),
				"Apache License Version 2.0",
				"https://www.apache.org/licenses/LICENSE-2.0",
				new ArrayList<>());

		return apiInfo;

	}

}
