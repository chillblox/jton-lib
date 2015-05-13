package com.arkasoft.jton.beans;

public class PropertyNotFoundException extends RuntimeException {
	private static final long serialVersionUID = -5962786618725599128L;

	public PropertyNotFoundException() {
		super();
	}

	public PropertyNotFoundException(String message) {
		super(message);
	}

	public PropertyNotFoundException(Throwable cause) {
		super(cause);
	}

	public PropertyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}