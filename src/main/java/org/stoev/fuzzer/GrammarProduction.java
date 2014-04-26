package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

class GrammarProduction implements Generatable {
	private static final Pattern WEIGHT_PATTERN = Pattern.compile("\\d+(%|)");
	private static final int DEFAULT_WEIGHT_VALUE = 1;
	private static final String VISITOR_EXTENSION = ".visitor";

	private final int weight;
	private final List<Generatable> elements;

        GrammarProduction(final String productionString) {
                Scanner scanner = new Scanner(productionString);
		elements = new ArrayList<Generatable>();

		if (scanner.hasNext(WEIGHT_PATTERN)) {
			String weightString = scanner.next(WEIGHT_PATTERN);
			if (weightString.endsWith("%")) {
				weight = Integer.parseInt(weightString.substring(0, weightString.length() - 1));
			} else {
				weight = Integer.parseInt(weightString);
			}
		} else {
			weight = DEFAULT_WEIGHT_VALUE;
		}

		if (weight < 1) {
			throw new ConfigurationException("Weight must be positive");
		}

                while (scanner.hasNext()) {
                        String elementString = scanner.next();
			elements.add(new GrammarLiteral(elementString));
                }
        }

	GrammarProduction(final List<Generatable> grammarElements) {
		weight = DEFAULT_WEIGHT_VALUE;
		elements = grammarElements;
	}

	final int getWeight() {
		return weight;
	}

	public final void generate(final Context context, final Sentence<?> sentence) {
		for (Generatable element: elements) {
			element.generate(context, sentence);
		}
	}

	public final void compile(final Grammar grammar) {
		for (int i = 0; i < elements.size(); i++) {
			String ruleName = elements.get(i).toString();
			Generatable replacement = null;

			if (ruleName.startsWith("$")) {
				String replacementRuleName = ruleName.substring(1);
				replacement = new CachedValue(replacementRuleName);
				grammar.setRuleCached(replacementRuleName);
			} else if (ruleName.endsWith(VISITOR_EXTENSION)) {
				String replacementVisitorName = ruleName.substring(0, ruleName.length() - VISITOR_EXTENSION.length());
				replacement = new VisitorValue(replacementVisitorName);
			} else {
				replacement = grammar.getRule(ruleName);
			}

			if (replacement != null) {
				elements.set(i, replacement);
			}
		}
	}

	public final String toString() {
		assert false;
		return "";
	}
}
