package com.vizalgo.domain.problems;

import com.vizalgo.domain.IProblem;
import com.vizalgo.domain.ISolution;
import com.vizalgo.domain.solutions.TarjansAlgorithm;
import com.vizalgo.primitives.AdjacencyListGenerator2D;
import com.vizalgo.primitives.IGenerator;
import com.vizalgo.primitives.IProgressListener;
import com.vizalgo.rendering.AdjacencyList2DGraphRenderer;
import com.vizalgo.rendering.IRenderer;

import java.util.ArrayList;
import java.util.List;

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
    public List<ISolution> getSolutions(IProgressListener listener) {
        ArrayList<ISolution> s = new ArrayList<ISolution>();
        s.add(new TarjansAlgorithm());
        return s;
    }
}
