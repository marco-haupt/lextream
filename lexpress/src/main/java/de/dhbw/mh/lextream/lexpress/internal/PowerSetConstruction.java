package de.dhbw.mh.lextream.lexpress.internal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import de.dhbw.mh.lextream.lexport.Interval;
import de.dhbw.mh.lextream.lexpress.DFA;
import de.dhbw.mh.lextream.lexpress.NFA;
import de.dhbw.mh.lextream.lexpress.NFA.State;
import de.dhbw.mh.lextream.lexpress.internal.Reachability.ConditionalAbstraction;
import de.dhbw.mh.redeggs.CodePointRange;
import de.dhbw.mh.redeggs.VirtualSymbol;

class PowerSetConstruction {

	static DFA on(NFA nfa) {
		TransitionCache cache = new TransitionCache(nfa);
		return cache.execute();
	}


	private static class TransitionCache implements ConditionalAbstraction<NFA.State, CodePointRange> {

		private static final CodePointRange EPSILON = null;

		private final NFA nfa;
		private final Map<NFA.State, Map<CodePointRange, Set<NFA.State>>> cache;
		private final List<CodePointRange> disjointCodePointRanges;
		private final Map<BigInteger, Mapping> stateMap;
		private final NFA.StateNumbering numbering;
		private final DeterministicStateMachine.Builder dfaBuilder;

		TransitionCache(NFA nfa) {
			this.nfa = nfa;
			cache = new HashMap<>();
			stateMap = new HashMap<>();
			dfaBuilder = new DeterministicStateMachine.Builder();
			numbering = NFA.StateNumbering.forAllStatesOf(nfa);
			disjointCodePointRanges = Interval.splitOverlappingIntervals(nfa.getAllCodePointRanges());
			disjointCodePointRanges.sort(Comparator.comparingInt(CodePointRange::firstCodePoint));
			nfa.forEachTransition((origin, input, target) -> {
				if(input == EPSILON) {
					addTransition(origin, EPSILON, target);
				}else {
					for(CodePointRange range : disjointRangesFor(input)) {
						addTransition(origin, range, target);
					}
				}
			});
		}

		private DFA execute() {
			List<Mapping> mappings = new ArrayList<>();
			int mappingIndex = 0;

			Mapping initialState = mappingFor(epsilonClosure(nfa.getInitialState()));
			mappings.add(initialState);

			while(mappingIndex < mappings.size()) {
				Mapping map = mappings.get(mappingIndex);

				for(CodePointRange disjointRange : disjointCodePointRanges) {
					Set<NFA.State> targets = uniteAllTargetsOf(map.nfaStates, disjointRange);

					Mapping target = mappingFor(epsilonClosure(targets));
					dfaBuilder.addTransition(map.dfaState, disjointRange, target.dfaState);
					if(!mappings.contains(target)) {
						mappings.add(target);
					}

					nfa.forEachAcceptingState(s -> {
						if(target.nfaStates.contains(s)) {
							dfaBuilder.acceptingState(target.dfaState);
							return;
						}
					});
				}
				++mappingIndex;
			}

			dfaBuilder.initialState(initialState.dfaState);
			return dfaBuilder.finish();
		}

		Set<NFA.State> uniteAllTargetsOf(Set<NFA.State> origins, CodePointRange range) {
			Set<NFA.State> result = new HashSet<>();
			for(NFA.State origin : origins) {
				Map<CodePointRange, Set<NFA.State>> transition = cache.get(origin);
				if(transition == null) {
					continue;
				}
				Set<NFA.State> targets = transition.get(range);
				if(targets == null) {
					continue;
				}
				result.addAll(targets);
			}
			return result;
		}


		private Collection<CodePointRange> disjointRangesFor(VirtualSymbol input) {
			List<CodePointRange> result = new LinkedList<>();
			Iterator<CodePointRange> inputRanges = input.sortedCodePointRanges().iterator();
			Iterator<CodePointRange> disjointRanges = disjointCodePointRanges.iterator();

			if(!inputRanges.hasNext() || !disjointRanges.hasNext()) {
				return result;
			}

			try {
				CodePointRange inputRange = inputRanges.next();
				CodePointRange disjointRange = disjointRanges.next();

				for(;;) {
					if(inputRange.firstCodePoint <= disjointRange.firstCodePoint && disjointRange.lastCodePoint >= inputRange.lastCodePoint) {
						result.add(disjointRange);
						disjointRange = disjointRanges.next();
					} else if(inputRange.lastCodePoint < disjointRange.firstCodePoint) {
						inputRange = inputRanges.next();
					} else if(disjointRange.lastCodePoint < inputRange.firstCodePoint) {
						disjointRange = disjointRanges.next();
					} else {
						throw new RuntimeException();
					}
				}
			} catch(NoSuchElementException ex) {}
			return result;
		}

		private void addTransition(NFA.State origin, CodePointRange input, NFA.State target) {
			Map<CodePointRange, Set<NFA.State>> actions = cache.get(origin);
			if(actions == null) {
				actions = new HashMap<>();
				cache.put(origin, actions);
			}
			Set<NFA.State> targets = actions.get(input);
			if(targets == null) {
				targets = new HashSet<>();
				actions.put(input, targets);
			}
			targets.add(target);
		}

		Set<NFA.State> epsilonClosure(NFA.State start) {
			return Reachability.findReachableNodes(this, start, null);
		}

		Set<NFA.State> epsilonClosure(Set<NFA.State> stateSet) {
			Set<NFA.State> additionalStates = new HashSet<>();
			for(NFA.State state : stateSet) {
				additionalStates.addAll(epsilonClosure(state));
			}
			stateSet.addAll(additionalStates);
			return stateSet;
		}

		private BigInteger idForSetOfStates(Set<NFA.State> states) {
			BigInteger setId = BigInteger.ZERO;
			for(NFA.State state : states) {
				setId = setId.add(BigInteger.TWO.pow(numbering.get(state)));
			}
			return setId;
		}

		Mapping mappingFor(Set<NFA.State> states) {
			BigInteger setId = idForSetOfStates(states);
			Mapping mapping = stateMap.get(setId);
			if(mapping == null) {
				mapping = new Mapping(dfaBuilder.newState(), states);
				stateMap.put(setId, mapping);
			}
			return mapping;
		}

		@Override
		public Collection<State> get(State current, CodePointRange conditional) {
			Set<NFA.State> result = new HashSet<>();
			if(cache.get(current) == null) {
				return result;
			}
			if(cache.get(current).get(conditional) == null) {
				return result;
			}
			return cache.get(current).get(conditional);
		}
	}


	static class Mapping {
		final DFA.State dfaState;
		final Set<NFA.State> nfaStates;

		Mapping(DFA.State dfaState, Set<NFA.State> nfaState) {
			super();
			this.dfaState = dfaState;
			this.nfaStates = nfaState;
		}
	}

}
