package com.liv.api.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI livOpenApi(ObjectProvider<BuildProperties> buildPropertiesProvider) {
		BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
		String version = buildProperties != null ? buildProperties.getVersion() : "local";

		return new OpenAPI().info(new Info()
				.title("LIV API")
				.description("API de gestão da LIV Assessoria Previdenciária")
				.version(version)
				.license(new License().name("Uso interno")));
	}
}
