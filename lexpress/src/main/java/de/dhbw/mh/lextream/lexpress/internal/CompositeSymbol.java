package de.dhbw.mh.lextream.lexpress.internal;

import java.util.Comparator;
import java.util.List;

import de.dhbw.mh.redeggs.CodePointRange;
import de.dhbw.mh.redeggs.VirtualSymbol;

/**
 * A virtual symbol composed of multiple code point ranges, sorted by their starting code point.
 */
class CompositeSymbol implements VirtualSymbol {

	private final List<CodePointRange> codePointRanges;

	CompositeSymbol(final List<CodePointRange> ranges) {
		if (ranges == null || ranges.isEmpty()) {
			throw new IllegalArgumentException("Code point ranges must not be null or empty.");
		}
		codePointRanges = ranges.stream()
				.sorted(Comparator.comparingInt(CodePointRange::firstCodePoint))
				.toList();
	}

	@Override
	public List<CodePointRange> sortedCodePointRanges() {
		return codePointRanges;
	}

}
