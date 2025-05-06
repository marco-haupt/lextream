package de.dhbw.mh.lextream.lexpress.internal;

import static de.dhbw.mh.redeggs.CodePointRange.range;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.dhbw.mh.lextream.lexport.Interval;
import de.dhbw.mh.redeggs.CodePointRange;

class IntervalTest {

	@Test
	void mergesOverlappingAndAdjacentRanges() {
		List<CodePointRange> input = List.of(
				range(11, 17), range(21, 21), range(5, 7), range(7, 13), range(3, 3)
		);
		List<CodePointRange> expected = List.of(
				range(3, 3), range(5, 17), range(21, 21)
		);

		List<CodePointRange> actual = Interval.mergeIntervals(input);

		assertThat(actual).containsExactlyElementsOf(expected);
	}

	@Test
	void splitsOverlappingRangesCorrectly() {
		List<CodePointRange> input = List.of(
				range(11, 17), range(21, 21), range(5, 7), range(7, 13), range(3, 3)
		);
		List<String> expected = List.of(
				range(3, 3), range(5, 6), range(7, 7), range(8, 10),
				range(11, 13), range(14, 17), range(21, 21)
		).stream().map(CodePointRange::toString).toList();

		List<String> actual = Interval.splitOverlappingIntervals(input).stream()
				.map(CodePointRange::toString).toList();

		assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
	}


	@Test
	void mergesRangesWithDuplicates() {
		List<CodePointRange> input = List.of(
				range(103, 103), range(97, 122), range(65, 90),
				range(97, 122), range(65, 90), range(48, 57)
		);
		List<CodePointRange> expected = List.of(
				range(48, 57), range(65, 90), range(97, 122)
		);

		List<CodePointRange> actual = Interval.mergeIntervals(input);

		assertThat(actual).containsExactlyElementsOf(expected);
	}

	@Test
	void splitsComplexOverlapsCorrectly() {
		List<CodePointRange> input = List.of(
				range(103, 103), range(97, 122), range(65, 90),
				range(97, 122), range(65, 90), range(48, 57)
		);
		List<CodePointRange> expected = List.of(
				range(48, 57), range(65, 90), range(97, 102),
				range(103, 103), range(104, 122)
		);

		List<CodePointRange> actual = Interval.splitOverlappingIntervals(input);

		assertThat(actual).containsExactlyElementsOf(expected);
	}

}
