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

public final class Context<T> {
	private final Grammar<T> grammar;
	private final Random random;
	private final Object visitor;
	private long idRangeStart, idRangeLength;

	private final HashMap<String, Sentence<T>> cachedRules = new HashMap<String, Sentence<T>>();

	private Context(final ContextBuilder<T> builder) {
		grammar = builder.grammar;
		random = builder.random;
		visitor = builder.visitor;

		idRangeStart = builder.idRangeStart;
		idRangeLength = builder.idRangeLength;

		if (grammar != null && visitor != null) {
			grammar.registerVisitor(visitor);
		}
	}

	public static final class ContextBuilder<T> {
		private Grammar<T> grammar;
		private Random random = new Random(1);
		private Object visitor;
		private long idRangeStart = 0;
		private long idRangeLength = Long.MAX_VALUE;

		public ContextBuilder<T> grammar(final Grammar<T> gr) {
			this.grammar = gr;
			return this;
		}

		public ContextBuilder<T> grammar(final String grammarString) {
			this.grammar = new Grammar<T>(new Scanner(grammarString), EnumSet.noneOf(GrammarFlags.class));
			return this;
		}

		public ContextBuilder<T> grammar(final String grammarString, final Set<GrammarFlags> flags) {
			this.grammar = new Grammar<T>(new Scanner(grammarString), flags);
			return this;
		}

		public ContextBuilder<T> grammar(final File file) throws FileNotFoundException {
			this.grammar = new Grammar<T>(new Scanner(file, "UTF-8"), EnumSet.noneOf(GrammarFlags.class));
			return this;
		}

		public ContextBuilder<T> grammar(final File file, final Set<GrammarFlags> flags) throws FileNotFoundException {
			this.grammar = new Grammar<T>(new Scanner(file, "UTF-8"), flags);
			return this;
		}

		public ContextBuilder<T> grammar(final InputStream stream) {
			this.grammar = new Grammar<T>(new Scanner(stream, "UTF-8"), EnumSet.noneOf(GrammarFlags.class));
			return this;
		}

		public ContextBuilder<T> grammar(final InputStream stream, final Set<GrammarFlags> flags) {
			this.grammar = new Grammar<T>(new Scanner(stream, "UTF-8"), flags);
			return this;
		}

		public ContextBuilder<T> random(final Random r) {
			this.random = r;
			return this;
		}

		public ContextBuilder<T> random(final int seed) {
			this.random = new Random(seed);
			return this;
		}

		public ContextBuilder<T> visitor(final Object v) {
			this.visitor = v;
			return this;
		}

		public ContextBuilder<T> idRange(final long start, final long length) {
			this.idRangeStart = start;
			this.idRangeLength = length;
			return this;
		}

		public Context<T> build() {
			return new Context<T>(this);
		}
	}

	public Sentence<T> newSentence() {
		Sentence<T> sentence = Sentence.newSentence(getNewId());
		return sentence;
	}

	public Sentence<T> sentenceFromId(final long id) {
		Sentence<T> sentence = Sentence.newSentence(id);
		return sentence;
	}

	public void generate(final Sentence<T> sentence) {
		sentence.populate(this, grammar);
	}

	public String generateString() {
		Sentence<T> sentence = newSentence();
		sentence.populate(this, grammar);
		return sentence.toString();
	}

	public void setIdRange(final long start, final long length) {
                        this.idRangeStart = start;
			this.idRangeLength = length;
	}

	long getNewId() {
		// Return an ID between idRangeStart and (idRangeStart + idRangeLength) inclusive
		return idRangeStart + (long) (random.nextDouble() * (idRangeLength + 1));
	}

	Grammar<T> getGrammar() {
		return grammar;
	}

	Object getVisitor() {
		return visitor;
	}

	boolean shouldCacheRule(final String ruleName) {
		assert grammar != null;

		return grammar.shouldCacheRule(ruleName);
	}

	Sentence<T> getCachedValue(final String ruleName) {
		Sentence<T> cachedValue = cachedRules.get(ruleName);

		if (cachedValue == null) {
			throw new ConfigurationException("Cached value for rule " + ruleName + " requested, but not available.");
		}

		return cachedValue;
	}

	void setCachedValue(final String ruleName, final Sentence<T> value) {
		cachedRules.put(ruleName, value);
	}
}
