package org.stoev.fuzzer;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public final class SentenceSimplifier implements Iterable<Sentence> {
	private final Sentence originalSentence;

	private Iterator<Sentence> iterator;
	private List<?> originalElements;
	private List<ProductionUse> productionsUsed;

	private final List<ProductionStatus> productionStatus;
	private int currentPosition = -1; // "-1" means the Iterator has not been used yet

	enum ProductionStatus {
		ORIGINAL,		// The original production, suitable for replacement with a constant
		NONMINIMIZABLE,		// Production that has no alternatives, so can not be minimized
		REPLACED,		// A production that was successfully replaced with a constant
		REMOVED,		// A nested production that was removed by success() because a higher-level production was minimized
		EMPTY			// A production that did not produce any output, can be skipped
	};

	SentenceSimplifier(final Sentence orig) {
		originalSentence = orig;
		originalElements = originalSentence.getElements();
		productionsUsed = originalSentence.getProductionsUsed();
		productionStatus = new ArrayList<ProductionStatus>(productionsUsed.size());

		// Loop through the productions and categorize them as per the ProductionStatus enum above

		for (ProductionUse productionUse : productionsUsed) {
			if (!productionUse.wasProductive()) {
				productionStatus.add(ProductionStatus.EMPTY);
			} else {
				GrammarProduction production = productionUse.getProduction();
				GrammarRule rule = (GrammarRule) production.getParent();
				Sentence shortestConstantSentence = rule.getShortestConstantSentence();

				if (shortestConstantSentence == null || rule.getProductionCount() == 1) {
					productionStatus.add(ProductionStatus.NONMINIMIZABLE);
				} else {
					productionStatus.add(ProductionStatus.ORIGINAL);
				}
			}
		}

		assert productionsUsed.size() == productionStatus.size();
	}

	Sentence getCurrentSentence() {
		Sentence sentence = new Sentence();
		List<Boolean> elementInOutput = new ArrayList<Boolean>(originalSentence.size());
		List<Sentence> constantSubstitutions = new ArrayList<Sentence>(originalSentence.size());

		// Assume initially that all elements from the original Sentence will make it into the new one
		// and no substitutions are taking place

		for (int i = 0; i < originalElements.size(); i++) {
			elementInOutput.add(true);
			constantSubstitutions.add(null);
		}

		// Then go though the productions and modify the output as per the status of the individual productions

		for (int i = 0; i < productionsUsed.size(); i++) {
			ProductionUse productionUse = productionsUsed.get(i);

			switch(productionStatus.get(i)) {
				case EMPTY:
				case ORIGINAL:
				case NONMINIMIZABLE:
					break;
				case REMOVED:
					for (int x = productionUse.getStart(); x <= productionUse.getEnd(); x++) {
						elementInOutput.set(x, false);
	                                }

					break;
				case REPLACED:
					for (int x = productionUse.getStart(); x <= productionUse.getEnd(); x++) {
						elementInOutput.set(x, false);
	                                }

					GrammarProduction production = productionUse.getProduction();
					GrammarRule rule = (GrammarRule) production.getParent();
					Sentence shortestConstantSentence = rule.getShortestConstantSentence();

					constantSubstitutions.set(productionUse.getStart(), shortestConstantSentence);

					break;
				default:
					assert false;
					break;
			}
		}

		// Finally, construct a new sentence taking either elements from the original one or constant replacements

		for (int i = 0; i < elementInOutput.size(); i++) {
			if (elementInOutput.get(i)) {
				sentence.add(originalElements.get(i));
			} else if (constantSubstitutions.get(i) != null) {
				sentence.addAll(constantSubstitutions.get(i));
			}
		}

		return sentence;
	}

	void succeeded() {
		assert productionStatus.get(currentPosition) == ProductionStatus.REPLACED;

		ProductionUse currentProductionUse = productionsUsed.get(currentPosition);

		if (currentProductionUse.wasProductive()) {
			// Mark as REMOVED all productions that are completely enclosed within the current one

			for (int i = currentPosition + 1; i < productionsUsed.size(); i++) {
				ProductionUse productionUse = productionsUsed.get(i);
				if (
					productionUse.wasProductive()
					&& productionUse.getStart() >= currentProductionUse.getStart()
					&& productionUse.getEnd() <= currentProductionUse.getEnd()
				) {
					productionStatus.set(i, ProductionStatus.REMOVED);
				}
			}
		}
	}

	void failed() {
		// Restore the last production we modified on to its original state
		productionStatus.set(currentPosition, ProductionStatus.ORIGINAL);
	}

	@Override
	public Iterator<Sentence> iterator() {
		if (iterator == null) {
			iterator = new Iterator<Sentence>() {
				@Override
				public boolean hasNext() {
					// Check if there are any productions that are potential targets for minimization
					for (int i = currentPosition + 1; i < productionsUsed.size(); i++) {
						if (productionStatus.get(i) == ProductionStatus.ORIGINAL) {
							return true;
						}
					}

					return false;
				}

				@Override
				public Sentence next() {
					// We advance the position to the next production that can be worked on
					for (currentPosition++; currentPosition < productionsUsed.size(); currentPosition++) {
						if (productionStatus.get(currentPosition) == ProductionStatus.ORIGINAL) {
							productionStatus.set(currentPosition, ProductionStatus.REPLACED);
							break;
						}
					}

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
