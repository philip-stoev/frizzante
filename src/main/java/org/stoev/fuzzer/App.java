package org.stoev.fuzzer;

public final class App {
/**
 * Not called.
 */
	private App() {

	}

/**
 * main().
 * @param args command-line arguments
 */
	public static void main(final String[] args) {

                Grammar grammar = new Grammar("main: foo , main | foo , foo ; foo: foo1 | foo2 ; foo2.java: { sentence.add(\"foo4\"); };");
		Context context = new Context.ContextBuilder(grammar).build();

		final long iterations = 10000000;
		final long millispernano = 1000000;

		long start = System.nanoTime();
		long preventOptimization = 0;

		for (int x = 1; x < iterations; x = x + 1) {
			String sentence = context.generateString();
			preventOptimization = preventOptimization + sentence.length();
		}

		long end = System.nanoTime();

		System.out.println("Benchmark took " + ((end - start) / millispernano) + " msecs. " + preventOptimization);
	}
}
