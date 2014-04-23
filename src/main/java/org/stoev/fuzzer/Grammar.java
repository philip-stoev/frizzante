package org.stoev.fuzzer;

import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.HashMap;

import java.io.IOException;

public class Grammar implements Generatable {

	private static final String STARTING_GRAMMAR_RULE = "main";

	private static final Pattern RULE_PATTERN = Pattern.compile(".*?\\s*;\\s*");
	private static final Pattern JAVA_PATTERN = Pattern.compile(".*?};\\s*");
	private static final Pattern RULE_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_.]*:");
	private static final String JAVA_EXTENSION = ".java";

	private final HashMap<String, Generatable> rules = new HashMap<String, Generatable>();
	private final HashMap<String, Boolean> shouldCacheRule = new HashMap<String, Boolean>();

	Grammar(final String grammarString) {
		Scanner scanner = new Scanner(grammarString);

		while (scanner.hasNext()) {
			String generatableName = scanner.findWithinHorizon(RULE_NAME_PATTERN, grammarString.length());
			generatableName = generatableName.substring(0, generatableName.length() - 1);

			Generatable generatableObject;

			if (generatableName.endsWith(JAVA_EXTENSION)) {
				generatableName = generatableName.substring(0, generatableName.length() - JAVA_EXTENSION.length());
				String javaString = scanner.findWithinHorizon(JAVA_PATTERN, grammarString.length());
				generatableObject = new JavaCode(generatableName, javaString);
			} else {
				String ruleString = scanner.findWithinHorizon(RULE_PATTERN, grammarString.length());
				generatableObject = new GrammarRule(generatableName, ruleString);
			}

			if (rules.containsKey(generatableName)) {
				throw new ConfigurationException("Name " + generatableName + " defined multiple times in grammar.");
			} else {
				rules.put(generatableName, generatableObject);
			}
		}

		compile(this);
	}

	public final void compile(final Grammar grammar) {
		for (Generatable rule : rules.values()) {
			rule.compile(grammar);
		}
	}

	public final void generate(final Context context, final Sentence<?> sentence) throws IOException {
		Generatable mainRule = rules.get(STARTING_GRAMMAR_RULE);

		if (mainRule != null) {
			mainRule.generate(context, sentence);
		} else {
			throw new ConfigurationException("Grammar does not have a starting grammar rule named " + STARTING_GRAMMAR_RULE);
		}
	}

	public final Generatable getRule(final String ruleName) {
		return rules.get(ruleName);
	}

	public final Generatable getRule(final Generatable generatable) {
		return rules.get(generatable.toString());
	}

	public final void setRuleCached(final String ruleName) {
		shouldCacheRule.put(ruleName, true);
	}

	public final boolean shouldCacheRule(final String ruleName) {
		return shouldCacheRule.containsKey(ruleName);
	}

	public final String toString() {
		assert false;
		return "foo";
	}
}
