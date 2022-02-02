package io.mdsl.web;

import io.mdsl.web.config.ConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ConfigProperties.class)
public class MDSLToolsWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(MDSLToolsWebApplication.class, args);
	}

}
