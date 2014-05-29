package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class SentenceSimplifier implements Iterable<Sentence> {
	private final Sentence originalSentence;

	private Iterator<Sentence> iterator;
	private List<?> originalElements;
	private List<ProductionUse> originalProductionsUsed;

	private final List<Boolean> productionRequired;
	private int currentProductionListPosition = -1;

	SentenceSimplifier(final Sentence orig) {
		originalSentence = orig;
		originalElements = originalSentence.getElements();
		originalProductionsUsed = originalSentence.getProductionsUsed();

		productionRequired = new ArrayList<Boolean>(originalProductionsUsed.size());

		for (int i = 0; i < originalProductionsUsed.size(); i++) {
			productionRequired.add(true);
		}
	}

	final Sentence getCurrentSentence() {
		Sentence sentence = new Sentence();
		List<Boolean> elementsRequired = new ArrayList<Boolean>(originalSentence.size());
		List<Sentence> constantSubstitutions = new ArrayList<Sentence>(originalSentence.size());

		// Assume initially that all elements are required and no substitutions are taking place
		for (int i = 0; i < originalElements.size(); i++) {
			elementsRequired.add(true);
			constantSubstitutions.add(null);
		}

		for (int i = 0; i < originalProductionsUsed.size(); i++) {
			ProductionUse productionUse = originalProductionsUsed.get(i);
			GrammarProduction production = productionUse.getProduction();
			GrammarRule rule = (GrammarRule) production.getParent();
			Sentence shortestConstantSentence = rule.getShortestConstantSentence();

			// IF the current production indeed produced something
			// AND the current production is indeeed required for success
			// AND the current element has not yet been flagged out by some other production
			// AND the current production has a constant value that we can subsitute with
			// THEN perform substitution

			if (
				(productionUse.wasProductive())
				&& (!productionRequired.get(i))
				&& (elementsRequired.get(productionUse.getStart()))
				&& (shortestConstantSentence != null)
			) {
				for (int x = productionUse.getStart(); x <= productionUse.getEnd(); x++) {
					elementsRequired.set(x, false);
				}

				constantSubstitutions.set(productionUse.getStart(), shortestConstantSentence);
			}
		}

		for (int i = 0; i < elementsRequired.size(); i++) {
			if (elementsRequired.get(i)) {
				sentence.add(originalElements.get(i));
			} else if (constantSubstitutions.get(i) != null) {
				sentence.addAll(constantSubstitutions.get(i));
			}
		}

		return sentence;
	}

	final void succeeded() {
		productionRequired.set(currentProductionListPosition, false);
	}

	final void failed() {
		productionRequired.set(currentProductionListPosition, true);
	}

	@Override
	public final Iterator<Sentence> iterator() {
		if (iterator == null) {
			iterator = new Iterator<Sentence>() {
				@Override
				public boolean hasNext() {
					if (originalProductionsUsed.size() == 0) {
						return false;
					} else {
						return (currentProductionListPosition < originalProductionsUsed.size() - 1);
					}
				}

				@Override
				public Sentence next() {
					currentProductionListPosition++;
					productionRequired.set(currentProductionListPosition, false);
					return getCurrentSentence();
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		return iterator;
	}
}
