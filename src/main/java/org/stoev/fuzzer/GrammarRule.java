package org.stoev.fuzzer;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

class GrammarRule implements Generatable {

	private final String ruleName;
	private final List<Generatable> productions;

        private static final String PRODUCTION_SEPARATION_PATTERN = "\\s*\\|\\s*|\\s*\\z";

	GrammarRule(final String rn, final String ruleString) {
		ruleName = rn;
		productions = new ArrayList<Generatable>();

                Scanner scanner = new Scanner(ruleString);
		scanner.useDelimiter(PRODUCTION_SEPARATION_PATTERN);

                while (scanner.hasNext()) {
                        String productionString = scanner.next();
			productions.add(new GrammarProduction(productionString));
		}
	}

	GrammarRule(final String rn, final List<Generatable> ruleProductions) {
		ruleName = rn;
		productions = ruleProductions;
	}

	public void generate(final Context context, final Sentence<?> sentence) throws IOException {
		if (productions.size() == 0) {
			return;
		}

		int randomProductionId = context.randomInt(productions.size());

		if (context.shouldCacheRule(ruleName)) {
			Sentence<String> ruleSentence = new Sentence<String>();
			productions.get(randomProductionId).generate(context, ruleSentence);
			context.setCachedValue(ruleName, ruleSentence);
			sentence.addAll(ruleSentence);
		} else {
			productions.get(randomProductionId).generate(context, sentence);
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
