package ex1.src;
import java.io.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class WGraph_Algo implements weighted_graph_algorithms {
	private weighted_graph graph;

	@Override
	public void init(weighted_graph g) { this.graph = g; }

	@Override
	public weighted_graph getGraph() { return this.graph; }

	@Override
	public weighted_graph copy() {
		weighted_graph  deepCopiedGraph  = new WGraph_DS();
		//copy all nodes into new nodes, with new pointers
		for (node_info currentNode : this.graph.getV()) {
			int key = copyNodeToGraph(currentNode, deepCopiedGraph);
			for (node_info currentNeighbor: this.graph.getV(key)) {
				int neighbourKey = copyNodeToGraph(currentNeighbor, deepCopiedGraph);
				double edgeWeight = this.graph.getEdge(key, neighbourKey);
				deepCopiedGraph.connect(key, neighbourKey, edgeWeight);
			}
		}

		return deepCopiedGraph;
	}

	private static int copyNodeToGraph(node_info node, weighted_graph g) {
		int key = node.getKey();
		node_info isNodePresentInGraph = g.getNode(key);
		if (isNodePresentInGraph == null) {
			String info = node.getInfo();
			double tag = node.getTag();
			g.addNode(key);
			node_info nodeFromGraph = g.getNode(key);
			nodeFromGraph.setInfo(info);
			nodeFromGraph.setTag(tag);
		}

		return key;
	}


	@Override
	public boolean isConnected() {
		int numberOfNodeInGraph = this.graph.nodeSize();
		if(numberOfNodeInGraph == 0) return true;
		node_info startingNode = this.graph.getV().iterator().next();
		int numberOfVisited = dijkstra(this.graph, startingNode, Integer.MIN_VALUE);
		resetAllTagsAndInfos();
		return numberOfNodeInGraph == numberOfVisited;
	}

	@Override
	public double shortestPathDist(int src, int dest) {
		node_info srcNode = this.graph.getNode(src);
		node_info destNode = this.graph.getNode(dest);
		int numberOfNodeInGraph = this.graph.nodeSize();

		if (numberOfNodeInGraph == 0 || srcNode == null || destNode == null) { return -1; }

		dijkstra(this.graph, srcNode, dest);
		double destTag = destNode.getTag();
		resetAllTagsAndInfos();
		return destTag == Integer.MAX_VALUE ? -1 : destTag;
	}

	@Override
	public List<node_info> shortestPath(int src, int dest) {
		List<node_info> path = new LinkedList<>();
		node_info srcNode = this.graph.getNode(src);
		node_info destNode = this.graph.getNode(dest);
		dijkstra(this.graph, srcNode, dest);
		for (String strKey: destNode.getInfo().split(",")) {
			int key = Integer.parseInt(strKey);
			node_info nodeInPath = this.graph.getNode(key);
			path.add(nodeInPath);
		}
		resetAllTagsAndInfos();
		return path;
	}

	@Override
	public boolean save(String file) {
		try {
			FileWriter fileWriter = new FileWriter(file);
			String jsonGraphString = ((WGraph_DS)this.graph).toJsonString();
			fileWriter.write(jsonGraphString);
			fileWriter.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public boolean load(String file) {
		try (FileReader input = new FileReader(file)){
			weighted_graph graphFromJson = WGraph_DS.jsonToGraph(input);
			this.init(graphFromJson);
			input.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static int dijkstra(weighted_graph g, node_info startingNode, int targetKey) {
		int totalNumberOfNodes = g.nodeSize();
		PriorityQueue<node_info> queue = new PriorityQueue<node_info>(new CustomComparator());
		startingNode.setTag(0);
		HashSet<Integer> visited = new HashSet<>();
		queue.add(startingNode);
		while (!queue.isEmpty() && visited.size() != totalNumberOfNodes) {
			node_info currentNode = queue.remove();
			int currentKey = currentNode.getKey();
			if (!visited.contains(currentKey)) {
				visited.add(currentKey);
				if (targetKey == currentKey) { break; }
				double currentTag = currentNode.getTag();
				String currentInfo = currentNode.getInfo();
				for (node_info currentNeighbour: g.getV(currentKey)) {
					int neighbourKey = currentNeighbour.getKey();
					double neighbourTag = currentNeighbour.getTag();
					double currentEdgeWeight = g.getEdge(currentKey, neighbourKey);
					if ((currentTag + currentEdgeWeight) < neighbourTag) {
						currentNeighbour.setTag(currentTag + currentEdgeWeight);
						currentNeighbour.setInfo(currentInfo + "," + neighbourKey);
						queue.add(currentNeighbour);
					}
				}
			}
		}
		return visited.size();
	}

	private void resetAllTagsAndInfos() {
		for (node_info node: this.graph.getV()) {
			String strKey = Integer.toString(node.getKey());
			node.setTag(Integer.MAX_VALUE);
			node.setInfo(strKey);
		}
	}

	
	static class CustomComparator implements Comparator<node_info> {
		@Override
		public int compare(node_info nodeA, node_info nodeB) {
			return nodeA.getTag() < nodeB.getTag() ? -1 : 1;
		}
	}
}


