package de.dhbw.mh.lextream.lexify;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.dhbw.mh.redeggs.CodePointRange;

class SortedListSymbolStrategy extends SymbolStrategy {

	private final List<CodePointRange> symbolList = new ArrayList<>();

	SortedListSymbolStrategy(Map<Integer, CodePointRange> symbolMap) {
		symbolList.addAll(symbolList);
		symbolList.sort(Comparator.comparingInt(CodePointRange::firstCodePoint));
	}

	@Override
	int containing(int codePoint) {
		for(int i = 0; i < symbolList.size(); ++i) {
			CodePointRange range = symbolList.get(i);
			if(range.firstCodePoint <= codePoint && codePoint <= range.lastCodePoint) {
				return i;
			}
		}
		return 0;
	}

	@Override
	int numberOfGroups() {
		return symbolList.size() + 1; // group 0 = "invalid"
	}

}
