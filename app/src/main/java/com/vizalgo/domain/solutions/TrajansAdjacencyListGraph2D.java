package com.vizalgo.domain.solutions;

import java.util.HashMap;
import java.util.Iterator;

import com.vizalgo.primitives.*;

/**
 * Created by garret on 12/14/15.
 */
public class TrajansAdjacencyListGraph2D extends AdjacencyListGraph<AdjacencyListNode2D> {
    public TrajansAdjacencyListGraph2D(IAdjacencyListChangeListener listener) {
        super(listener, true);
    }

    public TrajansAdjacencyListGraph2D(AdjacencyListGraph<AdjacencyListNode2D> parentGraph,
                                       IAdjacencyListChangeListener listener) {
        super(listener, true);

        // Copy nodes from parent graph
        for (Iterator it = parentGraph.getAllNodes().entrySet().iterator(); it.hasNext();) {
            AdjacencyListNode2D node = (AdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue();
            TrajansAdjacencyListNode2D newNode = new TrajansAdjacencyListNode2D(node, false);
            newNode.setIndex(node.getIndex());
            addNode(newNode);
        }
        // Copy edges from parent graph
        for (Iterator it = parentGraph.getAllNodes().entrySet().iterator(); it.hasNext();) {
            AdjacencyListNode2D node = (AdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue();
            AdjacencyListNode2D newNode = getAllNodes().get(node.getIndex());
            for (Iterator it2 = node.getEdges().iterator();
                 it2.hasNext(); ) {
                AdjacencyListEdge2D edge = (AdjacencyListEdge2D)it2.next();
                newNode.connect(getAllNodes().get(edge.getEndNode().getIndex()),
                        TrajansAlgorithm.TraversalLineColor, edge.getWeight(), true);
            }
        }
    }
}
