package org.stoev.frizzante;

interface Generatable<T> {
	void generate(final ThreadContext<T> threadContext, final Sentence<T> sentence);

	void compile(Grammar<T> grammar);

	boolean isConstant();

	String getName();
}
