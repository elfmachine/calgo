package com.vizalgo.primitives;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by garret on 12/11/15.
 */
public class AdjacencyListGraph<E> {
    private int currentIndex;
    private HashMap<Integer, E> nodes;
    private IAdjacencyListChangeListener<E> listener;
    private boolean isDirected;

    public AdjacencyListGraph(IAdjacencyListChangeListener<E> listener, boolean isDirected) {
        nodes = new HashMap<Integer, E>();
        this.listener = listener;
        this.isDirected = isDirected;
    }

    public void addNode(IAdjacencyListGraphNodeType node) {
        if (node.getIndex() == Constants.UNDEFINED) {
            node.setIndex(currentIndex++);
        }
        nodes.put(node.getIndex(), (E) node);
        if (listener != null) {
            listener.onNodeAdded(node);
        }
    }

    public void removeNode(IAdjacencyListGraphNodeType node) {
        nodes.remove(node.getIndex());

        // Remove all edges connecting the node
        for (Iterator it = nodes.entrySet().iterator(); it.hasNext();) {
            AdjacencyListNode2D graphNode = (AdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue();
            if (graphNode.getIndex() != node.getIndex()) {
                for (int i=0; i<graphNode.getEdges().size(); i++) {
                    AdjacencyListEdge2D edge = (AdjacencyListEdge2D)graphNode.getEdges().get(i);
                    if (edge.getEndNode().getIndex() == node.getIndex()) {
                        graphNode.getEdges().remove(i--);
                    }
                }
            }
        }
        if (listener != null) {
            listener.onNodeRemoved(node);
        }
    }

    public void connectNode(int index1, int index2, int color, int weight) {
        IAdjacencyListGraphEdgeType e = ((IAdjacencyListGraphNodeType)nodes.get(index1)).connect(
                (IAdjacencyListGraphNodeType) nodes.get(index2), color, weight, isDirected);
        if (listener != null) {
            listener.onEdgeAdded(e);
        }
    }

    public void connectNode(int index1, int index2, int weight) {
        IAdjacencyListGraphEdgeType e = ((IAdjacencyListGraphNodeType)nodes.get(index1)).connect(
                (IAdjacencyListGraphNodeType) nodes.get(index2), weight, isDirected);
        if (listener != null) {
            listener.onEdgeAdded(e);
        }
    }

    public HashMap<Integer, E> getAllNodes()
    {
        return nodes;
    }
}
