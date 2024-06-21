import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// Drithi Madagani
// ULID :- DMADAGA
// STUDENT ID - 807517185
// I wrote my code from scratch and did not copy it from others.
///**
// * Copyright (c) [Drithi Madagani]
//11/15/23 to 11/18/2023
public class preflowPush {
    private int[][] capacity; // Capacity graph
    private int[][] flow; // Flow graph
    private Map<Integer, NodeProperty> nodeProperties; // Properties of each node
    private Set<Integer> activeNodes; // Set of active nodes
    // 11/15/23 to 11/18/23

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java PreflowPush <input_file>");
            return;
        }

        String inputFileName = args[0];
        System.out.println("Pre-Flow Push Algorithm: ");
        try {
            List<Graph> graphs = parseGraphs(inputFileName);
            for (int i = 0; i < graphs.size(); i++) {
                Graph graph = graphs.get(i);
                preflowPush algorithm = new preflowPush(graph.getSize());

                for (Edge edge : graph.getEdges()) {
                    algorithm.capacity[edge.u][edge.v] = (int) edge.weight;
                }

                long startTime = System.currentTimeMillis();
                int maxFlow = algorithm.preflowPush(0, graph.getSize() - 1, graph.getSize());
                long endTime = System.currentTimeMillis();

                System.out.println("** G" + (i + 1) + ": |V|=" + graph.getSize());
                if (graph.getSize() <= 10) {
                    printFlowNetwork(graph);
                }
                System.out.println("Max flow ==> " + maxFlow + ".0 (" + (endTime - startTime) + " ms)");
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        System.out.println("*** Asg 7 by Drithi Madagani ***");
    }

    private static void printFlowNetwork(Graph graph) {
        System.out.println("       Flow network:");
        System.out.printf("%10s", " "); // Adjust the indentation
        for (int i = 0; i < graph.getSize(); i++) {
            System.out.printf("%4d", i);
        }
        System.out.println();
        System.out.print("         ");
        for (int i = 0; i < graph.getSize(); i++) {
            System.out.print("----");
        }
        System.out.println();
        for (int i = 0; i < graph.getSize(); i++) {
            System.out.printf("%4d:  ", i);
            List<Edge> edges = graph.getEdges();
            for (int j = 0; j < graph.getSize(); j++) {
                double weight = 0;
                for (Edge edge : edges) {
                    if (edge.u == i && edge.v == j) {
                        weight = edge.weight;
                        break;
                    }
                }
                System.out.printf("%4s", weight != 0 ? String.format("%4.0f", weight) : "-");
            }
            System.out.println();
        }
    }

    class NodeProperty {
        int height;
        int excess;

        NodeProperty(int height, int excess) {
            this.height = height;
            this.excess = excess;
        }
    }

    // 11/15/23 to 11/18/23
    public preflowPush(int vertexCount) {
        if (vertexCount <= 0) {
            throw new IllegalArgumentException("Vertex count must be positive.");
        }

        // Initializing the capacity matrix
        capacity = new int[vertexCount][vertexCount];

        // Initializing the flow matrix
        flow = new int[vertexCount][vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            System.arraycopy(capacity[i], 0, flow[i], 0, vertexCount);
        }

        // Initializing the node properties
        nodeProperties = new HashMap<>();
        for (int i = 0; i < vertexCount; i++) {
            nodeProperties.put(i, new NodeProperty(0, 0));
        }

        // Initialize the set for active nodes
        activeNodes = new HashSet<>();
    }

    private int calculateFlowPossible(int fromNode, int toNode) {
        int availableCapacity = capacity[fromNode][toNode] - flow[fromNode][toNode];
        int excessFlow = nodeProperties.get(fromNode).excess;
        return Math.min(excessFlow, availableCapacity);
    }

    private void preflow(int sourceNode, int numOfVertices) {
        initializeSourceNode(sourceNode, numOfVertices);
        updateInitialFlows(sourceNode, numOfVertices);
        manageActiveNodes(sourceNode, numOfVertices);
    }

    private void initializeSourceNode(int sourceNode, int numOfVertices) {
        nodeProperties.get(sourceNode).height = numOfVertices;
    }

    private void updateInitialFlows(int sourceNode, int numOfVertices) {
        int vertex = 0;
        while (vertex < numOfVertices) {
            int initialFlow = capacity[sourceNode][vertex];
            if (initialFlow > 0) {
                updateFlow(sourceNode, vertex, initialFlow);
            }
            vertex++;
        }
    }

    private void updateFlow(int fromNode, int toNode, int flowValue) {
        flow[fromNode][toNode] = flowValue;
        flow[toNode][fromNode] = -flowValue;
        nodeProperties.get(toNode).excess = flowValue;
    }

    private void manageActiveNodes(int sourceNode, int numOfVertices) {
        // Initialize a variable to iterate through vertices
        int vertex = 0;

        // Iterate through all vertices
        while (vertex < numOfVertices) {
            // Check if the vertex is not the source node and not the sink node
            if (vertex != sourceNode && vertex != numOfVertices - 1) {
                // Get the excess flow at the current vertex
                int excessFlow = nodeProperties.get(vertex).excess;

                // Check if the vertex has excess flow (greater than zero)
                if (excessFlow > 0) {
                    // If it has excess flow, add the vertex to the set of active nodes
                    activeNodes.add(vertex);
                }
            }

            // Move to the next vertex
            vertex++;
        }
    }

    // 11/15/23 to 11/18/23
    private void push(int fromNode, int toNode) {
        int flowPossible = calculateFlowPossible(fromNode, toNode);

        if (flowPossible > 0) {
            // Increase flow from fromNode to toNode
            flow[fromNode][toNode] = flow[fromNode][toNode] + flowPossible;

            // Decrease flow from toNode to fromNode (since it's the residual flow)
            flow[toNode][fromNode] = flow[toNode][fromNode] - flowPossible;

            // Adjust excess flow at both nodes
            NodeProperty fromNodeProp = nodeProperties.get(fromNode);
            NodeProperty toNodeProp = nodeProperties.get(toNode);

            fromNodeProp.excess = fromNodeProp.excess - flowPossible;
            toNodeProp.excess = toNodeProp.excess + flowPossible;

            // Check and add toNode to activeNodes if it's not there and is not sink or
            // source
            boolean shouldAddToActiveNodes = isNotSinkOrSource(toNode) && toNodeProp.excess > 0;
            if (shouldAddToActiveNodes) {
                activeNodes.add(toNode);
            }

        }
    }

    private boolean isNotSinkOrSource(int node) {
        return node != 0 && node != nodeProperties.size() - 1;
    }

    private void relabel(int currentNode, int totalVertices) {
        // Find the minimum height of an adjacent node with residual capacity
        int minHeightAdjacent = findMinHeightAdjacent(currentNode, totalVertices);

        // Update the height of the current node if there is an adjacent node with
        // residual capacity
        if (minHeightAdjacent < Integer.MAX_VALUE) {
            nodeProperties.get(currentNode).height = minHeightAdjacent + 1;
        }
    }

    // 11/15/23 to 11/18/23
    private int findMinHeightAdjacent(int currentNode, int totalVertices) {
        int minHeightAdjacent = Integer.MAX_VALUE;

        // Iterate through all vertices to find the minimum height of an adjacent node
        // with residual capacity
        for (int adjacentNode = 0; adjacentNode < totalVertices; adjacentNode++) {
            int residualCapacity = capacity[currentNode][adjacentNode] - flow[currentNode][adjacentNode];

            if (residualCapacity > 0 && nodeProperties.get(adjacentNode).height < minHeightAdjacent) {
                minHeightAdjacent = nodeProperties.get(adjacentNode).height;
            }
        }

        return minHeightAdjacent;
    }

    public int preflowPush(int source, int target, int numVertices) {
        preflow(source, numVertices);
        while (!activeNodes.isEmpty()) {
            int currentNode = activeNodes.iterator().next();
            activeNodes.remove(currentNode);

            if (currentNode != source && currentNode != target) {
                NodeProperty currentNodeProps = nodeProperties.get(currentNode);
                int oldHeight = currentNodeProps.height;

                int neighbor = 0;
                while (neighbor < numVertices) {
                    if (capacity[currentNode][neighbor] - flow[currentNode][neighbor] > 0 &&
                            currentNodeProps.height > nodeProperties.get(neighbor).height) {
                        push(currentNode, neighbor);
                    }
                    neighbor++;
                }

                if (currentNodeProps.height == oldHeight || currentNodeProps.excess > 0) {
                    if (currentNodeProps.height == oldHeight) {
                        relabel(currentNode, numVertices);
                    }

                    if (currentNodeProps.excess > 0) {
                        activeNodes.add(currentNode);
                    }
                }
            }
        }

        return nodeProperties.get(target).excess;
    }

    // 11/15/23 to 11/18/23
    private static List<Graph> parseGraphs(String path) throws IOException {
        List<Graph> graphs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            boolean inGraphSection = false;
            Graph currentGraph = null;

            while ((line = reader.readLine()) != null) {
                if (line.matches("\\*\\* G\\d+:.*")) {
                    if (currentGraph != null) {
                        graphs.add(currentGraph);
                    }
                    currentGraph = new Graph();
                    inGraphSection = true;
                } else if (inGraphSection && !line.startsWith("-")) {
                    parseGraphEdges(currentGraph, line);
                } else if (inGraphSection && line.startsWith("-")) {
                    inGraphSection = false;
                }
            }

            if (currentGraph != null) {
                graphs.add(currentGraph);
            }
        }
        return graphs;
    }

    private static void parseGraphEdges(Graph graph, String line) {
        if (line.contains("(")) {
            String[] parts = line.substring(line.indexOf('(') + 1, line.indexOf(')')).split(",");
            try {
                int u = Integer.parseInt(parts[0].trim());
                int v = Integer.parseInt(parts[1].trim());
                double weight = Double.parseDouble(parts[2].trim());
                graph.addEdge(u, v, weight);
            } catch (NumberFormatException e) {
                // Handle parsing errors
            }
        }
    }
}

class Graph {
    private List<Edge> edges;
    private int size;

    public Graph() {
        size = 0;
        edges = new ArrayList<>();
    }

    public void addEdge(int u, int v, double weight) {
        edges.add(new Edge(u, v, weight));
        size = Math.max(size, Math.max(u, v) + 1);
    }

    public int getSize() {
        return size;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}

class Edge {
    int u, v;
    double weight, flow;

    Edge(int u, int v, double weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
        this.flow = 0;
    }
}
