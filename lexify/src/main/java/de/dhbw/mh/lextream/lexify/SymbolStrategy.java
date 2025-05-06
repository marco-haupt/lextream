package de.dhbw.mh.lextream.lexify;

import java.util.Map;

import de.dhbw.mh.redeggs.CodePointRange;

abstract class SymbolStrategy {

	abstract int containing(int codePoint);

	abstract int numberOfGroups();

	static SymbolStrategy basedOn(Map<Integer, CodePointRange> symbolMap) {
		ArraySymbolStrategy.Builder arrayBuilder = new ArraySymbolStrategy.Builder(symbolMap);
		if(arrayBuilder.isAppropriate()) {
			return arrayBuilder.build();
		}
		return new SortedListSymbolStrategy(symbolMap);
	}

}
