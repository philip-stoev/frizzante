package org.stoev.fuzzer;

import java.io.IOException;

class GrammarLiteral implements Generatable {
	private final String value;

	GrammarLiteral(final String literalValue) {
		value = literalValue;
	}

	public void generate(final Context context, final Sentence<?> sentence) throws IOException {
		sentence.add(value);
	}

	public String toString() {
		return value;
	}

	public void compile(final Grammar grammar) {
		assert false;
	}
}
