package org.stoev.fuzzer;

import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.HashMap;

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;

public final class Grammar implements Generatable {

	private static final String STARTING_GRAMMAR_RULE = "main";

	private static final String ANY_STRING = ".*?";
	private static final String SEMICOLON_AT_EOL = ";\\s*(\\n+|\\z)";
	private static final String JAVA_DOUBLE_OPENING_BRACES = "\\{\\{";
	private static final String JAVA_DOUBLE_CLOSING_BRACES_SEMICOLON_EOL = "\\}\\};(\\n|\\z)";

	private static final Pattern RULE_PATTERN = Pattern.compile(ANY_STRING + Constants.OPTIONAL_WHITESPACE + SEMICOLON_AT_EOL + Constants.OPTIONAL_WHITESPACE, Pattern.DOTALL);
	private static final Pattern JAVA_PATTERN = Pattern.compile(JAVA_DOUBLE_OPENING_BRACES + ANY_STRING + JAVA_DOUBLE_CLOSING_BRACES_SEMICOLON_EOL + Constants.OPTIONAL_WHITESPACE, Pattern.DOTALL);
	private static final String RULE_NAME_PATTERN = "[a-zA-Z0-9_. ]*:";
	private static final String JAVA_EXTENSION = ".java";

	private final HashMap<String, Generatable> rules = new HashMap<String, Generatable>();
	private final HashMap<String, Boolean> shouldCacheRule = new HashMap<String, Boolean>();

	Grammar(final String grammarString) {
		this(new Scanner(grammarString));
	}

	Grammar(final File file) throws FileNotFoundException {
		this(new Scanner(file, "UTF-8"));
	}

	Grammar(final InputStream stream) {
		this(new Scanner(stream, "UTF-8"));
	}

	private Grammar(final Scanner scanner) {
		scanner.useDelimiter("");

		while (true) {
			// Trim leading whitespace
			while (scanner.hasNext(Constants.WHITESPACE)) {
				scanner.next(Constants.WHITESPACE);
			}

			String generatableName = scanner.findWithinHorizon(RULE_NAME_PATTERN, 0);

			if (generatableName == null) {
				break;
			}

			// Trim trailing colon
			generatableName = generatableName.substring(0, generatableName.length() - 1);

			if (generatableName.contains(Constants.SPACE)) {
				throw new ConfigurationException("Rule name " + generatableName + " contains space.");
			}

			if (rules.containsKey(generatableName)) {
				throw new ConfigurationException("Name " + generatableName + " defined multiple times in grammar.");
			}

			Generatable generatableObject;

			if (generatableName.endsWith(JAVA_EXTENSION)) {
				generatableName = generatableName.substring(0, generatableName.length() - JAVA_EXTENSION.length());
				String javaString = scanner.findWithinHorizon(JAVA_PATTERN, 0);

				if (javaString == null) {
					throw new ConfigurationException("Unable to parse Java code for rule " + generatableName + " (missing '}};' terminator?)");
				}

				generatableObject = new InlineJava(generatableName, javaString);
			} else {
				String ruleString = scanner.findWithinHorizon(RULE_PATTERN, 0);

				if (ruleString == null) {
					throw new ConfigurationException("Unable to parse rule " + generatableName + " (missing ';' terminator?)");
				}

				generatableObject = new GrammarRule(generatableName, ruleString);
			}

			rules.put(generatableName, generatableObject);
		}

		compile(this);
	}

	public void compile(final Grammar grammar) {
		for (Generatable rule : rules.values()) {
			rule.compile(grammar);
		}
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		Generatable startingRule = rules.get(STARTING_GRAMMAR_RULE);

		if (startingRule == null) {
			throw new ConfigurationException("Grammar does not have a starting grammar rule named " + STARTING_GRAMMAR_RULE);
		}

		sentence.getStack().push(startingRule);
	}

	Generatable getRule(final String ruleName) {
		return rules.get(ruleName);
	}

	void setRuleCached(final String ruleName) {
		shouldCacheRule.put(ruleName, true);
	}

	boolean shouldCacheRule(final String ruleName) {
		return shouldCacheRule.containsKey(ruleName);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Generatable rule: rules.values()) {
			sb.append(rule.toString());
		}

		return sb.toString();
	}

	public String getName() {
		assert false;
		return "";
	}
}
