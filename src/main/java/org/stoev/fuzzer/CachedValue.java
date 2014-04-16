package org.stoev.fuzzer;

class CachedValue implements Generatable {
	private final String ruleName;

	CachedValue(final String rn) {
		ruleName = rn;
	}

	public void generate(final Context context, final StringBuilder buffer) {
		String value = context.getCachedValue(ruleName);
		if (value != null) {
			buffer.append(value);
		}
	}

	public String toString() {
		return "$" + ruleName;
	}

	public void link(final Grammar grammar) {
		assert false;
	}
}
