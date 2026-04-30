package com.chtrembl.petstoreapp.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.chtrembl.petstoreapp")
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class AppConfig {
	
}
