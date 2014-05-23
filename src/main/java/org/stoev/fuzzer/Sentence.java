package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;

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

public class Sentence<T> implements Iterable<T>, Appendable {
	private final List<T> elements = new ArrayList<T>();
	private final Deque<Generatable> stack = new ArrayDeque<Generatable>();
	private final List<GrammarProduction> productions = new ArrayList<GrammarProduction>();

	/**
	Creates a new empty Sentence object of the same type.

	The caller could use sentence.getClass().newInstance() instead, however this will require
	catching exceptions. Our version is exception-free.
	**/
	public final Sentence<T> newInstance() {
		return new Sentence<T>();
	}

	/**
	Adds a new element to the Sentence

	@param element the element to be added.
	**/

	public final void add(final T element) {
		elements.add(element);
	}

	/**
	Adds all the elements from newSentence

	@param newSentence the Sentence to add elements from
	@throws ClassCastException if the Sentence being added is not compatible
	**/

	final void addAll(final Sentence<?> newSentence) {
		elements.addAll((List<T>) newSentence.elements);
	}

	/**
	Appends a string to the Sentence

	@param string the String to be appended
	@throws ClassCastException if the Sentence is not compatible with String
	**/

	public final void append(final String string) {
		elements.add((T) string);
	}

	/**
	Appends a CharSequence to the Sentence

	@param csq the CharSequence to be appended
	@throws ClassCastException if the Sentence is not compatible with String
	**/

	public final Appendable append(final CharSequence csq) throws IOException {
		elements.add((T) csq.toString());
		return this;
	}

	/**
	Not supported

	@throws UnsupportedOperationException
	**/

	public final Appendable append(final char c) throws IOException {
		throw new UnsupportedOperationException("Sentence does not support append(char r)");
	}

	/**
	Not supported

	@throws UnsupportedOperationException
	**/

	public final Appendable append(final CharSequence csq, final int start, final int end) throws IOException {
		throw new UnsupportedOperationException("Sentence does not support append(CharSequence csq, int start, int end)");
	}

	/**
	Returns an iterator over the elements of the Sentence
	**/

	public final Iterator<T> iterator() {
		assert stack.isEmpty() : "Stack was not empty at the time Iterator was accessed.";

		return elements.iterator();
	}

	/**
	Returns the Sentence as a string, possibly with seprators between the elements
	**/

	public final String toString() {

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

	final Deque<Generatable> getStack() {
		return stack;
	}

	final void registerProduction(final GrammarProduction production) {
		productions.add(production);
	}

	final void failed(final double penalty) {
		for (GrammarProduction production: productions) {
			production.demote(penalty);
		}
	}

	final void succeeded(final double promotion) {
		for (GrammarProduction production: productions) {
			production.promote(promotion);
		}
	}

}
