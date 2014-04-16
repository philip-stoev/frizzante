package org.stoev.fuzzer;

class Literal implements Generatable {
	private final String value;

	Literal(final String literalValue) {
		value = literalValue;
	}

	public void generate(final Context context, final StringBuilder buffer) {
		buffer.append(value);
	}

	public String toString() {
		return value;
	}

	public void link(final Grammar grammar) {
		assert false;
	}
}
