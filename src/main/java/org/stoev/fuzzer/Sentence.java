package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Random;

import java.io.IOException;

/**
 * This class holds the output from a random generation run.
 *
 * Usually, a String Sentence is used:
 * <pre>
 * {@code
 * Sentence<String> sentence = new Sentence<String>();
 * ... perform generation
 * System.out.println(sentence.toString());
 * }
 * </pre>
 * Alternatively, the Sentence can be composed of objects from some other type:
 * <pre>
 * {@code
 * Sentence<Long> sentence = new Sentence<Long>();
 * ... perform generation
 * for (Long element: sentence) {
 * ... process each element
 * }
 * }
 * </pre>
 *
 * @author Philip Stoev <philip [at] stoev.org>
**/

public final class Sentence<T> implements Iterable<T>, Appendable {
	private final List<T> elements = new ArrayList<T>();
	private final Deque<Generatable> generatableStack = new ArrayDeque<Generatable>();
	private final List<ProductionUse> productionUseList = new ArrayList<ProductionUse>();

	private final long id;
	private final Random random;

	public static <S> Sentence<S> newSentence(final long i) {
		return new Sentence<S>(i);
	}

	private Sentence(final long i) {
		id = i;
		random = new Random(id);
	}

	public int randomInt(final int n) {
		return random.nextInt(n);
	}

	public double randomDouble() {
		return random.nextDouble();
	}

	/**
	Creates a new empty Sentence object of the same type.

	The caller could use sentence.getClass().newInstance() instead, however this will require
	catching exceptions. Our version is exception-free.
	**/
	public Sentence<T> newInstance() {
		return new Sentence<T>(id);
	}

	/**
	Adds a new element to the Sentence

	@param element the element to be added.
	**/

	public void add(final T element) {
		elements.add(element);
	}

	/**
	Adds all the elements from newSentence

	@param newSentence the Sentence to add elements from
	@throws ClassCastException if the Sentence being added is not compatible
	**/

	void addAll(final Sentence<?> newSentence) {
		elements.addAll((List<T>) newSentence.elements);
	}

	/**
	Appends a string to the Sentence

	@param string the String to be appended
	@throws ClassCastException if the Sentence is not compatible with String
	**/

	public void append(final String string) {
		elements.add((T) string);
	}

	/**
	Appends a CharSequence to the Sentence

	@param csq the CharSequence to be appended
	@throws ClassCastException if the Sentence is not compatible with String
	**/

	public Appendable append(final CharSequence csq) throws IOException {
		elements.add((T) csq.toString());
		return this;
	}

	/**
	Not supported

	@throws UnsupportedOperationException
	**/

	public Appendable append(final char c) throws IOException {
		throw new UnsupportedOperationException("Sentence does not support append(char r)");
	}

	/**
	Not supported

	@throws UnsupportedOperationException
	**/

	public Appendable append(final CharSequence csq, final int start, final int end) throws IOException {
		throw new UnsupportedOperationException("Sentence does not support append(CharSequence csq, int start, int end)");
	}

	/**
	Returns an iterator over the elements of the Sentence
	**/

	public Iterator<T> iterator() {
		assert generatableStack.isEmpty() : "Generatable stack was not empty at the time Iterator was accessed.";

		return elements.iterator();
	}

	/**
	Returns the Sentence as a string, possibly with seprators between the elements
	**/

	public String toString() {
		if (elements.size() == 0) {
			return "";
		} else {
			StringBuilder builder = new StringBuilder();
			Iterator<T> iterator = iterator();

			builder.append(iterator.next());

			while (iterator.hasNext()) {
				builder.append(iterator.next());
			}

			return builder.toString();
		}
	}

	void populate(final Context context, final Generatable startingGeneratable) {
		assert generatableStack.size() == 0;

		generatableStack.push(startingGeneratable);

		while (!generatableStack.isEmpty()) {
			Generatable generatable = generatableStack.pop();
			generatable.generate(context, this);
		}
	}

	void pushGeneratable(final Generatable generatable) {
		generatableStack.push(generatable);
	}

	void enterProduction(final GrammarProduction production) {
		ProductionUse productionUse = new ProductionUse(production, elements.size());
		productionUseList.add(productionUse);

		GrammarFencepost grammarFencepost = new GrammarFencepost(productionUse);
		generatableStack.push(grammarFencepost);
	}

	void leaveProduction(final ProductionUse productionUse) {
		productionUse.setEnd(elements.size() - 1);
	}

	public List<T> getElements() {
		return elements;
	}

	public long getId() {
		return id;
	}

	public int size() {
		return elements.size();
	}

	public List<ProductionUse> getProductionsUsed() {
		return productionUseList;
	}

	void failed(final double penalty) {
		for (ProductionUse productionUse: productionUseList) {
			GrammarProduction production = productionUse.getProduction();
			production.demote(penalty);
		}
	}

	void succeeded(final double promotion) {
		for (ProductionUse productionUse: productionUseList) {
			GrammarProduction production = productionUse.getProduction();
			production.promote(promotion);
		}
	}
}

