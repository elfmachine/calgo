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
    public int RenderOption = 0;

    private AdjacencyList2DGraphRenderer renderer;
    private List<ISolution> solutions;

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
        RenderOption = algdm.RenderOption.Value;
        // Set render options.
        // TODO: Find a cleaner way to do this.
        for (ISolution i : solutions) {
            AdjacencyList2DGraphRenderer r = (AdjacencyList2DGraphRenderer) i.getRenderer();
            AdjacencyList2DGraphRenderer sr = (AdjacencyList2DGraphRenderer) i.getSolutionRenderer();
            switch (RenderOption) {
                case 0:
                    r.setRenderOptions(true, false, true);
                    sr.setRenderOptions(true, true, true);
                    break;

                case 1:
                    r.setRenderOptions(false, false, true);
                    sr.setRenderOptions(true, true, true);
                    break;

                case 2:
                    r.setRenderOptions(false, false, true);
                    sr.setRenderOptions(false, false, true);
                    break;
            }
        }
    }

    @Override
    public Object getDefaultDataModel() {
        AdjacencyListGraphDataModel algdm = new AdjacencyListGraphDataModel();
        algdm.Edges = Edges;
        algdm.Nodes = Nodes;
        algdm.RenderOption.Value = RenderOption;
        return algdm;
    }

    @Override
    public IGenerator getGenerator(IProgressListener listener) {
        return new AdjacencyListGenerator2D(Nodes, Edges, renderer, listener);
    }

    @Override
    public List<ISolution> getSolutions(IProgressListener listener) {
        if (solutions == null) {
            solutions = new ArrayList<ISolution>();
            solutions.add(new TarjansAlgorithm());
        }
        return solutions;
    }
}
