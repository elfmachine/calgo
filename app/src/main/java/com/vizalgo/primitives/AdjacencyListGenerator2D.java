package com.vizalgo.primitives;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by garret on 12/11/15.
 */
public class AdjacencyListGenerator2D implements IGenerator {
    private int minXCoordinate = 0;
    private int minYCoordinate = 0;
    private int maxXCoordinate = 100;
    private int maxYCoordinate = 100;

    private static final String[] methodList = { "Shortest Unconnected", "Next Closest",
                                                "Cycle Creating", "Spanning Tree with Back Connections"};

    private Dictionary<Integer, String> methods;

    private int method = 0;

    private int height, width;

    private double quadrantSize;

    private double nodeX, nodeY;

    private int nodes, edges;

    private IAdjacencyListChangeListener listener;

    private IProgressListener progressListener;

    public AdjacencyListGenerator2D() {
        // Generator with only method list
        // TODO: Move to meta-class
        methods = new Hashtable<>();
        int i = 0;
        for (String s : methodList) {
            methods.put(i++, s);
        }
    }

    public AdjacencyListGenerator2D(int nodes, int edges, IAdjacencyListChangeListener listener,
                                    IProgressListener progressListener) {
        this();
        this.nodes = nodes;
        this.edges = edges;
        this.listener = listener;
        this.progressListener = progressListener;
    }

    /*
        Generate a 2D connected graph of points given the number of nodes and edges.
     */
    @Override
    public Object generate(int method) {
        AdjacencyListGraph<AdjacencyListNode2D> graph = new AdjacencyListGraph<AdjacencyListNode2D>(listener, true);
        listener.onGraphCreated(graph);

        generateUniformNodeDistribution(graph);

        // TODO: Use enum?
        switch (method) {
            case 0:
                connectEdgesUsingShortestUnconnectedMethod(graph);
                break;

            case 1:
                connectEdgesUsingNextClosestMethodWithForking(graph);
                break;

            case 2:
                connectEdgesUsingCycleCreatingMethod(graph);
                break;

            case 3:
                connectEdgesUsingSpanningTreeWithBackConnections(graph);
                break;
        }

        listener.onComplete();
        return graph;
    }

    @Override
    public void setCoordinates(int x0, int y0, int x1, int y1) {
        minXCoordinate = x0;
        minYCoordinate = y0;
        maxXCoordinate = x1;
        maxYCoordinate = y1;
    }

    @Override
    public Dictionary<Integer, String> getMethods() {
        return methods;
    }

    private void generateUniformNodeDistribution(AdjacencyListGraph<AdjacencyListNode2D> graph) {
        // Divide the provided area up into quadrants and generate a node in proximity to
        // each quadrant

        width = maxXCoordinate - minXCoordinate;
        height = maxYCoordinate - minYCoordinate;

        // Determine the multiplier to create a grid where the number of desired nodes are
        // evenly distributed

        quadrantSize = Math.sqrt((width * height)/nodes);

        // A grid per se will result in an empty area at the bottom right if the number of
        // desired nodes does not divide evenly into the grid area.  In order to provide
        // a uniform distribution of nodes, visualize "unrolling" the grid into a one-dimensional
        // line of quadrants which have a nonintegral size.  Wrap that line back into the grid
        // by incrementing each quadrant size by the calculated width and add the remainder
        // into each successive row.

        // TODO: Verify that this generates correct and uniform points.  Unit test?

        System.out.println("Width is " + width + ", height is " + height);
        System.out.println("Quadrant size is " + quadrantSize);
        double x = minXCoordinate, y = minYCoordinate;

        //int color =  (int)(Math.random() * 4000000000.0);

        for (int i=0; i<nodes; i++)
        {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            //System.out.println("Creating node " + i);
            getNodeCoordinates(x, y);

            graph.addNode(new AdjacencyListNode2D((int)Math.round(nodeX),
                    (int)Math.round(nodeY)/*,  color*/));

            x += quadrantSize;
            if (x > maxXCoordinate) {
                x -= width;
                y += quadrantSize;
            }
            progressListener.onProgress(50 * i / nodes);
        }
    }

