package org.stoev.fuzzer;

import java.util.Random;
import java.util.Scanner;
import java.util.EnumSet;
import java.util.Set;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;

import org.stoev.fuzzer.Grammar.GrammarFlags;

public final class GlobalContext<T> {
	private final Grammar<T> grammar;

	private final Random random;
	private final Long randomSeed;

	private final Object visitor;

	private final long idRangeStart;
	private final long idRangeLength;

	private FuzzRunnableFactory runnableFactory;
	private int threadCount;
	private int duration;
	private long count;

	private GlobalContext(final ContextBuilder<T> builder) {
		grammar = builder.grammar;

		random = builder.random;
		randomSeed = builder.randomSeed;
		visitor = builder.visitor;

		idRangeStart = builder.idRangeStart;
		idRangeLength = builder.idRangeLength;

		runnableFactory = builder.runnableFactory;
		threadCount = builder.threadCount;
		duration = builder.duration;
		count = builder.count;

		if (grammar != null && visitor != null) {
			grammar.registerVisitor(visitor);
		}
	}

	public static final class ContextBuilder<T> {
		private Grammar<T> grammar;
		private Long randomSeed = Long.valueOf(1L);
		private Random random = new Random(randomSeed);
		private Object visitor;
		private long idRangeStart = 0;
		private long idRangeLength = Long.MAX_VALUE - 1;

		private FuzzRunnableFactory runnableFactory = null;
		private int threadCount = 1;
		private int duration = Integer.MAX_VALUE;
		private long count = Long.MAX_VALUE;

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
			this.grammar(file, EnumSet.noneOf(GrammarFlags.class));
			return this;
		}

		public ContextBuilder<T> grammar(final File file, final Set<GrammarFlags> flags) {
			try {
				this.grammar = new Grammar<T>(file, flags);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
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

		public ContextBuilder<T> random(final Random random) {
			this.randomSeed = null;
			this.random = random;
			return this;
		}

		public ContextBuilder<T> random(final long seed) {
			this.randomSeed = Long.valueOf(seed);
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

		public ContextBuilder<T> runnableFactory(final FuzzRunnableFactory runnableFactory) {
			this.runnableFactory = runnableFactory;
			return this;
		}

		public ContextBuilder<T> threads(final int threadCount) {
			this.threadCount = threadCount;
			return this;
		}

		public ContextBuilder<T> duration(final int duration) {
			this.duration = duration;
			return this;
		}

		public ContextBuilder<T> count(final long count) {
			this.count = count;
			return this;
		}

		public GlobalContext<T> build() {
			return new GlobalContext<T>(this);
		}
	}

	public Sentence<T> sentenceFromId(final long id) {
		Sentence<T> sentence = Sentence.newSentence(id);
		return sentence;
	}

	public ThreadContext<T> newThreadContext(final int id) {
                ThreadContext<T> threadContext = ThreadContext.newThreadContext(this, id);
                return threadContext;
        }


	public void run() throws Exception {
		if (runnableFactory == null) {
			throw new IllegalArgumentException("Global context has no runnable, so can not call run() on it.");
		}

		RunnableManager manager = new RunnableManager(this);
		manager.run();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Threads: ");
		sb.append(threadCount);
		sb.append("\n");

		sb.append("Global random seed: ");
		sb.append(randomSeed);
		sb.append("\n");

		sb.append("Grammar:\n");
		sb.append(grammar.toString());
		sb.append("\n");

		return sb.toString();
	}

	long getIdRangeStart() {
		return idRangeStart;
	}

	long getIdRangeLength() {
		return idRangeLength;
	}

	FuzzRunnableFactory getRunnableFactory() {
		return runnableFactory;
	}

	int getThreadCount() {
		return threadCount;
	}

	long getCount() {
		return count;
	}

	int getDuration() {
		return duration;
	}

	Grammar<T> getGrammar() {
		return grammar;
	}

	Object getVisitor() {
		return visitor;
	}

	Random getRandom() {
		return random;
	}
}
