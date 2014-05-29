package org.stoev.fuzzer;

interface Generatable {
	void generate(final Context context, final Sentence<?> sentence);

	void compile(Grammar grammar);

	boolean isConstant();

	String getName();
}
