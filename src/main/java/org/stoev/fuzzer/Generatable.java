package org.stoev.fuzzer;

public interface Generatable {
	void generate(Context context, StringBuilder buffer);

	void link(Grammar grammar);
}
