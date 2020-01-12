package com.vizalgo.domain;

import java.util.ArrayList;

import com.vizalgo.primitives.*;
import com.vizalgo.rendering.IRenderer;

/**
 * Created by garret on 12/11/15.
 */
public interface IProblem {
    String getName();
    // TODO: Uhh.. make list, not ArrayList?
    ArrayList<ISolution> getSolutions(IProgressListener listener);
    void setRenderer(IRenderer renderer);
    void setDataModel(Object dataModel);
    Object getDefaultDataModel();
    IGenerator getGenerator(IProgressListener listener);
}
