package org.stoev.fuzzer;

import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.EnumSet;

import java.io.File;
import java.io.FileNotFoundException;

import java.lang.reflect.Method;

import org.stoev.fuzzer.Grammar.GrammarOptions;

public final class Grammar<T> implements Generatable<T> {

	static enum GrammarOptions {
		STANDALONE_SEMICOLONS,
		SKIP_WHITESPACE,
		TRAILING_PIPES
	};

	private static final String STARTING_GRAMMAR_RULE = "main";

	private static final String ANY_STRING = ".*?";
	private static final String SEMICOLON_AT_EOL = ";\\s*(\\n+|\\z)";
	private static final String STANDALONE_SEMICOLON = "\n;\\s*(\\n+|\\z)";
	private static final String JAVA_DOUBLE_OPENING_BRACES = "\\{\\{";
	private static final String JAVA_DOUBLE_CLOSING_BRACES_SEMICOLON_EOL = "\\}\\};(\\n|\\z)";

	private static final Pattern JAVA_PATTERN = Pattern.compile(JAVA_DOUBLE_OPENING_BRACES + ANY_STRING + JAVA_DOUBLE_CLOSING_BRACES_SEMICOLON_EOL + Constants.OPTIONAL_WHITESPACE, Pattern.DOTALL);
	private static final String RULE_NAME_PATTERN = "[a-zA-Z0-9_. ]*:";
	private static final String JAVA_EXTENSION = ".java";

	private File file;
	private final Set<GrammarOptions> options = EnumSet.noneOf(GrammarOptions.class);

	private final Map<String, Generatable<T>> rules = new HashMap<String, Generatable<T>>();
	private final Map<String, Boolean> shouldCacheRule = new HashMap<String, Boolean>();

	Grammar(final File file) throws FileNotFoundException {
		this(new Scanner(file, "UTF-8"));
		this.file = file;
	}

	Grammar(final Scanner scanner) {
		parse(scanner);
	}


	private void parse(final Scanner scanner) {
		scanner.reset();
		scanner.useDelimiter("");

		while (true) {
			// Trim comments and process includes
			while (scanner.hasNext(Constants.COMMENT)) {
				String commentLine = scanner.nextLine();

				if (commentLine.startsWith(Constants.INCLUDE)) {
					if (commentLine.length() < Constants.INCLUDE.length() + 1) {
						throw new IllegalArgumentException("Malformed #include directive.");
					}

					String includeFile = commentLine.substring(Constants.INCLUDE.length() + 1).trim();

					try {
						parse(new Scanner(new File(includeFile), "UTF-8"));
					} catch (FileNotFoundException fileNotFoundException) {
						throw new IllegalArgumentException(fileNotFoundException);
					}
				}

				if (commentLine.startsWith(Constants.OPTION)) {
					String optionName = commentLine.substring(Constants.OPTION.length() + 1).trim();
					options.add(GrammarOptions.valueOf(optionName));
				}
			}

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
				throw new IllegalArgumentException("Rule name " + generatableName + " contains space.");
			}

			if (rules.containsKey(generatableName)) {
				throw new IllegalArgumentException("Name " + generatableName + " defined multiple times in grammar.");
			}

			final Pattern rulePattern;

			// We use Pattern.DOTALL here because grammar rules are allowed to span multiple rows

			if (options.contains(GrammarOptions.STANDALONE_SEMICOLONS)) {
				rulePattern = Pattern.compile(ANY_STRING + Constants.OPTIONAL_WHITESPACE + STANDALONE_SEMICOLON + Constants.OPTIONAL_WHITESPACE, Pattern.DOTALL);
			} else {
				rulePattern = Pattern.compile(ANY_STRING + Constants.OPTIONAL_WHITESPACE + SEMICOLON_AT_EOL + Constants.OPTIONAL_WHITESPACE, Pattern.DOTALL);
			}

			Generatable<T> generatableObject;

			if (generatableName.endsWith(JAVA_EXTENSION)) {
				generatableName = generatableName.substring(0, generatableName.length() - JAVA_EXTENSION.length());
				String javaString = scanner.findWithinHorizon(JAVA_PATTERN, 0);

				if (javaString == null) {
					throw new IllegalArgumentException("Unable to parse Java code for rule " + generatableName + " (missing '}};' terminator?)");
				}

				generatableObject = new InlineJava<T>(generatableName, javaString);
			} else {
				String ruleString = scanner.findWithinHorizon(rulePattern, 0);

				if (ruleString == null) {
					throw new IllegalArgumentException("Unable to parse rule " + generatableName + " (missing ';' terminator?)");
				}

				generatableObject = new GrammarRule<T>(generatableName, ruleString, options);
			}

			rules.put(generatableName, generatableObject);
		}

		compile(this);
	}

	public void compile(final Grammar<T> grammar) {
		for (Generatable<T> rule : rules.values()) {
			rule.compile(grammar);
		}
	}

	public void generate(final ThreadContext<T> threadContext, final Sentence<T> sentence) {
		Generatable<T> startingRule = rules.get(STARTING_GRAMMAR_RULE);

		if (startingRule == null) {
			throw new IllegalArgumentException("Grammar does not have a starting grammar rule named " + STARTING_GRAMMAR_RULE);
		}

		sentence.pushGeneratable(startingRule);
	}

	void registerVisitor(final Object visitor) {
		Class<?> visitorClass = visitor.getClass();

		Method[] methods = visitorClass.getDeclaredMethods();

		for (Method method: methods) {
			String methodName = method.getName();
			Generatable<T> existingGeneratable = rules.get(methodName);
			Generatable<T> javaVisitor = new JavaVisitor<T>(visitor, methodName, existingGeneratable);
			rules.put(methodName, javaVisitor);
		}

		compile(this);
	}

	Generatable<T> getRule(final String ruleName) {
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

		if (file != null) {
			sb.append("Grammar file: ");
			sb.append(file.getAbsolutePath());
		} else {
			sb.append("Grammar:\n");
			sb.append(getGrammarString());
		}

		return sb.toString();
	}

	public String getGrammarString() {
		StringBuilder sb = new StringBuilder();

		for (Generatable<T> rule: rules.values()) {
			sb.append(rule.toString());
		}

		return sb.toString();
	}

	public String getName() {
		assert false;
		return "";
	}

	public boolean isConstant() {
		assert false;
		return false;
	}
}
