package org.stoev.fuzzer;

import java.util.Random;
import java.util.HashMap;

public class Context {
	private final Random random;
	private final String unitSeparator;
	private final Grammar grammar;
        private final HashMap<String, String> cachedRules = new HashMap<String, String>();

	Context(final Random rnd, final String us) {
		grammar = null;
		random = rnd;
		unitSeparator = us;
	}

	Context(final Grammar gr, final Random rnd, final String us) {
		grammar = gr;
		random = rnd;
		unitSeparator = us;
	}

	final int randomInt(final int n) {
		return random.nextInt(n);
	}

	final void appendUnitSeparator(final StringBuilder buffer) {
		buffer.append(unitSeparator);
	}

	public final boolean shouldCacheRule(final String ruleName) {
		if (grammar == null) {
			return false;
		} else {
			return grammar.shouldCacheRule(ruleName);
		}
	}

	public final String getCachedValue(final String ruleName) {
		return cachedRules.get(ruleName);
	}

	public final void setCachedValue(final String ruleName, final String value) {
		cachedRules.put(ruleName, value);
	}
}
