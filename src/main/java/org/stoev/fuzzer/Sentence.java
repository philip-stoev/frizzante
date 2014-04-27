package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;

public class Sentence<T> implements Iterable<T> {
	private final List<T> elements = new ArrayList<T>();
	private final Deque<Generatable> stack = new ArrayDeque<Generatable>();

	private final String separator;

// Construct

	public Sentence() {
		separator = null;
	}

	public Sentence(final String s) {
		separator = s;
	}

// Put stuff in

        @SuppressWarnings("unchecked")
	public final void add(final Object newElement) {
		elements.add((T) newElement);
	}

        @SuppressWarnings("unchecked")
	final void addAll(final Sentence<?> newSentence) {
		elements.addAll((List<T>) newSentence.elements);
	}

// Take stuff out

	final Deque<Generatable> getStack() {
		return stack;
	}

	public final Iterator<T> iterator() {
		return elements.iterator();
	}

	public final String toString() {
		assert stack.isEmpty() : "Stack was not empty at the time toString() was called.";

		if (elements.size() == 0) {
			return "";
		} else {
			StringBuilder builder = new StringBuilder();
			Iterator<T> iterator = elements.iterator();

			builder.append(iterator.next().toString());

			while (iterator.hasNext()) {
				if (separator != null) {
					builder.append(separator);
				}
				builder.append(iterator.next().toString());
			}

			return builder.toString();
		}
	}
}
