package com.vizalgo.rendering;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.TextureView;

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
    private int EdgeThickness = 3;
    private int PointRadius = 3;
    private int ArrowAngle = 25;
    private int ArrowLength = 20;

    private Paint paint;
    private AdjacencyListGraph<AdjacencyListNode2D> graph;
    private IRenderer baseRenderer;

    private boolean drawOnNewNode = true;
    private boolean drawOnNewEdge = true;
    private boolean drawOnComplete = true;

    private TextureView textureView;

    public AdjacencyList2DGraphRenderer(IRenderer baseRenderer) {
        paint = new Paint();
        this.baseRenderer = baseRenderer;
    }

    @Override
    public void setTextureView(TextureView t) {
        textureView = t;
    }

    @Override
    public void render(Canvas canvas, Rect rect) {
        for (Iterator it = graph.getAllNodes().entrySet().iterator();
             it.hasNext(); ) {
            AdjacencyListNode2D node = (AdjacencyListNode2D)((HashMap.Entry)it.next()).getValue();
            for (Iterator it2 = node.getEdges().iterator();
                 it2.hasNext(); ) {
                drawEdge(canvas, (AdjacencyListEdge2D) it2.next(), rect);
            }
        }
        for (Iterator it = graph.getAllNodes().entrySet().iterator();
             it.hasNext(); ) {
            drawNode(canvas, (AdjacencyListNode2D) ((HashMap.Entry) it.next()).getValue(), rect);
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
            drawGraph(rectFromEdge(e));
        }
    }

    @Override
    public void onNodeAdded(IAdjacencyListGraphNodeType n) {
        if (drawOnNewNode) {
            drawGraph(new Rect(n.getX() - 10, n.getY() - 10, n.getX() + 10, n.getY() + 10));
        }
    }

    @Override
    public void onNodeRemoved(IAdjacencyListGraphNodeType n) {
        if (drawOnNewNode) {
            drawGraph(null);
        }
    }

    @Override
    public void onComplete() {
        if (drawOnComplete) {
            drawGraph(null);
        }
    }

    private void drawGraph(Rect dirty) {
        try {
            Canvas canvas = textureView.lockCanvas(dirty);
            // Render base graph or clear the canvas prior to each draw iteration
            if (baseRenderer != null) {
                baseRenderer.render(canvas, dirty);
            }
            else {
                canvas.drawColor(Color.rgb(0, 0, 0));
            }
            render(canvas, dirty);
            textureView.unlockCanvasAndPost(canvas);
        }
        catch (Exception ex) {
            System.out.println("Got exception in drawGraph(): " + ex);
        }
    }

    private void drawNode(Canvas canvas, AdjacencyListNode2D node, Rect dirtyRect) {
        if (dirtyRect == null || dirtyRect.contains(node.getX(), node.getY())) {
            paint.setColor(node.getColor());
            canvas.drawCircle(node.getX(), node.getY(), PointRadius, paint);
        }
    }

    private void drawEdge(Canvas canvas, AdjacencyListEdge2D edge, Rect dirtyRect) {
        Rect edgeRect = rectFromEdge(edge);
        if (dirtyRect == null || Rect.intersects(dirtyRect, edgeRect)
                || dirtyRect.contains(edgeRect) || edgeRect.contains(dirtyRect)) {
            paint.setColor(edge.getColor());
            paint.setStrokeWidth(EdgeThickness);
            AdjacencyListNode2D start = ((AdjacencyListNode2D) edge.getStartNode());
            AdjacencyListNode2D end = ((AdjacencyListNode2D) edge.getEndNode());

            // Arrows
            if (edge.isDirected()) {
                paint.setStrokeWidth(2);
                double angle = Math.atan2(start.getY() - end.getY(), start.getX() - end.getX());
                double arrow1_angle = angle - Math.toRadians(ArrowAngle);
                double arrow2_angle = angle + Math.toRadians(ArrowAngle);
                canvas.drawLine((float) (end.getX() + Math.cos(arrow1_angle) * ArrowLength),
                        (float) (end.getY() + Math.sin(arrow1_angle) * ArrowLength),
                        end.getX(), end.getY(), paint);
                canvas.drawLine((float) (end.getX() + Math.cos(arrow2_angle) * ArrowLength),
                        (float) (end.getY() + Math.sin(arrow2_angle) * ArrowLength),
                        end.getX(), end.getY(), paint);
            }

            canvas.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), paint);
        }
    }

    private Rect rectFromEdge(IAdjacencyListGraphEdgeType e) {
        int left, right, top, bottom;
        if (e.getStartNode().getX() < e.getEndNode().getX()) {
            left = e.getStartNode().getX();
            right = e.getEndNode().getX();
        } else {
            left = e.getEndNode().getX();
            right = e.getStartNode().getX();
        }
        if (e.getStartNode().getY() < e.getEndNode().getY()) {
            top = e.getStartNode().getY();
            bottom = e.getEndNode().getY();
        } else {
            top = e.getEndNode().getY();
            bottom = e.getStartNode().getY();
        }
        return new Rect(left, top, right, bottom);
    }

}
