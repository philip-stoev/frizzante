package org.stoev.fuzzer;

import java.util.Deque;

class GrammarLiteral implements Generatable {
	private final String value;

	GrammarLiteral(final String literalValue) {
		value = literalValue;
	}

	public void generate(final Context context, final Sentence<?> sentence, final Deque<Generatable> stack) {
		sentence.add(value);
	}

	public String toString() {
		return value;
	}

	public void compile(final Grammar grammar) {
		assert false;
	}
}
