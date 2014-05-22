package org.stoev.fuzzer;

final class Constants {

	private Constants() { }

	// Constants for regular expressions
	public static final String OR = "|";

	public static final String OPTIONAL_WHITESPACE = "\\s*";
	public static final String WHITESPACE = "\\s+";

	public static final String EOL = "\\n+";
	public static final String EOF = "\\z";

	public static final String SEMICOLON = ";";
	public static final String PIPE = "\\|";
	public static final String TRAILING_PIPE = "\\|\n";
	public static final String SPACE = " ";
}
