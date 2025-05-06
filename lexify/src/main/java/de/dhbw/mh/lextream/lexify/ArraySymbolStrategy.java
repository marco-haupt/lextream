package de.dhbw.mh.lextream.lexify;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.dhbw.mh.redeggs.CodePointRange;

/**
 * A symbol strategy that maps code points to groups using a compact array.
 */
class ArraySymbolStrategy extends SymbolStrategy {

	private final int[] symbolGroup;
	private final int offset;
	private final int numberOfGroups;

	private ArraySymbolStrategy(int lowestCodepoint, int highestCodepoint, Set<Entry<Integer, CodePointRange>> symbolMap) {
		offset = lowestCodepoint;
		symbolGroup = new int[highestCodepoint - lowestCodepoint + 1];
		numberOfGroups = symbolMap.size() + 1; // Group 0 is reserved for "invalid"
		for(Map.Entry<Integer, CodePointRange> entry : symbolMap) {
			int groupId = entry.getKey();
			CodePointRange range = entry.getValue();
			for(int cp = range.firstCodePoint; cp <= range.lastCodePoint; ++cp) {
				symbolGroup[cp - offset] = groupId;
			}
		}
	}

	@Override
	int containing(int codePoint) {
		int index = codePoint - offset;
		return (index >= 0 && index < symbolGroup.length) ? symbolGroup[index] : 0;
	}

	@Override
	int numberOfGroups() {
		return numberOfGroups;
	}


	/**
	 * Builder for {@link ArraySymbolStrategy}. It determines if an array-based
	 * symbol strategy is efficient given the code point distribution.
	 */
	static class Builder {
		private final int lowestCodepoint;
		private final int highestCodepoint;
		Set<Entry<Integer, CodePointRange>> symbolMap;

		Builder(Map<Integer, CodePointRange> symbolMap) {
			this.symbolMap = symbolMap.entrySet();
			int lowestCodepoint = Integer.MAX_VALUE;
			int highestCodepoint = Integer.MIN_VALUE;
			for(Map.Entry<Integer, CodePointRange> entry : this.symbolMap) {
				CodePointRange range = entry.getValue();
				lowestCodepoint = Math.min(lowestCodepoint, range.firstCodePoint);
				highestCodepoint = Math.max(highestCodepoint, range.lastCodePoint);
			}
			this.lowestCodepoint = lowestCodepoint;
			this.highestCodepoint = highestCodepoint;
		}

		/**
		 * Determines whether an array strategy is space-efficient.
		 */
		boolean isAppropriate() {
			return highestCodepoint - lowestCodepoint < 1024;
		}

		/**
		 * Builds the {@link ArraySymbolStrategy}.
		 */
		ArraySymbolStrategy build() {
			return new ArraySymbolStrategy(lowestCodepoint, highestCodepoint, symbolMap);
		}
	}

}
