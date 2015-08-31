package org.talend.components.salesforce.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Used the test the component service and the Salesforce components with an external web service.
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.talend.components")
public class TestApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}
}
