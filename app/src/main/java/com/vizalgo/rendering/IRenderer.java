package com.vizalgo.rendering;

import android.graphics.Canvas;
import android.view.TextureView;

/**
 * Created by garret on 12/11/15.
 */
public interface IRenderer {
    void render(Canvas c);

    //void setCanvas(Canvas c);
    void setTextureView(TextureView textureView);
    IRenderer getBaseRenderer();
    boolean supportsCanvas();
    boolean supportsRecyclerView();
}
