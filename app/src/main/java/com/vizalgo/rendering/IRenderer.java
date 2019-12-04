package com.vizalgo.rendering;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by garret on 12/11/15.
 */
public interface IRenderer {
    void render();
    void setCanvas(Canvas c);
    Canvas getCanvas();
    void setHolder(SurfaceHolder s);
    void setRenderOptions(boolean drawOnNewEdge, boolean drawOnNewNode, boolean drawOnComplete);
    IRenderer getBaseRenderer();
}
