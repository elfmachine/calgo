package com.vizalgo.rendering;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import com.vizalgo.primitives.AdjacencyListEdge2D;
import com.vizalgo.primitives.AdjacencyListGraph;
import com.vizalgo.primitives.AdjacencyListNode2D;
import com.vizalgo.primitives.IAdjacencyListChangeListener;
import com.vizalgo.primitives.IAdjacencyListGraphEdgeType;
import com.vizalgo.primitives.IAdjacencyListGraphNodeType;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by garret on 12/11/15.
 */
public class AdjacencyList2DGraphRenderer implements
        IAdjacencyListChangeListener<AdjacencyListNode2D>, IRenderer {
    public int EdgeThickness = 3;
    public int PointRadius = 3;
    public int ArrowAngle = 25;
    public int ArrowLength = 20;

    private Paint paint;
    private AdjacencyListGraph<AdjacencyListNode2D> graph;
    private IRenderer baseRenderer;

    private boolean drawOnNewNode = true;
    private boolean drawOnNewEdge = true;
    private boolean drawOnComplete = true;

    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    public AdjacencyList2DGraphRenderer(Paint p, IRenderer baseRenderer) {
        paint = p;
        this.baseRenderer = baseRenderer;
    }

    @Override
    public void setCanvas(Canvas c) {
        canvas = c;
    }


    @Override
    public void setHolder(SurfaceHolder s) {
        surfaceHolder = s;
    }

    @Override
    public void render() {
        //System.out.println("Rendering a graph with " + graph.getAllNodes().size() + "nodes");
        for (Iterator it = graph.getAllNodes().entrySet().iterator();
             it.hasNext(); ) {
            AdjacencyListNode2D node = (AdjacencyListNode2D)((HashMap.Entry)it.next()).getValue();
            for (Iterator it2 = node.getEdges().iterator();
                 it2.hasNext(); ) {
                drawEdge(canvas, (AdjacencyListEdge2D)it2.next());
            }
        }
        for (Iterator it = graph.getAllNodes().entrySet().iterator();
             it.hasNext(); ) {
            drawNode(canvas, (AdjacencyListNode2D)((HashMap.Entry)it.next()).getValue());
        }
    }

    public void setRenderOptions(boolean drawOnNewEdge, boolean drawOnNewNode, boolean drawOnComplete) {
        this.drawOnNewEdge = drawOnNewEdge;
        this.drawOnNewNode = drawOnNewNode;
        this.drawOnComplete = drawOnComplete;
    }

    @Override
    public void onGraphCreated(AdjacencyListGraph<AdjacencyListNode2D> graph) {
        this.graph = graph;
    }

    @Override
    public IRenderer getBaseRenderer() {
        return baseRenderer;
    }

    @Override
    public boolean supportsCanvas() {
        return true;
    }

    @Override
    public boolean supportsRecyclerView() {
        return false;
    }

    @Override
    public void onEdgeAdded(IAdjacencyListGraphEdgeType e) {
        if (drawOnNewEdge) {
            drawGraph();
        }
    }

    @Override
    public void onNodeAdded(IAdjacencyListGraphNodeType n) {
        if (drawOnNewNode) {
            drawGraph();
        }
    }

    @Override
    public void onNodeRemoved(IAdjacencyListGraphNodeType n) {
        if (drawOnNewNode) {
            drawGraph();
        }
    }

    @Override
    public void onComplete() {
        if (drawOnComplete) {
            drawGraph();
        }
    }

    private void drawGraph() {
        try {
            // Render base graph or clear the canvas prior to each draw iteration
            if (baseRenderer != null) {
                baseRenderer.setCanvas(canvas);
                baseRenderer.render();
            }
            else {
                canvas.drawColor(Color.rgb(0, 0, 0));
            }
            render();
            surfaceHolder.unlockCanvasAndPost(canvas);
            canvas = surfaceHolder.lockCanvas();
        }
        catch (Exception ex) {
            System.out.println("Got exception in drawGraph(): " + ex);
        }
        finally {
        }
    }

    private void drawNode(Canvas canvas, AdjacencyListNode2D node) {
        paint.setColor(node.getColor());
        canvas.drawCircle(node.getX(), node.getY(), PointRadius, paint);
    }

    private void drawEdge(Canvas canvas, AdjacencyListEdge2D edge) {
        paint.setColor(edge.getColor());
        paint.setStrokeWidth(EdgeThickness);
        AdjacencyListNode2D start = ((AdjacencyListNode2D)edge.getStartNode());
        AdjacencyListNode2D end = ((AdjacencyListNode2D)edge.getEndNode());

        // Arrows
        if (edge.isDirected()) {
            paint.setStrokeWidth(2);
            double angle = Math.atan2(start.getY() - end.getY(), start.getX() - end.getX());
            double arrow1_angle = angle - Math.toRadians(ArrowAngle);
            double arrow2_angle = angle + Math.toRadians(ArrowAngle);
            canvas.drawLine((float)(end.getX() + Math.cos(arrow1_angle) * ArrowLength),
                    (float)(end.getY() + Math.sin(arrow1_angle) * ArrowLength),
                    end.getX(), end.getY(), paint);
            canvas.drawLine((float)(end.getX() + Math.cos(arrow2_angle) * ArrowLength),
                    (float)(end.getY() + Math.sin(arrow2_angle) * ArrowLength),
                    end.getX(), end.getY(), paint);
        }

        canvas.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), paint);
    }

}
