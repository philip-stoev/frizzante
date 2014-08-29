package org.stoev.frizzante;

final class Constants {
	private Constants() {
		assert false;
	}

	// Constants for regular expressions
	static final String OR = "|";

	static final String OPTIONAL_WHITESPACE = "\\s*";
	static final String WHITESPACE = "\\s+";
	static final String COMMENT = "#";
	static final String INCLUDE = "#include";
	static final String OPTION = "#option";

	static final String EOL = "\\n+";
	static final String EOF = "\\z";

	static final String SEMICOLON = ";";
	static final String PIPE = "\\|";
	static final String TRAILING_PIPE = "\\|\n";
	static final String SPACE = " ";
}
