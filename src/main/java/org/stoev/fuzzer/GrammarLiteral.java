package org.stoev.fuzzer;

class GrammarLiteral<T> implements Generatable<T> {
	private final String value;

	GrammarLiteral(final String lv) {
		assert lv != null;
		assert lv.length() > 0;

		value = lv;
	}

	public void generate(final ThreadContext<T> threadContext, final Sentence<T> sentence) {
		sentence.append(value);
	}

	public String toString() {
		return value;
	}

	public String getName() {
		return value;
	}

	public void compile(final Grammar<T> grammar) {
		assert false;
	}

	public boolean isConstant() {
		return true;
	}
}
