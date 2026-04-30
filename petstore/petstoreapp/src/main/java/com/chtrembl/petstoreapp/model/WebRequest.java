package com.chtrembl.petstoreapp.model;

import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

@Getter
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebRequest implements Serializable {
	private final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

	public void addHeader(String headerKey, String headerValue) {
		this.headers.add(headerKey, headerValue);
	}
}
