package org.stoev.fuzzer;

class GrammarLiteral implements Generatable {
	private final String value;

	GrammarLiteral(final String lv) {
		value = lv;
		assert value != null;
		assert value.length() > 0;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		sentence.add(value);
	}

	public String toString() {
		return value;
	}

	public void compile(final Grammar grammar) {
		assert false;
	}
}
