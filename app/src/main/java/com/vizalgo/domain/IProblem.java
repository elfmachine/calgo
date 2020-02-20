package com.vizalgo.domain;

import com.vizalgo.primitives.IGenerator;
import com.vizalgo.primitives.IProgressListener;
import com.vizalgo.rendering.IRenderer;

import java.util.List;

/**
 * Created by garret on 12/11/15.
 */
public interface IProblem {
    String getName();

    List<ISolution> getSolutions(IProgressListener listener);
    void setRenderer(IRenderer renderer);
    void setDataModel(Object dataModel);
    Object getDefaultDataModel();
    IGenerator getGenerator(IProgressListener listener);
}
