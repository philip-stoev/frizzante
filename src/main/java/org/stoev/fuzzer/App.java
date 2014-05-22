package org.stoev.fuzzer;

import java.util.Map;
import java.util.HashMap;

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

                Grammar grammar = new Grammar("main: good1 | sometimes | bad1 ; sometimes: good2 | bad2 ;");
		Context context = new Context.ContextBuilder(grammar).build();

		final long iterations = 1000;
		final long cycles = 10;
		final float coefficient = 0.1f;

		for (int c = 1; c <= cycles; c = c + 1) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			for (int i = 1; i <= iterations; i = i + 1) {
				Sentence<String> sentence = new Sentence<String>();
				context.generate(sentence);
				String generated = sentence.toString();

				if (map.containsKey(generated)) {
					map.put(generated, map.get(generated) + 1);
				} else {
					map.put(generated, 1);
				}

				if (generated.contains("good")) {
					assert true;
					sentence.succeeded(coefficient);
				} else if (generated.contains("bad")) {
					sentence.failed(coefficient);
				}
			}

			System.out.println("Cycle: " + c);
			System.out.println(map.toString());
			System.out.println(grammar.toString());
			System.out.println();
		}
	}

	public static void mainBenchmark(final String[] args) {

                Grammar grammar = new Grammar("main: foo , main | foo , foo ; foo: foo1 | foo2 ; foo2.java: { sentence.add(\"foo4\"); };");
		Context context = new Context.ContextBuilder(grammar).build();

		final long iterations = 10000000;
		final long millispernano = 1000000;

		long start = System.nanoTime();
		long preventOptimization = 0;

		for (int x = 1; x <= iterations; x = x + 1) {
			String sentence = context.generateString();
			preventOptimization = preventOptimization + sentence.length();
		}

		long end = System.nanoTime();

		System.out.println("Benchmark took " + ((end - start) / millispernano) + " msecs. " + preventOptimization);
	}
}
