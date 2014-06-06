package org.stoev.fuzzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.BitSet;
import java.util.Iterator;

import java.util.Deque;
import java.util.ArrayDeque;

public final class StringMinimizer implements Iterable<String> {
	private Iterator<String> iterator;

	private final String delimiter;

	private final String initialString;
	private final List<String> initialElements = new ArrayList<String>();

	private Interval currentInterval;
	private Deque<Interval> intervalStack = new ArrayDeque<Interval>();

	private BitSet knownGoodBitset = new BitSet();
	private BitSet currentBitset = new BitSet();

	private static final int DEFAULT_GROUP_COUNT = 4;

	public StringMinimizer(final String initString, final String delim) {
		initialString = initString;
		delimiter = delim;

		Scanner scanner = new Scanner(initialString);

		if (delimiter == null) {
			throw new ConfigurationException("String delimiter required for StringMinimizer.");
		}

		// A very quick and dirty check for delimiters which look like regular expressions
		// Such delimiters are not allowed because they can not be appended back to the string as needed

		if (delimiter.contains("\\") || delimiter.contains(".") || delimiter.contains("*")) {
			throw new ConfigurationException("String delimiter for StringMinimizer can not be a regular expression.");
		}

		scanner.useDelimiter(delimiter);

		while (scanner.hasNext()) {
			String originalElement = scanner.next();
			initialElements.add(originalElement);
		}

		// We begin the algorithm using a single Interval that covers the entire original string

		if (initialElements.size() > 1) {
			Interval initialInterval = new Interval(0, initialElements.size() - 1);
			intervalStack.push(initialInterval);
		}
	}

	void failed() {
		// Restore the element bitmap to the known-good value
		currentBitset = (BitSet) knownGoodBitset.clone();

		final int currentIntervalStart = currentInterval.getStart();
		final int currentIntervalEnd = currentInterval.getEnd();
		final int currentIntervalLength = (currentIntervalEnd - currentIntervalStart) + 1; // NOPMD

		assert currentIntervalEnd < initialElements.size();
		assert currentIntervalLength > 0;
		assert currentIntervalLength <= initialElements.size();

		if (currentIntervalLength == 1) {
			// Current interval can not be subdivided further
			return;
		}

		if (currentIntervalLength <= DEFAULT_GROUP_COUNT) {
			// Current interval can only be subdivided into single-element intervals
			for (int i = currentIntervalStart; i <= currentIntervalEnd; i++) {
				final Interval singleElementInterval = new Interval(i, i);
				intervalStack.push(singleElementInterval);
			}
			return;
		}

		// Subdivide the current interval into DEFAULT_GROUP_COUNT groups

		final int newGroupSize = currentIntervalLength / DEFAULT_GROUP_COUNT;

		for (int i = 0; i < DEFAULT_GROUP_COUNT; i++) {
			final int newIntervalStart = currentIntervalStart + (i * newGroupSize); // NOPMD
			final int newIntervalEnd = currentIntervalStart + ((i + 1) * newGroupSize) - 1; // NOPMD
			assert newIntervalStart <= newIntervalEnd;
			assert newIntervalStart <= currentIntervalEnd;
			assert newIntervalEnd <= currentIntervalEnd;

			Interval newInterval = new Interval(newIntervalStart, newIntervalEnd);
			intervalStack.push(newInterval);
		}

		if (newGroupSize * DEFAULT_GROUP_COUNT == currentIntervalLength) {
			// There are no remaining elements
			assert currentIntervalStart + newGroupSize * DEFAULT_GROUP_COUNT - 1 == currentIntervalEnd; // NOPMD
			return;
		}

		// Put the remaining elements in their own interval

		final int newIntervalStart = currentInterval.getStart() + (newGroupSize * DEFAULT_GROUP_COUNT); // NOPMD
		assert newIntervalStart <= currentIntervalEnd;

		final int newIntervalEnd = currentInterval.getEnd();
		final Interval newTrailingInterval = new Interval(newIntervalStart, newIntervalEnd);
		intervalStack.push(newTrailingInterval);
	}

	void succeeded() {
		// The current element bitmap is the new known-good configuration
		knownGoodBitset = (BitSet) currentBitset.clone();
	}

	@Override
	public Iterator<String> iterator() {
		if (iterator == null) {
			iterator = new Iterator<String>() {
				@Override
				public boolean hasNext() {
					return !intervalStack.isEmpty();
				}
				@Override
				public String next() {
					currentInterval = intervalStack.pop();
					currentBitset.set(currentInterval.getStart(), currentInterval.getEnd() + 1);
					return getCurrentString();
                                }

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		return iterator;
	}

	public String getCurrentString() {
		// We build the current string by using the original elements, but only transferring those
		// where the corresponding bit in the bitmap is FALSE

		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < initialElements.size(); i++) {
			if (!currentBitset.get(i)) {
				stringBuilder.append(initialElements.get(i));
				stringBuilder.append(delimiter);
			}
		}

		return stringBuilder.toString();
	}

	static final class Interval {
		private final int start;
		private final int end;

		Interval(final int s, final int e) {
			start = s;
			end = e;

			assert start >= 0;
			assert end >= start;
		}

		int getStart() {
			return start;
		}

		int getEnd() {
			return end;
		}

		@Override
		public String toString() {
			return start + "-" + end;
		}
	}
}
