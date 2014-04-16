package org.stoev.fuzzer;

import java.util.Scanner;
import java.util.HashMap;

public class Grammar implements Generatable {

	private static final String STARTING_GRAMMAR_RULE = "main";

	private static final String RULE_SEPARATION_PATTERN = "\\s*;\\s*";
	private static final String JAVA_PATTERN = ".*}\\s*;\\s*";
	private static final String RULE_NAME_SEPARATION_PATTERN = "\\s*:\\s*";
	private static final String JAVA_EXTENSION = ".java";

	private final HashMap<String, Generatable> rules = new HashMap<String, Generatable>();
	private final HashMap<String, Boolean> shouldCacheRule = new HashMap<String, Boolean>();

	Grammar(final String grammarString) {
		Scanner scanner = new Scanner(grammarString);

		while (scanner.hasNext()) {
			scanner.useDelimiter(RULE_NAME_SEPARATION_PATTERN);
			String generatableName = scanner.next();
			scanner.skip(RULE_NAME_SEPARATION_PATTERN);

			if (generatableName.endsWith(JAVA_EXTENSION)) {
				String javaName = generatableName.substring(0, generatableName.length() - JAVA_EXTENSION.length());
				String javaString = scanner.next(JAVA_PATTERN);
				rules.put(javaName, new JavaCode(javaName, javaString));
			} else {
				scanner.useDelimiter(RULE_SEPARATION_PATTERN);
				String ruleString = scanner.next();
				scanner.skip(RULE_SEPARATION_PATTERN);
				rules.put(generatableName, new GrammarRule(generatableName, ruleString));
			}
		}

		link(this);
	}

	public final void link(final Grammar grammar) {
		for (Generatable rule : rules.values()) {
			rule.link(grammar);
		}
	}

	public final void generate(final Context context, final StringBuilder buffer) {
		Generatable mainRule = rules.get(STARTING_GRAMMAR_RULE);

		if (mainRule != null) {
			mainRule.generate(context, buffer);
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
