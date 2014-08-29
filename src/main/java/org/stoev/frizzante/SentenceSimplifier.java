package org.stoev.frizzante;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class SentenceSimplifier<T> implements Iterable<Sentence<T>> {
	private final Sentence<T> originalSentence;

	private Iterator<Sentence<T>> iterator;
	private List<T> originalElements;
	private List<ProductionInstance<T>> productionInstances;

	private final List<ProductionStatus> productionStatus;
	private int currentPosition = -1; // "-1" means the Iterator has not been Instanced yet

	enum ProductionStatus {
		ORIGINAL,		// The original production, suitable for replacement with a constant
		NONMINIMIZABLE,		// Production that has no alternatives, so can not be minimized
		REPLACED,		// A production that was successfully replaced with a constant
		REMOVED,		// A nested production that was removed by success() becaInstance a higher-level production was minimized
		EMPTY			// A production that did not produce any output, can be skipped
	};

	SentenceSimplifier(final Sentence<T> orig) {
		originalSentence = orig;
		originalElements = originalSentence.getElements();
		productionInstances = originalSentence.getProductionInstances();
		productionStatus = new ArrayList<ProductionStatus>(productionInstances.size());

		// Loop through the productions and categorize them as per the ProductionStatus enum above

		for (ProductionInstance<T> productionInstance : productionInstances) {
			if (!productionInstance.wasProductive()) {
				productionStatus.add(ProductionStatus.EMPTY);
			} else {
				GrammarProduction<T> production = productionInstance.getProduction();
				GrammarRule<T> rule = production.getParent();
				Sentence<T> shortestConstantSentence = rule.getShortestConstantSentence();

				if (shortestConstantSentence == null || rule.getProductionCount() == 1) {
					productionStatus.add(ProductionStatus.NONMINIMIZABLE);
				} else {
					productionStatus.add(ProductionStatus.ORIGINAL);
				}
			}
		}

		assert productionInstances.size() == productionStatus.size();
	}

	Sentence<T> getCurrentSentence() {
		Sentence<T> sentence = originalSentence.newInstance();
		List<Boolean> elementInOutput = new ArrayList<Boolean>(originalSentence.size());
		List<Sentence<T>> constantSubstitutions = new ArrayList<Sentence<T>>(originalSentence.size());

		// Assume initially that all elements from the original Sentence will make it into the new one
		// and no substitutions are taking place

		for (int i = 0; i < originalElements.size(); i++) {
			elementInOutput.add(true);
			constantSubstitutions.add(null);
		}

		// Then go though the productions and modify the output as per the status of the individual productions

		for (int i = 0; i < productionInstances.size(); i++) {
			ProductionInstance<T> productionInstance = productionInstances.get(i);

			switch(productionStatus.get(i)) {
				case EMPTY:
				case ORIGINAL:
				case NONMINIMIZABLE:
					break;
				case REMOVED:
					for (int x = productionInstance.getStart(); x <= productionInstance.getEnd(); x++) {
						elementInOutput.set(x, false);
	                                }

					break;
				case REPLACED:
					for (int x = productionInstance.getStart(); x <= productionInstance.getEnd(); x++) {
						elementInOutput.set(x, false);
	                                }

					GrammarProduction<T> production = productionInstance.getProduction();
					GrammarRule<T> rule = production.getParent();
					Sentence<T> shortestConstantSentence = rule.getShortestConstantSentence();

					constantSubstitutions.set(productionInstance.getStart(), shortestConstantSentence);

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

		ProductionInstance<T> currentProductionInstance = productionInstances.get(currentPosition);

		if (currentProductionInstance.wasProductive()) {
			// Mark as REMOVED all productions that are completely enclosed within the current one

			for (int i = currentPosition + 1; i < productionInstances.size(); i++) {
				ProductionInstance<T> productionInstance = productionInstances.get(i);
				if (
					productionInstance.wasProductive()
					&& productionInstance.getStart() >= currentProductionInstance.getStart()
					&& productionInstance.getEnd() <= currentProductionInstance.getEnd()
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
	public Iterator<Sentence<T>> iterator() {
		if (iterator == null) {
			iterator = new Iterator<Sentence<T>>() {
				@Override
				public boolean hasNext() {
					// Check if there are any productions that are potential targets for minimization
					for (int i = currentPosition + 1; i < productionInstances.size(); i++) {
						if (productionStatus.get(i) == ProductionStatus.ORIGINAL) {
							return true;
						}
					}

					return false;
				}

				@Override
				public Sentence<T> next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}

					// We advance the position to the next production that can be worked on
					for (currentPosition++; currentPosition < productionInstances.size(); currentPosition++) {
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
