package org.stoev.fuzzer;

import java.util.Random;
import java.util.HashMap;
import java.util.Deque;

import java.lang.reflect.Method;

public final class Context {
	private final Grammar grammar;
	private final Random random;
	private final Object visitor;

        private final HashMap<String, Sentence<?>> cachedRules = new HashMap<String, Sentence<?>>();
	private final HashMap<String, Method> cachedVisitors = new HashMap<String, Method>();

	private Context(final ContextBuilder builder) {
		this.grammar = builder.grammar;
		this.random = builder.random;
		this.visitor = builder.visitor;
	}

	public static class ContextBuilder {
		private final Grammar grammar;
		private Random random = new Random(1);
		private Object visitor;

		public ContextBuilder(final Grammar gr) {
			this.grammar = gr;
		}

		public final ContextBuilder random(final Random r) {
			this.random = r;
			return this;
		}

		public final ContextBuilder random(final int seed) {
			this.random = new Random(seed);
			return this;
		}

		public final ContextBuilder visitor(final Object v) {
			this.visitor = v;
			return this;
		}

		public final Context build() {
			return new Context(this);
		}
	}

	public void generate(final Sentence sentence) {
		Deque<Generatable> stack = sentence.getStack();
		stack.push(grammar);

		while (!stack.isEmpty()) {
			stack.pop().generate(this, sentence);
		}
	}

	public String generateString() {
		Sentence<String> sentence = new Sentence<String>();
		generate(sentence);
		return sentence.toString();
	}

	public int randomInt(final int n) {
		return random.nextInt(n);
	}

	public double randomDouble() {
		return random.nextDouble();
	}

	Object getVisitor() {
		return visitor;
	}

	boolean shouldCacheRule(final String ruleName) {
		assert grammar != null;

		return grammar.shouldCacheRule(ruleName);
	}

	Sentence<?> getCachedValue(final String ruleName) {
		return cachedRules.get(ruleName);
	}

	void setCachedValue(final String ruleName, final Sentence<?> value) {
		cachedRules.put(ruleName, value);
	}

	Method getCachedVisitor(final String methodName) {
		return cachedVisitors.get(methodName);
	}

	void setCachedVisitor(final String methodName, final Method methodObject) {
		cachedVisitors.put(methodName, methodObject);
	}
}
