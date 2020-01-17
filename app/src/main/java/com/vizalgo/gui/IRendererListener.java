package com.vizalgo.gui;

/**
 * Created by garrethamann on 12/25/15.
 */
public interface IRendererListener {
    void onProgress(int progress);
    void onGenerateStart();
    void onRenderStart();

    void onRenderError(String error);
    void onRenderDone();
}
