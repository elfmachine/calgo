package com.vizalgo.domain.problems;

import android.content.Context;

import com.vizalgo.domain.IProblem;
import com.vizalgo.domain.ISolution;
import com.vizalgo.domain.solutions.GarretsNaiveSubStringSolution;
import com.vizalgo.domain.solutions.GarretsSmartSubStringSolution;
import com.vizalgo.primitives.IGenerator;
import com.vizalgo.primitives.IProgressListener;
import com.vizalgo.primitives.StringListGenerator;
import com.vizalgo.rendering.IRenderer;
import com.vizalgo.rendering.StringListRenderer;

import java.util.ArrayList;
import java.util.List;

public class StringsAreSubstringsOfEachOther implements IProblem {
    private static final int DEFAULT_NUMBER_OF_STRINGS = 1000;
    private StringListDataModel dataModel;
    private Context context;
    private StringListRenderer renderer;

    public StringsAreSubstringsOfEachOther(Context context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return "Strings that are substrings of each other";
    }

    @Override
    public List<ISolution> getSolutions(IProgressListener listener) {
        ArrayList<ISolution> s = new ArrayList<ISolution>();
        s.add(new GarretsNaiveSubStringSolution());
        s.add(new GarretsSmartSubStringSolution());
        return s;
    }

    @Override
    public void setRenderer(IRenderer renderer) {
        this.renderer = (StringListRenderer)renderer;
    }

    @Override
    public void setDataModel(Object dataModel) {
        this.dataModel = (StringListDataModel)dataModel;
    }

    @Override
    public Object getDefaultDataModel() {
        StringListDataModel dataModel = new StringListDataModel();
        dataModel.NumberOfStrings = DEFAULT_NUMBER_OF_STRINGS;
        return dataModel;
    }

    @Override
    public IGenerator getGenerator(IProgressListener listener) {
        return new StringListGenerator(context, dataModel, renderer);
    }
}
