package com.vizalgo.primitives;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by garret on 12/11/15.
 */
public class AdjacencyListNode2D implements IAdjacencyListGraphNodeType {
    public ArrayList<IAdjacencyListGraphEdgeType> edges;

    private final int DEFAULT_COLOR = 0xFFa00040;

    private int value;
    private int index;

    private int x, y;

    protected int color;

    public AdjacencyListNode2D(int x, int y)
    {
        this(x,y,0);
    }

    public AdjacencyListNode2D(int x, int y, int value) {
        this(x,y,value,0);
        color = DEFAULT_COLOR;
    }

    public AdjacencyListNode2D(int x, int y, int value, int color) {
        //System.out.println("Creating new node at (" + x + ", " + y + "), " + value + ", " + color);
        this.x = x;
        this.y = y;
        this.value = value;
        this.color = color;
        index = Constants.UNDEFINED;
        edges = new ArrayList<IAdjacencyListGraphEdgeType>();
    }

    public AdjacencyListNode2D(AdjacencyListNode2D copyNode) {
        this(copyNode.getX(), copyNode.getY(), copyNode.getValue(), copyNode.getColor() );
        index = copyNode.getIndex();
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getDescription() {
        return "Node #" + index + "@(" + x + "," + y + "),v=" + value + ",c=" + color +
                ",#e=" + edges.size();
    }

    @Override
    public void setIndex(int i) {
        index = i;
    }

    @Override
    public void setValue(int v) { value = v; }

    @Override
    public double distanceTo(IAdjacencyListGraphNodeType otherNode) {
        // TODO: Throw different type of exception if wrong type of node is passed in
        AdjacencyListNode2D aListOtherNode = (AdjacencyListNode2D)otherNode;
        double xDistance = x-aListOtherNode.getX();
        double yDistance = y-aListOtherNode.getY();
        return Math.sqrt(xDistance*xDistance+yDistance*yDistance);
    }

    @Override
    public boolean isConnectedTo(IAdjacencyListGraphNodeType otherNode) {
        for (Iterator it = edges.iterator();
                it.hasNext();) {
            if (((AdjacencyListEdge2D)it.next()).getEndNode().getIndex() == otherNode.getIndex()) {
                return true;
            }
        }
        return false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public IAdjacencyListGraphEdgeType connect(IAdjacencyListGraphNodeType newNode, int color,
                                               int weight, boolean isDirected) {
        AdjacencyListEdge2D e = new AdjacencyListEdge2D(color, weight, this,
                    (AdjacencyListNode2D)newNode, isDirected);
        doConnect(e, newNode, isDirected);
        return e;
    }

    public IAdjacencyListGraphEdgeType connect(IAdjacencyListGraphNodeType newNode, int weight,
                                               boolean isDirected) {
        AdjacencyListEdge2D e = new AdjacencyListEdge2D(weight, this, (AdjacencyListNode2D)newNode,
                                        isDirected);
        doConnect(e, newNode, isDirected);
        return e;
    }

    private void doConnect(AdjacencyListEdge2D e, IAdjacencyListGraphNodeType newNode, boolean isDirected) {
        edges.add(e);

        // TODO: Look into having "connected to" list of edges for each node.  This might make tree
        // traversal easier.  Directed-ness would just then be a flag.
        if (!isDirected) {
            newNode.getEdges().add(e);
        }
    }

    public ArrayList<IAdjacencyListGraphEdgeType> getEdges() {
        return edges;
    }
}
