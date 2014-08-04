package org.stoev.fuzzer;

import org.stoev.fuzzer.Grammar.GrammarOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.ListIterator;
import java.util.Set;

final class GrammarProduction<T> implements Generatable<T> {
	private static final String WEIGHT_PATTERN = "^\\d+(%|)";
	private static final int DEFAULT_WEIGHT = 1;
	private static final String CACHED_EXTENSION = "_cached";

	private static final String ALPHANUMERIC_IDENTIFIER = "[a-zA-Z0-9_]+";
	private static final String EVERYTHING_ELSE = "[^a-zA-Z0-9_\\s]+";

	private final GrammarRule<T> parentRule;
	private final List<Generatable<T>> elements = new ArrayList<Generatable<T>>();


	private final double initialWeight;
	private double weight;

	GrammarProduction(final GrammarRule<T> parent, final String productionString, final Set<GrammarOptions> options) {
		this.parentRule = parent;

		final Scanner scanner = new Scanner(productionString);
		scanner.useDelimiter("");

		String weightString = scanner.findWithinHorizon(WEIGHT_PATTERN, 0);

		if (weightString == null) {
			weight = DEFAULT_WEIGHT;
			assert !productionString.matches("^\\d");
		} else if (weightString.endsWith("%")) {
			weight = Integer.parseInt(weightString.substring(0, weightString.length() - 1));
		} else {
			weight = Integer.parseInt(weightString);
		}

		if (weight < 1) {
			throw new IllegalArgumentException("Grammar production weight must be positive.");
		}

		initialWeight = weight;

		// Trim leading whitespace

		while (scanner.hasNext(Constants.WHITESPACE)) {
			scanner.next(Constants.WHITESPACE);
		}

		while (true) {
			String elementString = scanner.findWithinHorizon(
				  Constants.WHITESPACE
				+ Constants.OR + Constants.OPTIONAL_WHITESPACE + ALPHANUMERIC_IDENTIFIER
				+ Constants.OR + Constants.OPTIONAL_WHITESPACE + EVERYTHING_ELSE, 0);

			if (elementString == null) {
				break;
			}

			// We populate the elements array backwards as this is how items will be inserted into the stack during generation

			if (!options.contains(GrammarOptions.SKIP_WHITESPACE)) {
				elements.add(0, new GrammarLiteral<T>(elementString));
			} else if (!elementString.matches(Constants.WHITESPACE)) {
				elements.add(0, new GrammarLiteral<T>(elementString));
			}
		}
	}

	double getWeight() {
		return weight;
	}

	GrammarRule<T> getParent() {
		return parentRule;
	}

	void demote(final double penalty) {
		if (penalty < 0.0f || penalty > 1.0f) {
			throw new IllegalArgumentException("Demotion penalty must be between 0 and 1.0.");
		}

		this.weight = this.weight * (1 - penalty);
		assert this.weight >= 0.0f;

		parentRule.recalculateWeightSum();
	}

	void promote(final double promotion) {
		if (promotion < 0.0f || promotion > 1.0d) {
			throw new IllegalArgumentException("Promotion must be between 0 and 1.0.");
		}


		Double newWeight = this.weight / (1 - promotion);
		assert newWeight >= 0.0d;

		// Do not allow the new weight to exceed the initial value
		// Otherwise weights can grow indefinitely

		if (newWeight < initialWeight) {
			weight = newWeight;
			parentRule.recalculateWeightSum();
		}
	}

	public void generate(final ThreadContext<T> threadContext, final Sentence<T> sentence) {
		sentence.enterProduction(this);

		for (Generatable<T> element: elements) {
			sentence.pushGeneratable(element);
		}
	}

	public void compile(final Grammar<T> grammar) {
		for (int i = 0; i < elements.size(); i++) {
			String ruleName = elements.get(i).getName();
			Generatable<T> replacement = null;

			if (ruleName.endsWith(CACHED_EXTENSION)) {
				String replacementRuleName = ruleName.substring(0, ruleName.length() - CACHED_EXTENSION.length());
				replacement = new CachedValue<T>(replacementRuleName);
				grammar.setRuleCached(replacementRuleName);
			} else {
				replacement = grammar.getRule(ruleName);
			}

			if (replacement != null) {
				elements.set(i, replacement);
			}
		}
	}

	public boolean isConstant() {
		boolean isConstant = true;

		for (Generatable<T> element: elements) {
			if (!element.isConstant()) {
				isConstant = false;
			}
		}

		return isConstant;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(weight);
		sb.append(Constants.SPACE);

		if (elements.size() == 0) {
			return sb.toString();
		}

		// As we store the elements in reverse order, this iterator works backwards
		ListIterator<Generatable<T>> i = elements.listIterator(elements.size());

		sb.append(i.previous().getName());

		while (i.hasPrevious()) {
			sb.append(i.previous().getName());
		}

		return sb.toString();
	}

	public String getName() {
		return "";
	}
}
