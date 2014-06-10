package org.stoev.fuzzer;

interface Generatable<T> {
	void generate(final Context<T> context, final Sentence<T> sentence);

	void compile(Grammar<T> grammar);

	boolean isConstant();

	String getName();
}
