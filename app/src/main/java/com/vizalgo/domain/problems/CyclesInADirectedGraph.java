package com.vizalgo.domain.problems;

import java.util.ArrayList;

import com.vizalgo.domain.*;
import com.vizalgo.primitives.*;
import com.vizalgo.rendering.*;

import com.vizalgo.domain.solutions.TarjansAlgorithm;

/**
 * Created by garret on 12/11/15.
 */
public class CyclesInADirectedGraph implements IProblem {
    public int Nodes = 200;
    public int Edges = 200;

    private AdjacencyList2DGraphRenderer renderer;

    @Override
    public String getName() {
        return "Cycles in a directed graph";
    }

    @Override
    public void setRenderer(IRenderer renderer) {
        // TODO: Check type of renderer and throw different exception if not correct
        this.renderer = (AdjacencyList2DGraphRenderer)renderer;
    }

    @Override
    public void setDataModel(Object dataModel) {
        AdjacencyListGraphDataModel algdm = (AdjacencyListGraphDataModel)dataModel;
        Nodes = algdm.Nodes;
        Edges = algdm.Edges;
    }

    @Override
    public Object getDefaultDataModel() {
        AdjacencyListGraphDataModel algdm = new AdjacencyListGraphDataModel();
        algdm.Edges = Edges;
        algdm.Nodes = Nodes;
        return algdm;
    }

    @Override
    public IGenerator getGenerator(IProgressListener listener) {
        return new AdjacencyListGenerator2D(Nodes, Edges, renderer, listener);
    }

    @Override
    public ArrayList<ISolution> getSolutions(IProgressListener listener) {
        ArrayList<ISolution> s = new ArrayList<ISolution>();
        s.add(new TarjansAlgorithm());
        return s;
    }
}
