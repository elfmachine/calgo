package com.vizalgo.primitives;

/**
 * Created by garret on 12/11/15.
 */
public interface IAdjacencyListGraphEdgeType {
    boolean isDirected();
    int getColor();
    int getWeight();
    IAdjacencyListGraphNodeType getStartNode();
    IAdjacencyListGraphNodeType getEndNode();
}
