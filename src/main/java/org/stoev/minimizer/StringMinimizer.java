package org.stoev.minimizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Iterator;

/**
 * Imlements a minimization algorithm that works on Strings.
 *
 * <p> Using an Iterator, the algorithm produces intermediate Strings, which are attempts to minimize the initial String.
 * The caller uses {@link #success() success} and {@link #failure() failure} or @link #report() report} to report back whether a given minimization
 * attempt was successfull or not, which guides the algorithm. After the Iterator has run its course, {@link #getCurrentString() getCurrentString}
 * returns the final minimized String.
 *
 * <p><strong>Example</strong>
 * <p>For example, consider a String, which, for some reason, is not persisted properly when inserted into a custom HashMap implementation
 * We would like to obtain the shortest string that exposes this behavior
 * <code><pre>
 * String originalWordList = ...;
 * StringMinimizer minimizer = new StringMinimizer(originalWordList, " ");
 * Map<String,Boolean> map = new SomeExperimentalHashMap<String,Boolean>();
 *
 * for (String currentString: minimizer) {
 *     map.put(currentString, true);
 *     minimizer.report(map.get(currentString) != null);
 * }
 *
 * String minimizedString = minimizer.getCurrentString();
 * </pre></code>
 *
 * @author Philip Stoev <philip@stoev.org>
 * @see SequenceMinimizer
 */

public final class StringMinimizer implements Iterable<String> {
	private final SequenceMinimizer<String> listMinimizer;

	private final Iterator<List<String>> listIterator;
	private final Iterator<String> stringIterator;

	private final String delimiter;

 	/**
	 * Begin a minimization from a string and a delimiter.
	 *
	 * @param initialString
	 * @param delim the String delimiter that will be used to chop the initialString into pieces using Scanner.
	 * 	The delimiter is a literal string and not a regular expression.
	 *	If you specify an empty delimiter, the string will be chopped into individual letters
	 * @throws IllegalArgumentException if no delimiter was specified
	 * @throws IllegalArgumentException the delimiter appears to be a regular expression
        */

	public StringMinimizer(final String initialString, final String delim) {
		this.delimiter = delim;

		if (delimiter == null) {
			throw new IllegalArgumentException("String delimiter required for SequenceMinimizer of Strings.");
		}

		// A very quick and dirty check for delimiters which look like regular expressions
		// Such delimiters are not allowed because they can not be appended back to the string as needed

		if (delimiter.contains("\\") || delimiter.contains(".") || delimiter.contains("*")) {
			throw new IllegalArgumentException("String delimiter for SequenceMinimizer can not be a regular expression.");
		}

		Scanner scanner = new Scanner(initialString);
		scanner.useDelimiter(delimiter);

		List<String> scannerElements = new ArrayList<String>();

		while (scanner.hasNext()) {
			scannerElements.add(scanner.next());
		}

		listMinimizer = new SequenceMinimizer<String>(scannerElements);
		listIterator = listMinimizer.iterator();

		stringIterator = new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return listIterator.hasNext();
			}

			@Override
			public String next() {
				return listToString((List<?>) listIterator.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

	}

 	/**
	 * Report that the latest minimization attempt did not work. The changes will be rolled back and the algorithm will try a different minimization
	 */

	public void failure() {
		listMinimizer.failure();
	}

 	/**
	 * Report that the latest minimization attempt was successfull. The minimization will be made permanent and the algorithm will continue in the same vein.
	 */

	public void success() {
		listMinimizer.success();
	}

 	/**
	 * Report the outcome from the latest minimization. Can be used instead of a pair of success/failure calls.
	 *
         * @param success if the latest minimization was successfull or not.
	 */

	public void report(final boolean success) {
		if (success) {
			success();
		} else {
			failure();
		}
	}

 	/**
	 * Returns an Iterator that is used to iterate through the individual minimization attempts the algorithm produces.
	 */

	public Iterator<String> iterator() {
		return stringIterator;
	}

 	/**
	 * Returns the current state of the String being minimized. At the end of the algorithm, will return the final outcome from the minimization.
	 */

	public String getCurrentString() {
		return listToString(listMinimizer.getCurrentList());
	}

	@Override
	public String toString() {
		return getCurrentString();
	}

	String listToString(final List<?> list) {
		StringBuilder stringBuilder = new StringBuilder();

		for (Object element: list) {
			stringBuilder.append(element);
			stringBuilder.append(delimiter);
		}

		return stringBuilder.toString();
	}
}
