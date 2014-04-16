package org.stoev.fuzzer;

import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;

class GrammarRule implements Generatable {

	private final String ruleName;
	private final List<Generatable> productions;

        private static final String PRODUCTION_SEPARATION_PATTERN = "\\s*\\|\\s*|\\s*\\z";

	GrammarRule(final String rn, final String ruleString) {
		ruleName = rn;
		productions = new LinkedList<Generatable>();

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

	public void generate(final Context context, final StringBuilder buffer) {
		if (productions.size() == 0) {
			return;
		}

		int randomProductionId = context.randomInt(productions.size());

		if (context.shouldCacheRule(ruleName)) {
			StringBuilder temporaryBuffer = new StringBuilder();
			productions.get(randomProductionId).generate(context, temporaryBuffer);
			context.setCachedValue(ruleName, temporaryBuffer.toString());
			buffer.append(temporaryBuffer);
		} else {
			productions.get(randomProductionId).generate(context, buffer);
		}
	}

	public void link(final Grammar grammar) {
		for (Generatable production : productions) {
			production.link(grammar);
		}
	}

	public String toString() {
		return ruleName;
	}
}
