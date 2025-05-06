package de.dhbw.mh.lextream.lexpress.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import de.dhbw.mh.redeggs.CodePointRange;
import de.dhbw.mh.redeggs.VirtualSymbol;

abstract class RecursiveThompsonAutomaton extends ThompsonAutomaton {

	RecursiveThompsonAutomaton() {
		this(new State(), new State());
	}

	private RecursiveThompsonAutomaton(State initialState, State acceptingState) {
		super(initialState, acceptingState);
	}

	@Override
	public Set<CodePointRange> getAllCodePointRanges() {
		Set<CodePointRange> result = new HashSet<>();
		addIntervalTo(result);
		return result;
	}

	public abstract void addIntervalTo(Set<CodePointRange> intervals);


	public static final class EmptySet extends RecursiveThompsonAutomaton {

		public EmptySet() {
			super();
		}

		@Override
		public void forEachState(Consumer<State> action) {
			action.accept(initialState);
			action.accept(acceptingState);
		}

		@Override
		public void forEachTransition(Transition action) {
			return;
		}

		@Override
		public void addIntervalTo(Set<CodePointRange> intervals) {}

	}


	public static final class EmptyWord extends RecursiveThompsonAutomaton {
		public EmptyWord() {
			super();
		}

		@Override
		public void forEachState(Consumer<State> action) {
			action.accept(initialState);
			action.accept(acceptingState);
		}

		@Override
		public void forEachTransition(Transition action) {
			action.accept(initialState, null, acceptingState);
		}

		@Override
		public void addIntervalTo(Set<CodePointRange> intervals) {}

	}


	public static final class Literal extends RecursiveThompsonAutomaton {

		public final VirtualSymbol symbol;

		public Literal(VirtualSymbol symbol) {
			super();
			this.symbol = symbol;
		}

		@Override
		public void forEachState(Consumer<State> action) {
			action.accept(initialState);
			action.accept(acceptingState);
		}

		@Override
		public void forEachTransition(Transition action) {
			action.accept(initialState, symbol, acceptingState);
		}

		@Override
		public void addIntervalTo(Set<CodePointRange> intervals) {
			intervals.addAll(symbol.sortedCodePointRanges());
		}

	}


	public static final class KleeneClosure extends RecursiveThompsonAutomaton {
		public final RecursiveThompsonAutomaton base;

		public KleeneClosure(RecursiveThompsonAutomaton base) {
			super();
			this.base = base;
		}

		@Override
		public void forEachState(Consumer<State> action) {
			action.accept(initialState);
			action.accept(acceptingState);
		}

		@Override
		public void forEachTransition(Transition action) {
			action.accept(initialState, null, acceptingState);
			action.accept(initialState, null, base.initialState);
			action.accept(base.acceptingState, null, acceptingState);
			action.accept(base.acceptingState, null, base.initialState);
		}

		@Override
		public void addIntervalTo(Set<CodePointRange> intervals) {
			base.addIntervalTo(intervals);
		}

	}


	public static final class Concatenation extends RecursiveThompsonAutomaton {

		public final RecursiveThompsonAutomaton left, right;

		public Concatenation(RecursiveThompsonAutomaton left, RecursiveThompsonAutomaton right) {
			super();
			this.left = left;
			this.right = right;
		}

		@Override
		public void forEachState(Consumer<State> action) {
			action.accept(initialState);
			action.accept(acceptingState);
			left.forEachState(action);
			right.forEachState(action);
		}

		@Override
		public void forEachTransition(Transition action) {
			action.accept(initialState, null, left.initialState);
			left.forEachTransition(action);
			action.accept(left.acceptingState, null, right.initialState);
			right.forEachTransition(action);
			action.accept(right.acceptingState, null, acceptingState);
		}

		@Override
		public void addIntervalTo(Set<CodePointRange> intervals) {
			left.addIntervalTo(intervals);
			right.addIntervalTo(intervals);
		}

	}


	public static final class Alternation extends RecursiveThompsonAutomaton {

		public final RecursiveThompsonAutomaton left, right;

		public Alternation(RecursiveThompsonAutomaton left, RecursiveThompsonAutomaton right) {
			super();
			this.left = left;
			this.right = right;
		}

		@Override
		public void forEachState(Consumer<State> action) {
			action.accept(initialState);
			action.accept(acceptingState);
			left.forEachState(action);
			right.forEachState(action);
		}

		@Override
		public void forEachTransition(Transition action) {
			action.accept(initialState, null, left.initialState);
			action.accept(initialState, null, right.initialState);
			left.forEachTransition(action);
			right.forEachTransition(action);
			action.accept(left.acceptingState, null, acceptingState);
			action.accept(right.acceptingState, null, acceptingState);
		}

		@Override
		public void addIntervalTo(Set<CodePointRange> intervals) {
			left.addIntervalTo(intervals);
			right.addIntervalTo(intervals);
		}

	}

}
