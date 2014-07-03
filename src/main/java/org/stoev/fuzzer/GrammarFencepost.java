package org.stoev.fuzzer;

class GrammarFencepost<T> implements Generatable<T> {
	private final ProductionInstance<T> productionInstance;

	GrammarFencepost(final ProductionInstance<T> p) {
		productionInstance = p;
	}

	public void generate(final ThreadContext<T> threadContext, final Sentence<T> sentence) {
		sentence.leaveProduction(productionInstance);
	}

	public String toString() {
		assert false;
		return "";
	}

	public String getName() {
		assert false;
		return "";
	}

	public void compile(final Grammar<T> grammar) {
		assert false;
	}

	public boolean isConstant() {
		assert false;
		return true;
	}
}
