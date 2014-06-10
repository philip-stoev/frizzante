package org.stoev.fuzzer;

import org.stoev.fuzzer.Grammar.GrammarFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Iterator;
import java.util.Set;

class GrammarRule<T> implements Generatable<T> {

	private final String ruleName;
	private final List<GrammarProduction<T>> productions = new ArrayList<GrammarProduction<T>>();

	private double weightSum;

	private boolean shortestConstantCalculated;
	private Sentence<T> shortestConstantSentence;

	GrammarRule(final String rn, final String ruleString, final Set<GrammarFlags> flags) {
		ruleName = rn;
		assert ruleString != null;

		final String pipePattern;

		if (flags.contains(GrammarFlags.TRAILING_PIPES_ONLY)) {
			pipePattern = Constants.TRAILING_PIPE;
		} else {
			pipePattern = Constants.PIPE;
		}

		final String productionSeparationPattern = Constants.OPTIONAL_WHITESPACE + pipePattern
		+ Constants.OR + Constants.OPTIONAL_WHITESPACE + Constants.SEMICOLON + Constants.OPTIONAL_WHITESPACE + Constants.EOF;

		Scanner scanner = new Scanner(ruleString);
		scanner.useDelimiter(productionSeparationPattern);

		while (scanner.hasNext()) {
			String productionString = scanner.next();
			GrammarProduction<T> production = new GrammarProduction<T>(this, productionString, flags);
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

	public void generate(final Context<T> context, final Sentence<T> sentence) {
		if (productions.size() == 0) {
			if (!shortestConstantCalculated) {
				shortestConstantSentence = sentence.newInstance();
				shortestConstantCalculated = true;
			}
			return;
		}

		GrammarProduction<T> randomProduction = null;
		double randomWeight = sentence.randomDouble() * weightSum;
		double runningWeight = 0;

		for (GrammarProduction<T> production: productions) {
			runningWeight = runningWeight + production.getWeight();
			if (runningWeight > randomWeight) {
				randomProduction = production;
				break;
			}
		}

		assert randomProduction != null;

		if (context.shouldCacheRule(ruleName)) {
			Sentence<T> cachedSentence = sentence.newInstance();
			cachedSentence.populate(context, randomProduction);

			context.setCachedValue(ruleName, cachedSentence);
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
				constantSentence.populate(context, production);

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
