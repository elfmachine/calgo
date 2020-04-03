package com.vizalgo.primitives;

import java.util.ArrayList;

/**
 * Created by garret on 12/11/15.
 */
public interface IAdjacencyListGraphNodeType {
    int getValue();
    int getColor();
    int getIndex();
    String getDescription();
    void setIndex(int i);
    void setValue(int v);

    int getX();

    int getY();

    IAdjacencyListGraphEdgeType connect(IAdjacencyListGraphNodeType newNode, int color, int weight,
                                        boolean isDirected);
    ArrayList<IAdjacencyListGraphEdgeType> getEdges();

    double distanceTo(IAdjacencyListGraphNodeType otherNode);

    boolean isConnectedTo(IAdjacencyListGraphNodeType otherNode);

    IAdjacencyListGraphEdgeType connect(IAdjacencyListGraphNodeType newNode, int weight,
                                               boolean isDirected);
}
