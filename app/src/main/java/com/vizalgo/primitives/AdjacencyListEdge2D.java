package com.vizalgo.primitives;

/**
 * Created by garret on 12/11/15.
 */
public class AdjacencyListEdge2D implements IAdjacencyListGraphEdgeType {
    private AdjacencyListNode2D startNode;
    private AdjacencyListNode2D endNode;

    private int color;

    private int weight;

    private final static int DEFAULT_COLOR = 0xFF103080;

    private boolean isDirected;

    public AdjacencyListEdge2D(int weight, AdjacencyListNode2D start, AdjacencyListNode2D end,
                               boolean isDirected) {
        this(0, weight, start, end, isDirected);
        color = DEFAULT_COLOR;
    }

    public AdjacencyListEdge2D(int color, int weight, AdjacencyListNode2D start, AdjacencyListNode2D end,
                                boolean isDirected) {
        startNode = start;
        endNode = end;
        this.color = color;
        this.weight = weight;
        this.isDirected = isDirected;
    }

    public int getColor() {
        return color;
    }

    public int getWeight() {
        return weight;
    }
    
    public boolean isDirected() {
        return isDirected;
    }

    public IAdjacencyListGraphNodeType getStartNode() {
        return startNode;
    }

    public IAdjacencyListGraphNodeType getEndNode() {
        return endNode;
    }
}
