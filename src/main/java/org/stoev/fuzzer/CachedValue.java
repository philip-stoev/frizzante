package org.stoev.fuzzer;

class CachedValue implements Generatable {
	private final String ruleName;

	CachedValue(final String rn) {
		ruleName = rn;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		Sentence<?> cachedValue = context.getCachedValue(ruleName);
		sentence.addAll(cachedValue);
	}

	public String toString() {
		return ruleName + "_cached";
	}

	public String getName() {
		return ruleName + "_cached";
	}

	public void compile(final Grammar grammar) {
		assert false;
	}

	public boolean isConstant() {
		// TODO - determine isConstant() based on the value being cached
		return false;
	}
}
