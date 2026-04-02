package com.chtrembl.petstoreapp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Order
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Order {
	private String id;
	private String email;
	private List<Product> products;
	private Status status;
	private Boolean complete = false;

	/**
	 * Order Status
	 */
	public enum Status {
		PLACED("placed"),
		APPROVED("approved"),
		DELIVERED("delivered");

		private final String value;

		Status(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static Status fromValue(String text) {
			for (Status b : Status.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	public Order id(String id) {
		this.id = id;
		return this;
	}

	public Order products(List<Product> products) {
		this.products = products;
		return this;
	}

	public boolean isComplete() {
		return complete != null && complete;
	}
}
