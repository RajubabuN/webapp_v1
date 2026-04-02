package com.chtrembl.petstore.pet.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

@Component
@Setter
@Getter
@Slf4j
public class ContainerEnvironment {
    private String containerHostName;
    private String appVersion;
    private String appDate;
    private String year;

	@PostConstruct
    private void initialize() {
		try {
			this.setContainerHostName(
					InetAddress.getLocalHost().getHostAddress() + "/" + InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			this.setContainerHostName("unknown");
		}

		try {
			ObjectMapper objectMapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("version.json");

            try (InputStream inputStream = resource.getInputStream()) {
                Version version = objectMapper.readValue(inputStream, Version.class);
                this.setAppVersion(version.getVersion());
                this.setAppDate(version.getDate());
            }
		} catch (IOException e) {
            log.error("Error parsing file {}", e.getMessage());
			this.setAppVersion("unknown");
			this.setAppDate("unknown");
		}

		this.setYear(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
	}

    public String getAppVersion() {
        if ("version".equals(this.appVersion) || this.appVersion == null) {
            return String.valueOf(System.currentTimeMillis());
        }
        return this.appVersion;
    }
}
