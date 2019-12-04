package com.vizalgo.primitives;

/**
 * Created by garret on 12/11/15.
 */
public interface IAdjacencyListChangeListener<E> {
    void onGraphCreated(AdjacencyListGraph<E> graph);
    void onNodeAdded(IAdjacencyListGraphNodeType n);
    void onNodeRemoved(IAdjacencyListGraphNodeType n);
    void onEdgeAdded(IAdjacencyListGraphEdgeType n);
    void onComplete();
}
