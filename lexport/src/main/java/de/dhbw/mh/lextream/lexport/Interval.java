package de.dhbw.mh.lextream.lexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.dhbw.mh.redeggs.CodePointRange;

public class Interval implements Comparable<Interval> {
	
	public final int FIRST;
	public final int LAST;

	public Interval(int start, int end) {
		super();
		FIRST = start;
		LAST = end;
	}

	@Override
	public int compareTo(Interval other) {
		if (this.FIRST != other.FIRST) {
			return Integer.compare(this.FIRST, other.FIRST);
		} else {
			return Integer.compare(this.LAST, other.LAST);
		}
	}
	
	@Override
	public String toString( ){
		if( FIRST == LAST ){
			return String.format( "%s", escape(FIRST) );
		}
		return String.format( "%s-%s", escape(FIRST), escape(LAST) );
	}
	
	private String escape( int symbol ){
		switch( symbol ){
		case '\n': return "\\n";
		case '\t': return "\\t";
		case '\r': return "\\r";
		}
		return String.format( "%c", symbol);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Interval interval = (Interval) o;
		return FIRST == interval.FIRST && LAST == interval.LAST;
	}

	@Override
	public int hashCode() {
		int result = Integer.hashCode(FIRST);
		result = 31 * result + Integer.hashCode(LAST);
		return result;
	}

	public static Comparator<Interval> ascendingByIntervalStart = (p1, p2) -> p1.FIRST - p2.FIRST;

	private static int min(int... values) {
		if (values.length <= 0) {
			throw new RuntimeException();
		}
		int min = values[0];
		for (int i = 1; i < values.length; ++i) {
			if (values[i] < min) {
				min = values[i];
			}
		}
		return min;
	}

	public static List<CodePointRange> splitOverlappingIntervals(Collection<CodePointRange> intervals) {
		List<CodePointRange> result = new LinkedList<>();
		ListIterator<CodePointRange> mergedIntervals = mergeIntervals(intervals).listIterator();

		ListIterator<Integer> endPositions = Stream
				.concat(intervals.stream().map(i -> i.firstCodePoint - 1), intervals.stream().map(i -> i.lastCodePoint)).sorted()
				.distinct().collect(Collectors.toList()).listIterator();

		CodePointRange mergedInterval = mergedIntervals.next();
		int nextEndPosition = endPositions.next();

		int lastPos = mergedInterval.firstCodePoint;
		while (true) { //mergedIntervals.hasNext()
			while (nextEndPosition < lastPos) {
				nextEndPosition = endPositions.next();
			}
			int end = min(mergedInterval.lastCodePoint, nextEndPosition);
			result.add(new CodePointRange(lastPos, end));
			lastPos = end+1;
			if (end == mergedInterval.lastCodePoint) {
				if(!mergedIntervals.hasNext()) {
					break;
				}
				mergedInterval = mergedIntervals.next();
				lastPos = mergedInterval.firstCodePoint;
			}
			if (end == nextEndPosition) {
				nextEndPosition = endPositions.next();
			}
		}
		return result;
	}

	public static List<CodePointRange> mergeIntervals(Collection<CodePointRange> intervals) {
		List<CodePointRange> copy = new ArrayList<>(intervals);
//		List<CodePointRange> copy = intervals;
		if (copy.size() <= 1) {
			return copy;
		}
		copy.sort(Comparator.comparingInt(CodePointRange::firstCodePoint));
//		copy.sort(ascendingByIntervalStart);
		List<CodePointRange> result = new LinkedList<>();
		ListIterator<CodePointRange> iterator = copy.listIterator();
		
		CodePointRange previousInterval = iterator.next();
		while (iterator.hasNext()) {
			CodePointRange currentInterval = iterator.next();
			if(currentInterval.firstCodePoint >= previousInterval.firstCodePoint && currentInterval.lastCodePoint <= previousInterval.lastCodePoint) {
				//do nothing
			}else if(previousInterval.lastCodePoint + 1 >= currentInterval.firstCodePoint){
				previousInterval = new CodePointRange(previousInterval.firstCodePoint, currentInterval.lastCodePoint);
			}else {
				result.add(previousInterval);
				previousInterval = currentInterval;
			}
		}
		result.add(previousInterval);
		
		return result;
	}

	public static Set<Integer> boundariesOf(Collection<Interval> intervals) {
		Set<Integer> result = new HashSet<Integer>();
		for (Interval interval : intervals) {
			result.add(interval.FIRST - 1);
			result.add(interval.LAST + 1);
		}
		return result;
	}

}
