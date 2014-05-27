package org.stoev.fuzzer;

class GrammarFencepost implements Generatable {
	private final ProductionUse productionUse;

	GrammarFencepost(final ProductionUse p) {
		productionUse = p;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		sentence.leaveProduction(productionUse);
	}

	public String toString() {
		return "";
	}

	public String getName() {
		return "";
	}

	public void compile(final Grammar grammar) {
		assert false;
	}
}
