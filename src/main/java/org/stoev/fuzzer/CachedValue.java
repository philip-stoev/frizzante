package org.stoev.fuzzer;

import java.io.IOException;

class CachedValue implements Generatable {
	private final String ruleName;

	CachedValue(final String rn) {
		ruleName = rn;
	}

	public void generate(final Context context, final Sentence<?> sentence) throws IOException {
		Sentence<?> cachedSentenceFragment = context.getCachedValue(ruleName);
		if (cachedSentenceFragment != null) {
			sentence.addAll(cachedSentenceFragment);
		}
	}

	public String toString() {
		return "$" + ruleName;
	}

	public void compile(final Grammar grammar) {
		assert false;
	}
}
