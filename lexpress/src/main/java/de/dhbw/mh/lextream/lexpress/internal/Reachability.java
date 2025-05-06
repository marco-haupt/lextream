package de.dhbw.mh.lextream.lexpress.internal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

class Reachability {

	static interface ConditionalAbstraction<T, C> {
		public Collection<T> get(T current, C conditional);
	}

	static <T, C> Set<T> findReachableNodes(ConditionalAbstraction<T, C> graph, T start) {
		return findReachableNodes(graph, start, null);
	}


	static <T, C> Set<T> findReachableNodes(ConditionalAbstraction<T, C> graph, T start, C conditional) {
		return findReachableNodes(graph, start, conditional, true);
	}


	static <T, C> Set<T> findReachableNodes(ConditionalAbstraction<T, C> graph, T start, C conditional, boolean containsStartNodes) {
		Set<T> visited = new HashSet<>();
		Deque<T> stack = new ArrayDeque<>();
		stack.push(start);

		while (!stack.isEmpty()) {
			T current = stack.pop();
			if (!visited.contains(current)) {
				visited.add(current);
				for (T neighbor : graph.get(current, conditional)) {
					if (!visited.contains(neighbor)) {
						stack.push(neighbor);
					}
				}
			}
		}

		if(!containsStartNodes) {
			visited.remove(start);
		}
		return visited;
	}

}
