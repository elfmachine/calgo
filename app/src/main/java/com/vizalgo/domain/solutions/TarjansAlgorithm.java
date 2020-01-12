package com.vizalgo.domain.solutions;

import android.graphics.Paint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import com.vizalgo.domain.*;
import com.vizalgo.primitives.*;
import com.vizalgo.rendering.AdjacencyList2DGraphRenderer;
import com.vizalgo.rendering.IRenderer;

/**
 * Created by garret on 12/11/15.
 */
public class TarjansAlgorithm implements ISolution {
    public String getName() {
        return "Trajan's Algorithm";
    }

    public static int TraversalLineColor = 0xFF8080F0;
    public static int TraversalNodeColor = 0xFFFFFFFF;
    public static int FinalLineColor = 0xFF80C080;
    public static int FinalNodeColor = 0xFF00C000;

    public boolean ShowTraversal = true;

    private TarjansAdjacencyListGraph2D traversalGraph;
    private TarjansAdjacencyListGraph2D solutionGraph;

    private AdjacencyList2DGraphRenderer renderer;
    private AdjacencyList2DGraphRenderer solutionRenderer;

    private int currentIndex = 0;

    private Stack<TarjansAdjacencyListNode2D> nodeStack = new Stack<>();

    private IProgressListener listener;

    public TarjansAlgorithm() {
        this.listener = listener;
    }

    private int runningProgressCount;

    private int progressMax;

    private int cycles;

    public Object solve(Object problemRepresentation) {
        traversalGraph = new TarjansAdjacencyListGraph2D(
                (AdjacencyListGraph<AdjacencyListNode2D>)problemRepresentation, null);
        solutionGraph = new TarjansAdjacencyListGraph2D(solutionRenderer);
        solutionRenderer.onGraphCreated(solutionGraph);

        cycles = 0;
        runningProgressCount = 0;
        progressMax = 0;

        for (Iterator it = traversalGraph.getAllNodes().entrySet().iterator(); it.hasNext();) {
            TarjansAdjacencyListNode2D node = (TarjansAdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue();
            progressMax += node.getEdges().size();
        }
        int edgesCount = progressMax;

        for (Iterator it = traversalGraph.getAllNodes().entrySet().iterator(); it.hasNext();) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            TarjansAdjacencyListNode2D node = (TarjansAdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue();
            if (node.Index == Constants.UNDEFINED) {
                TarjansAdjacencyListNode2D traversalNode = null;
                if (ShowTraversal) {
                    traversalNode = new TarjansAdjacencyListNode2D(node, false);
                    solutionGraph.addNode(traversalNode);
                }
                strongConnect(node, traversalNode);
            }
        }
        solutionRenderer.onComplete();
        return "Nodes: " + traversalGraph.getAllNodes().size()
                + " Edges: " + edgesCount + " Cycles: " + cycles;
    }

    private void strongConnect(TarjansAdjacencyListNode2D node, TarjansAdjacencyListNode2D traversalNode) {
        // TODO: Verify this.  Currently, most large graphs seem to only have one cycle.

        System.out.println("StrongConnect " + node.getDescription() + " with traversal node "
                            + traversalNode.getDescription());
        node.Index = currentIndex;
        node.LowLink = currentIndex++;
        nodeStack.push(node);
        node.OnStack = true;
        for (int i=0; i<node.getEdges().size(); i++) {
            listener.onProgress(runningProgressCount * 100 / progressMax);
            if (runningProgressCount < progressMax) {
                runningProgressCount++;
            }
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            AdjacencyListEdge2D edge = (AdjacencyListEdge2D)node.getEdges().get(i);
            TarjansAdjacencyListNode2D endNode = (TarjansAdjacencyListNode2D)edge.getEndNode();
            if (endNode.Index == Constants.UNDEFINED) {
                TarjansAdjacencyListNode2D traversalEndNode = null;
                if (ShowTraversal) {
                    traversalEndNode = new TarjansAdjacencyListNode2D(endNode, false);
                    solutionGraph.addNode(traversalEndNode);
                    System.out.println("Connecting " + traversalNode.getDescription() + " to node "
                            + traversalEndNode.getDescription());
                    solutionGraph.connectNode(traversalNode.getIndex(), traversalEndNode.getIndex(),
                            TraversalLineColor, 0);
                }
                strongConnect(endNode, traversalEndNode);
                node.LowLink = Math.min(node.LowLink, endNode.LowLink);
            }
            else if (endNode.OnStack) {
                node.LowLink = Math.min(node.LowLink, endNode.Index);
            }
        }

        if (ShowTraversal) {
            System.out.println("Removing " + traversalNode.getDescription());
            solutionGraph.removeNode(traversalNode);
        }

        if (node.LowLink == node.Index) {
            if (node.getIndex() != nodeStack.peek().getIndex()) {
                System.out.println("Found cycle");
                cycles++;
                TarjansAdjacencyListNode2D rootNode = new TarjansAdjacencyListNode2D(node, true);
                solutionGraph.addNode(rootNode);
                TarjansAdjacencyListNode2D lastNode = rootNode;
                for (TarjansAdjacencyListNode2D nextNode = nodeStack.pop();
                     nextNode.getIndex() != rootNode.getIndex();
                     nextNode = nodeStack.pop()) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    nextNode.OnStack = false;
                    nextNode = new TarjansAdjacencyListNode2D(nextNode, true);
                    solutionGraph.addNode(nextNode);
                    solutionGraph.connectNode(nextNode.getIndex(), lastNode.getIndex(), FinalLineColor, 0);
                    lastNode = nextNode;
                }

                solutionGraph.connectNode(lastNode.getIndex(), rootNode.getIndex(), FinalLineColor, 0);
                lastNode.connect(rootNode, FinalLineColor, 0, true);

                // Uhh fifty shades of green
                // TODO: Fix magic numbers
                FinalLineColor = 0xFF808080 + (int)(Math.random() * 0x40) + ((int)(Math.random() * 0x80) << 8);
            }
            else {
                System.out.println("Found single node");
                nodeStack.pop();
            }
        }
    }

    public void cancel() {

    }

    public IRenderer getRenderer(Paint p) {
        renderer = new AdjacencyList2DGraphRenderer(p, null);
        return renderer;
    }

    public IRenderer getSolutionRenderer(Paint p) {
        solutionRenderer = new AdjacencyList2DGraphRenderer(p, renderer);
        return solutionRenderer;
    }

    public void setProgressListener(IProgressListener listener) {
        this.listener = listener;
    }
}
