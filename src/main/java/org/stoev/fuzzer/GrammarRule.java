package org.stoev.fuzzer;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

class GrammarRule implements Generatable {

	private final String ruleName;
	private final List<GrammarProduction> productions;
	private final int totalWeight;

        private static final String PRODUCTION_SEPARATION_PATTERN = "\\s*\\||\\s*\\z|\\s*;\\s*";

	GrammarRule(final String rn, final String ruleString) {
		ruleName = rn;
		productions = new ArrayList<GrammarProduction>();

                Scanner scanner = new Scanner(ruleString);
		scanner.useDelimiter(PRODUCTION_SEPARATION_PATTERN);

		int runningWeightSum = 0;

                while (scanner.hasNext()) {
                        String productionString = scanner.next();
			GrammarProduction production = new GrammarProduction(productionString);
			productions.add(production);
			runningWeightSum = runningWeightSum + production.getWeight();
		}

		totalWeight = runningWeightSum;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		if (productions.size() == 0) {
			return;
		}

		GrammarProduction randomProduction = null;
		int randomWeight = context.randomInt(totalWeight);
		int runningWeight = 0;

		for (GrammarProduction production: productions) {
			runningWeight = runningWeight + production.getWeight();
			if (runningWeight > randomWeight) {
				randomProduction = production;
				break;
			}
		}

		assert randomProduction != null;

		if (context.shouldCacheRule(ruleName)) {
			Sentence<String> ruleSentence = new Sentence<String>();
			randomProduction.generate(context, ruleSentence);
			context.setCachedValue(ruleName, ruleSentence);
			sentence.addAll(ruleSentence);
		} else {
			randomProduction.generate(context, sentence);
		}
	}

	public void compile(final Grammar grammar) {
		for (Generatable production : productions) {
			production.compile(grammar);
		}
	}

	public String toString() {
		return ruleName;
	}
}

