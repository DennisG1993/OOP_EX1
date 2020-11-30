package ex1.src;

import java.io.FileReader;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("serial")
public class WGraph_DS  implements weighted_graph, Serializable {
    private HashMap<Integer, node_info> nodes;
    private int numberOfEdges = 0;
	private int numberOfChanges = 0;

    public WGraph_DS(){
        this.nodes = new HashMap<Integer, node_info>();
    }

    @Override
    public node_info getNode(int key) { return this.nodes.get(key); }

    @Override
    public boolean hasEdge(int node1, int node2) {
    	NodeInfo firstNodeFromGraph = (NodeInfo)this.getNode(node1);
		NodeInfo secondNodeFromGraph = (NodeInfo)this.getNode(node2);
		if (firstNodeFromGraph != null && secondNodeFromGraph != null) {
			return firstNodeFromGraph.hasNeighbour(node2) && secondNodeFromGraph.hasNeighbour(node1);
		}
    	return false;
    }

    @Override
    public double getEdge(int node1, int node2) {
    	boolean isConnected = this.hasEdge(node1, node2);
    	if(isConnected) {
    		NodeInfo firstNodeFromGraph = (NodeInfo)this.getNode(node1);
    		return firstNodeFromGraph.getNeighbour(node2);
    	}
    	return -1;
    }

    @Override
    public void addNode(int key) {
    	NodeInfo newNode = new NodeInfo(key);
        node_info isInGraph = this.nodes.putIfAbsent(key, newNode);
        if (isInGraph == null) this.numberOfChanges++;
    }

    @Override
    public void connect(int node1, int node2, double w) {
    	if(w>=0 && node1 != node2) {
    		NodeInfo firstNodeFromGraph = (NodeInfo)this.getNode(node1);
    		NodeInfo secondNodeFromGraph = (NodeInfo)this.getNode(node2);
        	if (firstNodeFromGraph != null && secondNodeFromGraph != null) {
        		boolean isAlreadyConnected = this.hasEdge(node1, node2);
        		firstNodeFromGraph.addNeighbour(node2, w);
        		secondNodeFromGraph.addNeighbour(node1, w);
        		this.numberOfChanges++;
        		if (!isAlreadyConnected) this.numberOfEdges++;
        	}
        	
    	}
    }

    @Override
    public Collection<node_info> getV() { return this.nodes.values(); }

    @Override
    public Collection<node_info> getV(int node_id) {
        try {
        	NodeInfo nodeFromGraph = (NodeInfo)this.getNode(node_id);
        	Set<Integer> neighbours =  nodeFromGraph.getNeighbours().keySet();
        	return neighbours.stream().map(key -> this.getNode(key)).collect(Collectors.toCollection(ArrayList::new));
        }catch (Exception e){
            return new ArrayList<node_info>();
        }

    }

    @Override
    public node_info removeNode(int key) {
    	NodeInfo nodeFromGraph = (NodeInfo)this.getNode(key);
		if (nodeFromGraph != null) {
			Collection<Integer> keys = nodeFromGraph.getNeighbours().keySet();
			while(!keys.isEmpty()) {
				try {
					int key2 = keys.stream().findFirst().get();
					this.removeEdge(key, key2);
				} catch(Exception e) {
				}
			}
			this.nodes.remove(key);
			this.numberOfChanges++;
		}
		return nodeFromGraph;
    }

	@Override
	public void removeEdge(int node1, int node2) {
		if (hasEdge(node1, node2)) {
			NodeInfo firstNodeFromGraph = (NodeInfo)this.getNode(node1);
			NodeInfo secondNodeFromGraph = (NodeInfo)this.getNode(node2);
			firstNodeFromGraph.removeNode(secondNodeFromGraph);
			this.numberOfChanges++;
			this.numberOfEdges--;
		}
	}

    @Override
    public int nodeSize() { return this.nodes.size(); }

    @Override
    public int edgeSize() { return this.numberOfEdges; }

