package org.stoev.frizzante;

import org.stoev.frizzante.Grammar.GrammarOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;

class GrammarRule<T> implements Generatable<T> {

	private final String ruleName;
	private final List<GrammarProduction<T>> productions = new ArrayList<GrammarProduction<T>>();

	private double weightSum;

	private boolean shortestConstantCalculated;
	private Sentence<T> shortestConstantSentence;

	GrammarRule(final String rn, final String ruleString, final Set<GrammarOptions> options) {
		ruleName = rn;
		assert ruleString != null;

		// Trim whitespace after trailing semicolon
		final String trimmedRuleString = ruleString.replaceFirst("\\s+$", "");
		assert trimmedRuleString.endsWith(";");

		// Trim trailing semicolon and any whitespace that was immediately before it
		final String ruleStringNoSemicolon = trimmedRuleString.substring(0, trimmedRuleString.length() - 1).replaceFirst("\\s+$", "");
		final String pipePattern;

		if (options.contains(GrammarOptions.TRAILING_PIPES)) {
			pipePattern = Constants.TRAILING_PIPE;
		} else {
			pipePattern = Constants.PIPE;
		}

		String[] productionStrings = ruleStringNoSemicolon.split(Constants.OPTIONAL_WHITESPACE + pipePattern, -1);

		for (String productionString: productionStrings) {
			GrammarProduction<T> production = new GrammarProduction<T>(this, productionString, options);
			productions.add(production);
		}

		recalculateWeightSum();
	}

	public void recalculateWeightSum() {
		weightSum = 0;

		for (GrammarProduction<T> production: productions) {
			weightSum += production.getWeight();
		}
	}

	public void generate(final ThreadContext<T> threadContext, final Sentence<T> sentence) {
		if (productions.size() == 0) {
			if (!shortestConstantCalculated) {
				shortestConstantSentence = sentence.newInstance();
				shortestConstantCalculated = true;
			}
			return;
		}

		GrammarProduction<T> randomProduction = null;
		double randomWeight = sentence.getRandom().nextDouble() * weightSum;
		double runningWeight = 0;

		for (GrammarProduction<T> production: productions) {
			runningWeight = runningWeight + production.getWeight();
			if (runningWeight > randomWeight) {
				randomProduction = production;
				break;
			}
		}

		assert randomProduction != null;

		if (threadContext.getGlobalContext().getGrammar().shouldCacheRule(ruleName)) {
			Sentence<T> cachedSentence = sentence.newInstance();
			cachedSentence.populate(threadContext, randomProduction);

			threadContext.setCachedValue(ruleName, cachedSentence);
			sentence.addAll(cachedSentence);
		} else {
			sentence.pushGeneratable(randomProduction);
		}

		if (!shortestConstantCalculated) {
			for (GrammarProduction<T> production: productions) {
				if (!production.isConstant()) {
					continue;
				}

				Sentence<T> constantSentence = sentence.newInstance();
				constantSentence.populate(threadContext, production);

				if (shortestConstantSentence == null || shortestConstantSentence.size() > constantSentence.size()) {
					shortestConstantSentence = constantSentence;
				}
			}

			shortestConstantCalculated = true;
		}
	}

	public Sentence<T> getShortestConstantSentence() {
		assert shortestConstantCalculated;

		return shortestConstantSentence;
	}

	public int getProductionCount() {
		return productions.size();
	}

	public void compile(final Grammar<T> grammar) {
		for (Generatable<T> production : productions) {
			production.compile(grammar);
		}
	}

	public boolean isConstant() {
		if (productions.size() == 0) {
			return true;
		} else if (productions.size() == 1) {
			return productions.get(0).isConstant();
		} else {
			return false;
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

		Iterator<GrammarProduction<T>> i = productions.iterator();

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
