package org.stoev.fuzzer;

public class ConfigurationException extends RuntimeException {
	static final long serialVersionUID = 42L;

	ConfigurationException(final String message) {
		super(message);
	}

};
