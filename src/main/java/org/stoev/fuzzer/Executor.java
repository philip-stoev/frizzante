package org.stoev.fuzzer;

public interface Executor<T> {
	int execute(final Sentence<T> sentence);
}
