package org.stoev.fuzzer;

import java.util.Random;
import java.util.HashMap;

public final class ThreadContext<T> {
	private final GlobalContext<T> globalContext;
	private final long randomSeed;
	private final Random random;
	private final int contextId;

	private final HashMap<String, Sentence<T>> ruleCache = new HashMap<String, Sentence<T>>();

	public static <T> ThreadContext<T> newThreadContext(final GlobalContext<T> globalContext, final int contextId) {
                return new ThreadContext<T>(globalContext, contextId);
        }

	private ThreadContext(final GlobalContext<T> globalContext, final int contextId) {
		this.globalContext = globalContext;
		this.randomSeed = globalContext.getRandom().nextLong();
		this.random = new Random(randomSeed);
		this.contextId = contextId;
	}

	public Sentence<T> newSentence() {
		Sentence<T> sentence = Sentence.newSentence(getNewId());
		return sentence;
	}

	public Sentence<T> generateSentence() {
		Sentence<T> sentence = newSentence();
		sentence.populate(this, globalContext.getGrammar());
		return sentence;
	}

	public void generate(final Sentence<T> sentence) {
		sentence.populate(this, globalContext.getGrammar());
	}

	public String generateString() {
		Sentence<T> sentence = newSentence();
		sentence.populate(this, globalContext.getGrammar());
		return sentence.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Thread random seed: ");
		sb.append(randomSeed);
		sb.append("\n");

		return sb.toString();
	}

	public Random getRandom() {
		return random;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public int getContextId() {
		return contextId;
	}

	public GlobalContext<T> getGlobalContext() {
		return globalContext;
	}

	long getNewId() {
		// Return an ID between idRangeStart and (idRangeStart + idRangeLength) inclusive
		long newId = globalContext.getIdRangeStart() + (long) (random.nextDouble() * (globalContext.getIdRangeLength() + 1));

		assert newId >= globalContext.getIdRangeStart();
		assert newId <= globalContext.getIdRangeStart() + globalContext.getIdRangeLength();

		return newId;
	}

	Sentence<T> getCachedValue(final String ruleName) {
		Sentence<T> cachedValue = ruleCache.get(ruleName);

		if (cachedValue == null) {
			throw new IllegalArgumentException("Cached value for rule '" + ruleName + "' was requested, but not available.");
		}

		return cachedValue;
	}

	void setCachedValue(final String ruleName, final Sentence<T> value) {
		ruleCache.put(ruleName, value);
	}
}
