package org.stoev.fuzzer;

import java.util.Random;
import java.util.HashMap;
import java.util.Scanner;
import java.util.EnumSet;
import java.util.Set;

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;

import org.stoev.fuzzer.Grammar.GrammarFlags;

public final class Context {
	private final Grammar grammar;
	private final Random random;
	private final Object visitor;

	private final HashMap<String, Sentence<?>> cachedRules = new HashMap<String, Sentence<?>>();

	private Context(final ContextBuilder builder) {
		grammar = builder.grammar;
		random = builder.random;
		visitor = builder.visitor;

		if (grammar != null && visitor != null) {
			grammar.registerVisitor(visitor);
		}
	}

	public static final class ContextBuilder {
		private Grammar grammar;
		private Random random = new Random(1);
		private Object visitor;

		public ContextBuilder grammar(final Grammar gr) {
			this.grammar = gr;
			return this;
		}

		public ContextBuilder grammar(final String grammarString) {
			this.grammar = new Grammar(new Scanner(grammarString), EnumSet.noneOf(GrammarFlags.class));
			return this;
		}

		public ContextBuilder grammar(final String grammarString, final Set<GrammarFlags> flags) {
			this.grammar = new Grammar(new Scanner(grammarString), flags);
			return this;
		}

		public ContextBuilder grammar(final File file) throws FileNotFoundException {
			this.grammar = new Grammar(new Scanner(file, "UTF-8"), EnumSet.noneOf(GrammarFlags.class));
			return this;
		}

		public ContextBuilder grammar(final File file, final Set<GrammarFlags> flags) throws FileNotFoundException {
			this.grammar = new Grammar(new Scanner(file, "UTF-8"), flags);
			return this;
		}

		public ContextBuilder grammar(final InputStream stream) {
			this.grammar = new Grammar(new Scanner(stream, "UTF-8"), EnumSet.noneOf(GrammarFlags.class));
			return this;
		}

		public ContextBuilder grammar(final InputStream stream, final Set<GrammarFlags> flags) {
			this.grammar = new Grammar(new Scanner(stream, "UTF-8"), flags);
			return this;
		}

		public ContextBuilder random(final Random r) {
			this.random = r;
			return this;
		}

		public ContextBuilder random(final int seed) {
			this.random = new Random(seed);
			return this;
		}

		public ContextBuilder visitor(final Object v) {
			this.visitor = v;
			return this;
		}

		public Context build() {
			return new Context(this);
		}
	}

	public void generate(final Sentence sentence) {
		sentence.populate(this, grammar);
	}

	public String generateString() {
		Sentence<String> sentence = new Sentence<String>();
		sentence.populate(this, grammar);
		return sentence.toString();
	}

	public int randomInt(final int n) {
		return random.nextInt(n);
	}

	public double randomDouble() {
		return random.nextDouble();
	}

	Grammar getGrammar() {
		return grammar;
	}

	Object getVisitor() {
		return visitor;
	}

	boolean shouldCacheRule(final String ruleName) {
		assert grammar != null;

		return grammar.shouldCacheRule(ruleName);
	}

	Sentence<?> getCachedValue(final String ruleName) {
		Sentence<?> cachedValue = cachedRules.get(ruleName);

		if (cachedValue == null) {
			throw new ConfigurationException("Cached value for rule " + ruleName + " requested, but not available.");
		}

		return cachedValue;
	}

	void setCachedValue(final String ruleName, final Sentence<?> value) {
		cachedRules.put(ruleName, value);
	}
}