    @Override
    public int getMC() { return this.numberOfChanges; }
    
    @Override
    public String toString() {
        return "Graph: {\n" +
                "nodes: " + nodes.toString() +
                "\numberOfEdges: " + numberOfEdges +
                "\nnumberOfChanges: " + numberOfChanges + "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return o.toString().equals(this.toString());
    }
    
	@SuppressWarnings("unchecked")
	public String toJsonString() {
		JSONArray nodes = new JSONArray();
		for (node_info node : this.getV()) {
			JSONObject currentNode = new JSONObject();
			JSONObject neighbors = new JSONObject();
			currentNode.put("key", node.getKey());
			currentNode.put("info", node.getKey());
			for (node_info neighbor : this.getV(node.getKey())) {
				neighbors.put(neighbor.getInfo(), this.getEdge(node.getKey(), neighbor.getKey()));
			}
			currentNode.put("neighbors", neighbors);
			nodes.add(currentNode);
		}
		return nodes.toJSONString();
	}
	
	public static weighted_graph jsonToGraph(FileReader input) throws Exception {
		weighted_graph graphFromJson = new WGraph_DS();
		JSONParser parser = new JSONParser();
		try {
			JSONArray nodes = (JSONArray)parser.parse(input);
			for (Object node: nodes) {
				JSONObject currentNode = (JSONObject) node;
				graphFromJson.addNode(((Long) currentNode.get("key")).intValue());
			}
			for (Object node: nodes) {
				JSONObject currentNode = (JSONObject) node;
				int currentKey = ((Long) currentNode.get("key")).intValue();
				JSONObject neighbors = (JSONObject)currentNode.get("neighbors");
				for (Object key : neighbors.keySet()) {
					int neighborKey = Integer.parseInt(key.toString());
					double neighborTag = (Double)neighbors.get(key);
					graphFromJson.connect(currentKey, neighborKey, neighborTag);
				}
			}
			return graphFromJson;
		} catch (Exception e) {
			throw e;
		}
	}
    static class NodeInfo implements node_info, Serializable {
        private static int numberOfCreatedInstances = 0;
        private int key;
        private String info;
        private double tag = Integer.MAX_VALUE;
        private HashMap<Integer, Double> neighbors;
        
        public NodeInfo(){
            this.key = numberOfCreatedInstances++;
            this.info = Integer.toString(this.key);
            this.tag = Integer.MAX_VALUE;
            this.neighbors = new HashMap<Integer, Double>();
        }

        public NodeInfo(int key){
            this.key = key;
            this.info = Integer.toString(key);
            numberOfCreatedInstances += key;
            this.neighbors = new HashMap<Integer, Double>();
        }
        
        public NodeInfo(int key, String info, double tag){
            this.key = key;
            this.info = info;
            this.tag = tag;
            numberOfCreatedInstances += key;
            this.neighbors = new HashMap<Integer, Double>();
        }

        @Override
        public int getKey() { return this.key; }

        @Override
        public String getInfo() { return this.info; }

        @Override
        public void setInfo(String s) { this.info = s; }

        @Override
        public double getTag() { return this.tag; }

        @Override
        public void setTag(double t) { this.tag = t; }

        public boolean hasNeighbour(int key) { return neighbors.containsKey(key); }

        public HashMap<Integer, Double> getNeighbours() { return this.neighbors; }

        public double getNeighbour(int key) { return this.neighbors.get(key); }

        public void removeNode(NodeInfo node) {
    		int key = node.getKey();
    		if (this.hasNeighbour(key)) {
    			this.neighbors.remove(key);
    			if(node.hasNeighbour(this.key)) {
    				node.removeNode(this);
    			}
    		}
    	}
        
        public void addNeighbour(int key, double dist) { this.neighbors.put(key, dist); }

        @Override
    	public String toString() { return "{\nkey: " + this.getKey() +"\ninfo: " + this.getInfo() + "\ntag: " + this.getTag() +"\nnodes: " + this.neighbors.keySet() + "\n}"; }
    }
}
