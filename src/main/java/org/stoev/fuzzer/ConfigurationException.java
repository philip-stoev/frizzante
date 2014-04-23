package org.stoev.fuzzer;

public class ConfigurationException extends RuntimeException {
	static final long serialVersionUID = 42L;

	ConfigurationException() {
		super();
	}

	ConfigurationException(final String message) {
		super(message);
	}

};
