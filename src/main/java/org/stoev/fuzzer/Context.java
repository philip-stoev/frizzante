package org.stoev.fuzzer;

import java.util.Random;
import java.util.HashMap;

import java.io.IOException;

public class Context {
	private final Random random;
	private final String unitSeparator;
	private final Grammar grammar;
        private final HashMap<String, Sentence<?>> cachedRules = new HashMap<String, Sentence<?>>();
	private final Object visitor;

	Context(final Grammar gr) {
		grammar = gr;
		random = new Random(1);
		unitSeparator = "";
		visitor = null;
	}

	Context(final Grammar gr, final String us) {
		grammar = gr;
		random = new Random(1);
		unitSeparator = us;
		visitor = null;
	}

	Context(final Random rnd, final String us) {
		grammar = null;
		random = rnd;
		unitSeparator = us;
		visitor = null;
	}

	Context(final Grammar gr, final Random rnd, final String us) {
		grammar = gr;
		random = rnd;
		unitSeparator = us;
		visitor = null;
	}

	Context(final Grammar gr, final Object v) {
		grammar = gr;
		random = new Random(1);
		unitSeparator = "";
		visitor = v;
	}

	public final String generateString() throws IOException {
		Sentence<String> sentence = new Sentence<String>(unitSeparator);
		grammar.generate(this, sentence);
		return sentence.toString();
	}

	final int randomInt(final int n) {
		return random.nextInt(n);
	}

	final Object getVisitor() {
		return visitor;
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

	public final Sentence<?> getCachedValue(final String ruleName) {
		return cachedRules.get(ruleName);
	}

	public final void setCachedValue(final String ruleName, final Sentence<?> value) {
		cachedRules.put(ruleName, value);
	}
}
