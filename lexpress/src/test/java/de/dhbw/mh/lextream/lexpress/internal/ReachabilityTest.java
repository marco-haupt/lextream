package de.dhbw.mh.lextream.lexpress.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.dhbw.mh.lextream.lexpress.internal.Reachability.ConditionalAbstraction;

class ReachabilityTest {

	private static class GraphAdapter implements ConditionalAbstraction<String, String> {

		private final Map<String, List<String>> graph;

		GraphAdapter(Map<String, List<String>> graph) {
			this.graph = graph;
		}

		@Override
		public Collection<String> get(String current, String conditional) {
			return graph.getOrDefault(current, List.of());
		}

	}


	@Test
	void findsAllReachableNodes() {
		Map<String, List<String>> graph = new HashMap<>();
		graph.put("A", List.of("B", "C"));
		graph.put("B", List.of("D"));
		graph.put("C", List.of("E"));
		graph.put("D", List.of("F"));
		graph.put("E", List.of());
		graph.put("F", List.of("C")); // cycle back to C

		GraphAdapter adapter = new GraphAdapter(graph);
		Set<String> reachable = Reachability.findReachableNodes(adapter, "A", null, false);

		assertThat(reachable).containsExactlyInAnyOrder("B", "C", "D", "F", "E");
	}

}
