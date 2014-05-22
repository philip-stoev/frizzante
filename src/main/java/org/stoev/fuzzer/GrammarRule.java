package org.stoev.fuzzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Iterator;

class GrammarRule implements Generatable {

	private final String ruleName;
	private final List<GrammarProduction> productions;
	private double totalWeight;

	private static final String PIPE_NOT_ESCAPED = "(?<!\\\\)\\|";

	private static final String ESCAPED_PIPE = "\\\\\\|";

	private static final String PRODUCTION_SEPARATION_PATTERN =
		  Constants.OPTIONAL_WHITESPACE + PIPE_NOT_ESCAPED
		+ Constants.OR + Constants.OPTIONAL_WHITESPACE + Constants.SEMICOLON + Constants.EOL + Constants.OPTIONAL_WHITESPACE
		+ Constants.OR + Constants.OPTIONAL_WHITESPACE + Constants.SEMICOLON + Constants.OPTIONAL_WHITESPACE + Constants.EOF;

	GrammarRule(final String rn, final String ruleString) {
		ruleName = rn;
		productions = new ArrayList<GrammarProduction>();
		assert ruleString != null;

		Scanner scanner = new Scanner(ruleString);
		scanner.useDelimiter(PRODUCTION_SEPARATION_PATTERN);

		while (scanner.hasNext()) {
			String productionString = scanner.next();
			productionString = productionString.replaceAll(ESCAPED_PIPE, "|");
			GrammarProduction production = new GrammarProduction(this, productionString);
			productions.add(production);
		}

		recalculateWeights();
	}

	public void recalculateWeights() {
		double runningWeightSum = 0;

		for (GrammarProduction production: productions) {
			runningWeightSum = runningWeightSum + production.getWeight();
		}

		totalWeight = runningWeightSum;
	}

	public void generate(final Context context, final Sentence<?> sentence) {
		if (productions.size() == 0) {
			return;
		}

		GrammarProduction randomProduction = null;
		double randomWeight = context.randomDouble() * totalWeight;
		double runningWeight = 0;

		for (GrammarProduction production: productions) {
			runningWeight = runningWeight + production.getWeight();
			if (runningWeight > randomWeight) {
				randomProduction = production;
				break;
			}
		}

		assert randomProduction != null;

		if (context.shouldCacheRule(ruleName)) {
			Sentence<String> cachedSentence = new Sentence<String>();

			cachedSentence.getStack().push(randomProduction);

			while (!cachedSentence.getStack().isEmpty()) {
				cachedSentence.getStack().pop().generate(context, cachedSentence);
			}

			context.setCachedValue(ruleName, cachedSentence);
			sentence.addAll(cachedSentence);
		} else {
			sentence.getStack().push(randomProduction);
		}
	}

	public void compile(final Grammar grammar) {
		for (Generatable production : productions) {
			production.compile(grammar);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ruleName);
		sb.append(":");

		if (productions.size() == 0) {
			sb.append(";\n");
			return sb.toString();
		}

		Iterator<GrammarProduction> i = productions.iterator();

		sb.append(i.next().toString());
		sb.append("\n");

		while (i.hasNext()) {
			sb.append("|");
			sb.append(i.next().toString());
			sb.append("\n");
		}

		sb.append(";\n");

		return sb.toString();
	}

	public String getName() {
		return ruleName;
	}
}

