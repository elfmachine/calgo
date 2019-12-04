package com.vizalgo.domain.solutions;

import com.vizalgo.primitives.AdjacencyListNode2D;
import com.vizalgo.primitives.Constants;

/**
 * Created by garret on 12/14/15.
 */
public class TrajansAdjacencyListNode2D extends AdjacencyListNode2D {
    public int Index;
    public int LowLink;
    public boolean OnStack;

    TrajansAdjacencyListNode2D(AdjacencyListNode2D originalNode, boolean isSolution) {
        super(originalNode);
        Index = Constants.UNDEFINED;
        LowLink = Constants.UNDEFINED;
        OnStack = false;
        color = isSolution ? TrajansAlgorithm.FinalNodeColor : TrajansAlgorithm.TraversalNodeColor;
    }
}
