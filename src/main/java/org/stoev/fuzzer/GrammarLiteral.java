package org.stoev.fuzzer;

class GrammarLiteral implements Generatable {
	private final String value;

	GrammarLiteral(final String lv) {
		assert lv != null;
		assert lv.length() > 0;
		assert !lv.matches(Constants.WHITESPACE);

		value = lv;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		sentence.append(value);
	}

	public String toString() {
		return value;
	}

	public String getName() {
		return value;
	}

	public void compile(final Grammar grammar) {
		assert false;
	}
}