    private void connectEdgesUsingShortestUnconnectedMethod(AdjacencyListGraph<AdjacencyListNode2D> graph) {
        ArrayList<AdjacencyListNode2D> unconnectedNodes = new ArrayList<>();
        ArrayList<AdjacencyListNode2D> connectedNodes = new ArrayList<>();

        // Create edges in the graph by repeatedly creating sets of "unconnected" nodes and
        // connecting to the closest connected node in the graph.  This has the effect of first
        // making sure all nodes in the graph are connected, and then repeatedly adding layers
        // of n-level connectivity to the list of nodes in sequence.  This should be a
        // O(edges * nodes) time algorithm with running time O(edges * nodes/2).
        boolean gotFirstConnection = false;
        boolean allNodesConnected = false;
        for (int i =0; i<edges; i++) {

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            // Connect the connected graph to the set of unconnected nodes one at a time, using
            // the shortest possible non-existing connection
            if (unconnectedNodes.size() == 0) {
                HashMap<Integer, AdjacencyListNode2D> nodeMap = graph.getAllNodes();
                for (Iterator it = nodeMap.entrySet().iterator();
                     it.hasNext(); ) {
                    unconnectedNodes.add((AdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue());
                }
                System.out.println("There are now " + unconnectedNodes.size() + " unconnected nodes.");
                if (gotFirstConnection) {
                    allNodesConnected = true;
                }
            }

            AdjacencyListNode2D nodeToCheck = unconnectedNodes.get(0);
            //System.out.println("Checking node 0 of " + unconnectedNodes.size() + ": " + nodeToCheck.getDescription());
            AdjacencyListNode2D nodeToConnect = findClosestNode(
                    gotFirstConnection ? connectedNodes : unconnectedNodes,
                    nodeToCheck);

            if (nodeToConnect == null) {
                // This indicates a bug in the algorithm
                System.out.println("Warning: Node to connect is null!");
            }
            else {
                System.out.println("Connecting to node " + nodeToConnect.getDescription());
                graph.connectNode(nodeToCheck.getIndex(), nodeToConnect.getIndex(), 0);
            }

            // TODO: Determine if this data structure is optimal for this particular problem.
            // Maybe use a queue or something?
            unconnectedNodes.remove(0);
            if (!allNodesConnected) {
                connectedNodes.add(nodeToCheck);
            }
            if (!gotFirstConnection) {
                unconnectedNodes.remove(nodeToConnect);
                gotFirstConnection = true;
                if (!allNodesConnected) {
                    connectedNodes.add(nodeToConnect);
                }
            }
            progressListener.onProgress(50 + 50 * i / edges);
        }
    }

    private void connectEdgesUsingSpanningTreeWithBackConnections(AdjacencyListGraph<AdjacencyListNode2D> graph) {
        ArrayList<AdjacencyListNode2D> unconnectedNodes = new ArrayList<>();
        ArrayList<AdjacencyListNode2D> connectedNodes = new ArrayList<>();

        boolean gotFirstConnection = false;
        boolean allNodesConnected = false;

        HashMap<Integer, AdjacencyListNode2D> nodeMap = graph.getAllNodes();
        for (Iterator it = nodeMap.entrySet().iterator();
             it.hasNext(); ) {
            unconnectedNodes.add((AdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue());
        }
        System.out.println("There are now " + unconnectedNodes.size() + " unconnected nodes.");

        AdjacencyListNode2D nodeToCheck = unconnectedNodes.get((int)(Math.random() * connectedNodes.size()));

        for (int i =0; i<edges; i++) {

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            // Use remaining edges to create random back connections

            if (allNodesConnected) {
                // Randomly connect nodes to earlier nodes in the cycle.  This does not guarantee
                // that we are creating a cycle, but it will much of the time since earlier nodes
                // are likely to be parent nodes
                int leafIndex = (int)(Math.random() * (connectedNodes.size() - 1));
                AdjacencyListNode2D leaf = connectedNodes.get(leafIndex);
                AdjacencyListNode2D parent = connectedNodes.get((int)(Math.random() * (leafIndex - 1)));

                // TODO: Retry on failure. This may create less than the desired number of edges
                if (!(leaf).isConnectedTo(parent)) {
                    System.out.println("Successfully connected leaf " + leaf.getDescription()
                                        + " to parent " + parent.getDescription());
                    graph.connectNode(leaf.getIndex(), parent.getIndex(), 0);
                }
                else {
                    System.out.println("Could not leaf " + leaf.getDescription()
                            + " to parent " + parent.getDescription());
                }
            }
            else {

                //System.out.println("Checking node 0 of " + unconnectedNodes.size() + ": " + nodeToCheck.getDescription());
                AdjacencyListNode2D nodeToConnect = findClosestNode(unconnectedNodes, nodeToCheck);

                if (nodeToConnect == null) {
                    // This indicates a bug in the algorithm
                    System.out.println("Warning: Node to connect is null!");
                    if (unconnectedNodes.isEmpty()) {
                        allNodesConnected = true;
                    }
                    else {
                        nodeToCheck = unconnectedNodes.get((int)(Math.random() * unconnectedNodes.size()));
                    }
                    continue;
                } else {
                    System.out.println("Connecting to node " + nodeToConnect.getDescription());
                    graph.connectNode(nodeToCheck.getIndex(), nodeToConnect.getIndex(), 0);
                }

                // TODO: Determine if this data structure is optimal for this particular problem.
                // Maybe use a queue or something?
                unconnectedNodes.remove(nodeToConnect);
                connectedNodes.add(nodeToCheck);
                if (!gotFirstConnection) {
                    unconnectedNodes.remove(nodeToConnect);
                    gotFirstConnection = true;
                }
                if (unconnectedNodes.isEmpty()) {
                    allNodesConnected = true;
                }
                // About half of the time, bump to a new node
                if (Math.random() < 0.5) {
                    // Half the time, it will be the child we just connected, half the time it
                    // will be some random connected node
                    if (Math.random() < 0.5) {
                        nodeToCheck = nodeToConnect;
                    } else {
                        nodeToCheck = connectedNodes.get((int)(Math.random() * connectedNodes.size()));
                    }
                }
            }
            progressListener.onProgress(50 + 50 * i / edges);
        }
    }

    private void connectEdgesUsingNextClosestMethodWithForking(AdjacencyListGraph<AdjacencyListNode2D> graph) {

        // TODO: Combine with previous method

        ArrayList<AdjacencyListNode2D> unconnectedNodes = new ArrayList<>();

        AdjacencyListNode2D nodeToCheck = null;
        for (int i =0; i<edges; i++) {

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            boolean removeFirstNode = false;
            // Connect the connected graph to the set of unconnected nodes one at a time, using
            // the shortest possible non-existing connection
            if (unconnectedNodes.size() == 0) {
                HashMap<Integer, AdjacencyListNode2D> nodeMap = graph.getAllNodes();
                for (Iterator it = nodeMap.entrySet().iterator();
                     it.hasNext(); ) {
                    unconnectedNodes.add((AdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue());
                }
                System.out.println("There are now " + unconnectedNodes.size() + " unconnected nodes.");
            }
            if (nodeToCheck == null) {
                nodeToCheck = unconnectedNodes.get(0);
                removeFirstNode = true;
            }

            AdjacencyListNode2D nodeToConnect;
            do {
                //System.out.println("Checking node 0 of " + unconnectedNodes.size() + ": " + nodeToCheck.getDescription());
                nodeToConnect = findClosestNode(unconnectedNodes, nodeToCheck);

                if (nodeToConnect == null) {
                    // This indicates a bug in the algorithm (or too many edges?)
                    System.out.println("Warning: Node to connect is null!");
                    unconnectedNodes.clear();
                    continue;
                } else {
                    System.out.println("Connecting to node " + nodeToConnect.getDescription());
                    graph.connectNode(nodeToCheck.getIndex(), nodeToConnect.getIndex(), 0);
                }

                // TODO: Determine if this data structure is optimal for this particular problem.
                // Maybe use a queue or something?
                unconnectedNodes.remove(nodeToConnect);
                if (removeFirstNode) {
                    unconnectedNodes.remove(nodeToCheck);
                }
            }
            while (unconnectedNodes.size() > 0 && Math.random() < 0.5); // fork randomly
            nodeToCheck = nodeToConnect;
            progressListener.onProgress(50 + 50 * i / edges);
        }
    }

    private void connectEdgesUsingCycleCreatingMethod(AdjacencyListGraph<AdjacencyListNode2D> graph) {

        // TODO: Combine with previous method

        ArrayList<AdjacencyListNode2D> unconnectedNodes = new ArrayList<>();

        AdjacencyListNode2D nodeToCheck = null;
        AdjacencyListNode2D rootNode = null;
        boolean removeFirstNode = true;
        for (int i =0; i<edges; i++) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            // Connect the connected graph to the set of unconnected nodes one at a time, using
            // the shortest possible non-existing connection
            if (unconnectedNodes.size() == 0) {
                HashMap<Integer, AdjacencyListNode2D> nodeMap = graph.getAllNodes();
                for (Iterator it = nodeMap.entrySet().iterator();
                     it.hasNext(); ) {
                    unconnectedNodes.add((AdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue());
                }
                System.out.println("There are now " + unconnectedNodes.size() + " unconnected nodes.");
            }
            if (nodeToCheck == null) {
                rootNode = unconnectedNodes.get((int)(Math.random() * unconnectedNodes.size()));
                nodeToCheck = rootNode;
            }

            //System.out.println("Checking node 0 of " + unconnectedNodes.size() + ": " + nodeToCheck.getDescription());
            AdjacencyListNode2D nodeToConnect = findClosestNode(unconnectedNodes, nodeToCheck);

            if (nodeToConnect == null) {
                // This indicates a bug in the algorithm (or too many edges?)
                System.out.println("Warning: Node to connect is null!");
                unconnectedNodes.clear();
                continue;
            } else {
                System.out.println("Connecting to node " + nodeToConnect.getDescription());
                graph.connectNode(nodeToCheck.getIndex(), nodeToConnect.getIndex(), 0);
            }

            // TODO: Determine if this data structure is optimal for this particular problem.
            // Maybe use a queue or something?
            unconnectedNodes.remove(nodeToConnect);
            if (removeFirstNode) {
                unconnectedNodes.remove(nodeToCheck);
                removeFirstNode = false;
            }

            // With a probability as such that there will be on average 5 graphs per cycle,
            // create a cycle by connecting back to the root node.
            if (nodeToConnect.getIndex() != rootNode.getIndex()
                    && !nodeToConnect.isConnectedTo(rootNode)
                    && !rootNode.isConnectedTo(nodeToConnect)
                    //&& Math.random() < 5.0 / unconnectedNodes.size())
                    && Math.random() < 0.2) {
                System.out.println("Connecting to node " + rootNode.getDescription());
                graph.connectNode(nodeToConnect.getIndex(), rootNode.getIndex(), 0);
                nodeToCheck = null;
            }
            else {
                nodeToCheck = nodeToConnect;
            }
            progressListener.onProgress(50 + 50 * i / edges);
        }
    }

    private AdjacencyListNode2D findClosestNode(
            Collection<AdjacencyListNode2D> nodeList, AdjacencyListNode2D node) {

        AdjacencyListNode2D closestNode = null;
        double runningShortestDistance = Double.MAX_VALUE;

        //System.out.println("Checking shortest node from " + node.getIndex());

        // Iterate through each node and find the closest connected node to the node we are
        // checking that is not connected to the node we are checking.
        for (Iterator it = nodeList.iterator(); it.hasNext(); ) {
            AdjacencyListNode2D otherNode = (AdjacencyListNode2D)it.next();
            //System.out.println("Checking shortest node to " + otherNode.getIndex());
            if (node.getIndex() != otherNode.getIndex()) { // Do not allow connections to self (loops?)
                //System.out.println("Different node");
                if (!node.isConnectedTo(otherNode)
                        && !otherNode.isConnectedTo(node)) {  // Avoid creating bidirectional connections
                    double distanceToCheck = node.distanceTo(otherNode);
                    //System.out.println("Node is not connected to other node, distance is " + distanceToCheck);
                    if (distanceToCheck < runningShortestDistance) { // Save new closest unconnected node
                        //System.out.println("New shortest distance!");
                        runningShortestDistance = distanceToCheck;
                        closestNode = otherNode;
                    }
                }
            }
        }
        return closestNode;
    }

    private void getNodeCoordinates(double x, double y) {
        double adjusted_y = y;
        nodeX = x + Math.random() * quadrantSize;

        // Account for horizontal underflow or overflow by jumping up or down a quadrant
        if (nodeX < maxXCoordinate) {
            nodeX += width;
            adjusted_y -= quadrantSize;
        }

        if (nodeX > maxXCoordinate) {
            nodeX -= width;
            adjusted_y += quadrantSize;
        }

        // Bottom row may be compressed because this algorithm actually sucks.  Account for that.
        double maxHeight = adjusted_y + quadrantSize > maxYCoordinate
                                ? maxYCoordinate - adjusted_y : quadrantSize;
        nodeY = adjusted_y + Math.random() * maxHeight;

        //System.out.println("(" + x + "," + y + ") --> (" + nodeX + "," + nodeY + ")");
    }
}
