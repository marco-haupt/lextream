package de.dhbw.mh.lextream.lexpress.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import de.dhbw.mh.lextream.lexpress.DFA;
import de.dhbw.mh.redeggs.CodePointRange;

class Minimizer {

	private final SimpleDfa automaton;
	private final int[] distinguishableLength;
	private final int maxIterations;

	private boolean changeHappened = true;
	private int iteration;

	Minimizer(SimpleDfa dfa) {
		automaton = dfa;
		maxIterations = dfa.numberOfStates();
		distinguishableLength = new int[linearize(maxIterations, 0)];

		for(int i=0; i<distinguishableLength.length; ++i) {
			distinguishableLength[i] = Integer.MAX_VALUE;
		}

		//accepting states and non-accepting states can never be identical
		equivalenceRelation(maxIterations, (i, j) -> {
			if(dfa.acceptsInState(i) != dfa.acceptsInState(j)) {
				distinguishableLength[linearize(i, j)] = 0;
			}
		});

		iteration = 1;
		while(changeHappened) {
			nextIteration();
		}
	}


	private int[] getRepresentatives() {
		int representative[] = new int[automaton.numberOfStates()];
		for(int i=0; i<representative.length; ++i) {
			representative[i] = i;
		}

		equivalenceRelation(automaton.numberOfStates(), (i, j) -> {
			if(distinguishableLength[linearize(i, j)] == Integer.MAX_VALUE) {
				if(representative[j] < representative[i]) {
					representative[i] = representative[j];
				}
			}else {
				if(j < representative[j]) {
					representative[j] = j;
				}
			}
		});

		return representative;
	}


	static DeterministicStateMachine applyOn(SimpleDfa dfa) {
		Minimizer minimizer = new Minimizer(dfa);
		int[] representative = minimizer.getRepresentatives();
		return minimizer.constructMinDfa(representative);
	}


	DeterministicStateMachine constructMinDfa(int[] representative) {
		List<DFA.State> states = new LinkedList<>();
		Set<DFA.State> acceptingStates = new HashSet<>();
		Map<DFA.State, Map<CodePointRange, DFA.State>> transitions = new HashMap<>();

		DFA.State minStates[] = new DFA.State[representative.length];
		for(int i=0; i<representative.length; ++i) {
			if( representative[i] == i ) {
				minStates[i] = new DFA.State();
				states.add(minStates[i]);
				if(automaton.acceptsInState(i)) {
					acceptingStates.add(minStates[i]);
				}
			}else {
				minStates[i] = minStates[representative[i]];
			}
		}
		for(int i=0; i<representative.length; ++i) {
			if( representative[i] == i ) {
				Map<CodePointRange, DFA.State> newTransitions = new HashMap<>();
				transitions.put(minStates[i], newTransitions);
				for(Map.Entry<CodePointRange,Integer> origTransition : automaton.getTransitions().get(i).entrySet()) {
					newTransitions.put(origTransition.getKey(), minStates[origTransition.getValue()]);
				}
			}
		}
		DFA.State newInitialState = minStates[automaton.getInitialState()];
		return new DeterministicStateMachine(states, automaton.getSymbols(), newInitialState, transitions, acceptingStates);
	}


	private void nextIteration() {
		changeHappened = false;
		equivalenceRelation(automaton.numberOfStates(), (i, j) -> {
			// cell is already marked
			if(distinguishableLength[linearize(i, j)] != Integer.MAX_VALUE) {
				return;
			}
			for(CodePointRange symbol : automaton.getSymbols()) {
				if (distinguishable(i, j, symbol)) {
					distinguishableLength[linearize(i, j)] = iteration;
					changeHappened = true;
				}
			}
		});
	}

	private boolean distinguishable(int i, int j, CodePointRange symbol) {
		int targetA = automaton.targetOf(i, symbol);
		int targetB = automaton.targetOf(j, symbol);
		if(targetA == targetB) {
			return false;
		}
		if(distinguishableLength[lookup(targetA, targetB)] != Integer.MAX_VALUE) {
			return true;
		}
		return false;
	}

	private int lookup(int stateId1, int stateId2) {
		if(stateId1 > stateId2) {
			int temp = stateId2;
			stateId2 = stateId1;
			stateId1 = temp;
		}
		return linearize(stateId2, stateId1);
	}


	public static int linearize(int i, int j) {
		return i*(i-1)/2 + j;
	}

	public static void equivalenceRelation(int n, BiConsumer<Integer, Integer> action) {
		for(int i = 1; i < n; ++i) {
			for(int j = 0; j < i; ++j) {
				action.accept(i, j);
			}
		}
	}

}
