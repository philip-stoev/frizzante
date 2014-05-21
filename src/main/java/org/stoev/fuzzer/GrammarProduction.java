package org.stoev.fuzzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class GrammarProduction implements Generatable {
	private static final String WEIGHT_PATTERN = "^\\d+(%|)";
	private static final int DEFAULT_WEIGHT_VALUE = 1;
	private static final String VISITOR_EXTENSION = "_visitor";
	private static final String CACHED_EXTENSION = "_cached";

	private static final String ALPHANUMERIC_IDENTIFIER = "[a-zA-Z0-9_]+";
	private static final String EVERYTHING_ELSE = "[^a-zA-Z0-9_\\s]+";

	private final GrammarRule parentRule;
	private final List<Generatable> elements;

	private final double originalWeight;
	private double weight;

        GrammarProduction(final GrammarRule parent, final String productionString) {
		this.parentRule = parent;

		final Scanner scanner = new Scanner(productionString);
		scanner.useDelimiter("");

		elements = new ArrayList<Generatable>();

		String weightString = scanner.findWithinHorizon(WEIGHT_PATTERN, 0);

		if (weightString == null) {
			weight = DEFAULT_WEIGHT_VALUE;
			assert !productionString.matches("^\\d");
		} else if (weightString.endsWith("%")) {
			weight = Integer.parseInt(weightString.substring(0, weightString.length() - 1));
		} else {
			weight = Integer.parseInt(weightString);
		}

		if (weight < 1) {
			throw new ConfigurationException("Weight must be positive.");
		}

		originalWeight = weight;

		// Trim leading whitespace

		while (scanner.hasNext(Constants.WHITESPACE)) {
			scanner.next(Constants.WHITESPACE);
		}

		// We populate the elements array backwards as this is how items will be inserted into the stack during generation

                while (true) {
                        String elementString = scanner.findWithinHorizon(
				  Constants.WHITESPACE
				+ Constants.OR + Constants.OPTIONAL_WHITESPACE + ALPHANUMERIC_IDENTIFIER
				+ Constants.OR + Constants.OPTIONAL_WHITESPACE + EVERYTHING_ELSE, 0);
			if (elementString == null) {
				break;
			}

			if (elementString.matches(Constants.WHITESPACE)) {
				elements.add(0, new Separator());
			} else {
				elements.add(0, new GrammarLiteral(elementString));
			}
                }
        }

	double getWeight() {
		return weight;
	}

	void penalize(final double penalty) {
		this.weight = this.weight * (1 - penalty);
		parentRule.recalculateWeights();
	}

	void promote(final double promotion) {
		Double newWeight = this.weight / (1 - promotion);

		// Do not allow the new weight to exceed the original one
		// Otherwise weights can grow indefinitely

		if (newWeight < originalWeight) {
			weight = newWeight;
			parentRule.recalculateWeights();
		}
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		sentence.addProduction(this);
		for (Generatable element: elements) {
			sentence.getStack().push(element);
		}
	}

	public void compile(final Grammar grammar) {
		for (int i = 0; i < elements.size(); i++) {
			String ruleName = elements.get(i).getName();

			Generatable replacement = null;

			if (ruleName.endsWith(CACHED_EXTENSION)) {
				String replacementRuleName = ruleName.substring(0, ruleName.length() - CACHED_EXTENSION.length());
				replacement = new CachedValue(replacementRuleName);
				grammar.setRuleCached(replacementRuleName);
			} else if (ruleName.endsWith(VISITOR_EXTENSION)) {
				String replacementVisitorName = ruleName.substring(0, ruleName.length() - VISITOR_EXTENSION.length());
				replacement = new JavaVisitor(replacementVisitorName);
			} else {
				replacement = grammar.getRule(ruleName);
			}

			if (replacement != null) {
				elements.set(i, replacement);
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(weight);
		sb.append(" ");

		for (Generatable element: elements) {
			sb.append(element.getName());
			sb.append(" ");
		}

		return sb.toString();
	}

	public String getName() {
		return "";
	}
}
