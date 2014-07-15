package org.stoev.minimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;

/**
 * Imlements a minimization algorithm that works on Lists of elements, such as Strings.
 *
 * <p> Using an Iterator, the algorithm produces intermediate Lists, which are attempts to minimize the initial List.
 * The caller uses {@link #success() success} and {@link #failure() failure} to report back whether a given minimization
 * attempt was successfull or not, which guides the algorithm. After the Iterator has run its course, {@link #getCurrentList() getCurrentList}
 * returns the final product of the minimization.
 *
 * <p><strong>Applications</strong>
 * <p>The algorithm is used to minimize down large lists of items, such as a long list of SQL queries from an automatically generated test case where
 * the elements form a simple sequence with no hierarchical relationships. The algorithm performs best when most of the elements are
 * unnecessary and can be chopped away in large blocks.
 * <p><strong>Example</strong>
 * <p>For example, consider a large list of SQL queries that, when run as a sequence, cause a database server to throw a particular unwanted exception.
 * We would like to minimize the list of queries to the shortest list that still reproduces the problem.
 * <code><pre>
 * List<String> originalQueryList = ...;
 * SequenceMinimizer<String> minimizer = new SequenceMinimizer<String>(originalQueryList);
 *
 * for (List<String> currentQueryList: minimizer) {
 *     Statement stmt = conn.createStatement();
 *     try {
 *         for (String query: currentQueryList) {
 *             stmt.executeUpdate(query);
 *             minimizer.success();
 *     } except (SomeFatalException e) {
 *         minimizer.failure();
 *     }
 * }
 *
 * List<String> minimizedQueryList = minimizer.getCurrentList();
 *
 * </pre></code>
 * <p><strong>The algorithm:</strong>
 * <p>The algorithm works as follows:
 * <ul>
 * <li> attempt to chop off a block that is 1/4th of the current list and send the remainder to the caller for evaluation;
 * <li> if the caller reports success, make the change permanent and move on to the next block;
 * <li> if the caller reports failure, revert the change and attempt to chop off smaller pieces from this block in the future;
 * <li> repeat until the blocks are one element in length and all such blocks have been processed;
 * </ul>
 * The algorithm was expired by ddmin as described in "Simplifying and Isolating Failure-Inducing Input" by Andreas Zeller and 	Ralf Hildebrandt
 * <p><strong>Complexity and running time</strong>
 * <p>The algorithm does not attempt to try all possible combinations of initial elements, so it should complete in less than 2 * N cycles.
 *
 * @author Philip Stoev <philip@stoev.org>
 * @see StringMinimizer
 */

public final class SequenceMinimizer<T> implements Iterable<List<T>> {
	private static final int DEFAULT_GROUP_COUNT = 4;

	private Iterator<List<T>> listIterator;

	private final List<T> initialElements;

	private Interval currentInterval;
	private Deque<Interval> intervalQueue = new ArrayDeque<Interval>();

	private BitSet knownGoodBitset = new BitSet();
	private BitSet currentBitset = new BitSet();

	/**
	* Begin a minimization from an initial List of elements
	*/

	public SequenceMinimizer(final List<T> initElements) {
		initialElements = initElements;

		// We begin the algorithm using a single Interval that covers the entire original element list

		if (initialElements.size() > 1) {
			Interval initialInterval = new Interval(0, initialElements.size() - 1);
			intervalQueue.add(initialInterval);
		}

		listIterator = new Iterator<List<T>>() {
			@Override
			public boolean hasNext() {
				return !intervalQueue.isEmpty();
			}

			@Override
			public List<T> next() {
				if (intervalQueue.isEmpty()) {
					throw new NoSuchElementException();
				}

				currentInterval = intervalQueue.remove();
				currentBitset.set(currentInterval.getStart(), currentInterval.getEnd() + 1);
				return getCurrentList();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	* Signal that the most recent minimization produced by the object was not sucessfull.
	*
	* The minimization is rolled back and the algorithm will attempt to chop smaller pieces.
	*/

	public void failure() {
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
				intervalQueue.add(singleElementInterval);
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
			intervalQueue.add(newInterval);
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
		intervalQueue.add(newTrailingInterval);
	}

	/**
	* Signal that the most recent minimization produced by the object was successfull.
	*
	* The minimization is made permanent and the algorithm moves to attempt to minimize other parts of the input
	*/

	public void success() {
		// The current element bitmap is the new known-good configuration
		knownGoodBitset = (BitSet) currentBitset.clone();
	}

	/**
	* Signal the outcome from the most recent minimization.
	*
	* This call can be used in place of the <tt>success</tt> / </tt>failure</tt> pair.
	*/
	public void report(final boolean success) {
		if (success) {
			success();
		} else {
			failure();
		}
	}

	/**
	* Returns the Iterator that returns subsequent minimization attempts
	*
	* The Iterator produces minimization attempts until the algorithm runs its course and no further attempts are possible.
	*/

	public Iterator<List<T>> iterator() {
		return listIterator;
	}

	/**
	* Return the current state of the minimization.
	*
	* This call also is used to obtain the final product of the minimization after all the minimization attempts are complete
	*
	* @return a List<T> that is a subset of the initial List of objects
	*/

	public List<T> getCurrentList() {
		// We build the current list using the original elements, but only transferring those
		// where the corresponding bit in the bitmap is FALSE

		List<T> currentList = new ArrayList<T>();

		for (int i = 0; i < initialElements.size(); i++) {
			if (!currentBitset.get(i)) {
				currentList.add(initialElements.get(i));
			}
		}

		return currentList;
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
