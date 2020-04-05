package com.vizalgo.domain;

import com.vizalgo.primitives.IProgressListener;
import com.vizalgo.rendering.IRenderer;

/**
 * Created by garret on 12/11/15.
 */
public interface ISolution {
    String getName();
    Object solve(Object problem);

    IRenderer getRenderer();

    IRenderer getSolutionRenderer();
    void setProgressListener(IProgressListener listener);
}
