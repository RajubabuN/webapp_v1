package com.chtrembl.petstoreapp.model;

import com.chtrembl.petstoreapp.telemetry.PetStoreTelemetryClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session based for each user, each user will also have a unique Telemetry
 * Client instance.
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class User {
	private String name = "Guest";
	private String sessionId;
	private String email;
	private List<Pet> pets;
	private List<Product> products;
	private int cartCount;
	private boolean initialTelemetryRecorded;

	@Autowired(required = false)
	private transient PetStoreTelemetryClient telemetryClient;

	@Autowired
	private ContainerEnvironment containerEnvironment;

	public synchronized void setPets(List<Pet> pets) {
		this.pets = pets;
	}

	public synchronized void setProducts(List<Product> products) {
		this.products = products;
	}

	public synchronized String getName() {
		return this.name != null ? this.name : "Guest";
	}

	public synchronized void setName(String name) {
		this.name = name != null ? name : "Guest";
	}

	public Map<String, String> getCustomEventProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("session_Id", this.sessionId);
		properties.put("appDate", this.containerEnvironment.getAppDate());
		properties.put("appVersion", this.containerEnvironment.getAppVersion());
		properties.put("containerHostName", this.containerEnvironment.getContainerHostName());
		return properties;
	}
}