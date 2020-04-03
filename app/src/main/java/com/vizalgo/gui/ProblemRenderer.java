package com.vizalgo.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.garretware.graphtestapp.R;
import com.vizalgo.domain.IProblem;
import com.vizalgo.domain.ISolution;
import com.vizalgo.primitives.IGenerator;
import com.vizalgo.primitives.IProgressListener;
import com.vizalgo.rendering.AdjacencyList2DGraphRenderer;
import com.vizalgo.rendering.IRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by garret on 12/11/15.
 */
public class ProblemRenderer extends TextureView implements
        Runnable, IProgressListener {

    private int generatorMethod;

    private IProblem problem;
    private ISolution solution;
    private IRendererListener rendererListener;
    private IGenerator generator;

    private Context context;

    private RecyclerView altView;

    public ProblemRenderer(Context context, IProblem problem, ISolution solution,
                           IRendererListener listener, RecyclerView altView) {
        super(context);
        this.context = context;
        this.problem = problem;
        this.solution = solution;
        this.rendererListener = listener;
        this.altView = altView;
    }

    public void updateProblem(IProblem problem) {
        this.problem = problem;
    }

    public void updateGeneratorMethod(int method) {
        generatorMethod = method;
    }

    public void updateSolution(ISolution solution) {
        this.solution = solution;
    }

    private class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public TextViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }
    }

    public void initViews() {
        setVisibility(VISIBLE);
        // TODO: Uhhh.. fix this.
        IRenderer tempSolutionRenderer = solution.getRenderer(new Paint());
        if (tempSolutionRenderer.supportsRecyclerView()) {
            altView.setAdapter(new TextRecyclerViewAdapter(new ArrayList<String>()));
            setOpaque(false);
            setAlpha(0.5f);
            altView.setVisibility(VISIBLE);
        } else {
            setOpaque(true);
            setAlpha(1.0f);
            altView.setVisibility(INVISIBLE);
        }
        // Clear canvas
        Canvas canvas = lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.rgb(0, 0, 0));
            unlockCanvasAndPost(canvas);
        }
    }

    private class TextRecyclerViewAdapter extends RecyclerView.Adapter<TextViewHolder> {
        List<String> contents;

        public TextRecyclerViewAdapter(List<String> contents) {
            this.contents = contents;
        }

        @Override
        public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new TextViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TextViewHolder holder, int position) {
            holder.mTextView.setText(contents.get(position));
        }

        @Override
        public int getItemCount() {
            return contents.size();
        }
    }

    @Override
    public void run() {
        System.out.println("Start render run");
        final IRenderer renderer = solution.getRenderer(new Paint());
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        int renderOption = sharedPref.getInt("renderOption", -1);
        IRenderer solutionRenderer = solution.getSolutionRenderer(new Paint());
        Canvas canvas = null;
        try
        {
            // Run the problem
            problem.setRenderer(renderer);
            generator = problem.getGenerator(this);
            if (renderer.supportsCanvas()) {
                canvas = lockCanvas();
                // TODO: Get this from view itself, not canvas.
                canvas.drawColor(Color.rgb(0, 0, 0));
                generator.setCoordinates(0, 0, canvas.getWidth(), canvas.getHeight());
                unlockCanvasAndPost(canvas);
                canvas = null;
                renderer.setTextureView(this);
            }
            // TODO: Replace with DataModel
            if (renderer instanceof AdjacencyList2DGraphRenderer) {
                AdjacencyList2DGraphRenderer adjacencyList2DGraphRenderer = (AdjacencyList2DGraphRenderer) renderer;
                AdjacencyList2DGraphRenderer adjacencyList2DGraphSolutionRenderer = null;
                if (solutionRenderer instanceof AdjacencyList2DGraphRenderer) {
                    adjacencyList2DGraphSolutionRenderer = (AdjacencyList2DGraphRenderer) solutionRenderer;
                }
                switch (renderOption) {
                    case R.id.draw_everything:
                        adjacencyList2DGraphRenderer.setRenderOptions(true, false, true);
                        if (adjacencyList2DGraphSolutionRenderer != null) {
                            adjacencyList2DGraphSolutionRenderer.setRenderOptions(true, true, true);
                        }
                        break;

                    case R.id.only_completion:
                        adjacencyList2DGraphRenderer.setRenderOptions(false, false, true);
                        if (adjacencyList2DGraphSolutionRenderer != null) {
                            adjacencyList2DGraphSolutionRenderer.setRenderOptions(false, false, true);
                        }
                        break;

                    case R.id.only_traversal:
                        adjacencyList2DGraphRenderer.setRenderOptions(false, false, true);
                        if (adjacencyList2DGraphSolutionRenderer != null) {
                            adjacencyList2DGraphSolutionRenderer.setRenderOptions(true, true, true);
                        }
                        break;
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("Interrupt before generate");
                return;
            }

            rendererListener.onProgress(0);
            long time = System.currentTimeMillis();
            rendererListener.onGenerateStart();
            System.out.println(String.format("Finished generate in %dms", System.currentTimeMillis() - time));
            Object problemRepresentation = generator.generate(generatorMethod);
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("Interrupt after generate");
                return;
            }
            if (solutionRenderer.supportsCanvas()) {
                solutionRenderer.setTextureView(this);
            }
            rendererListener.onProgress(0);
            rendererListener.onRenderStart();
            time = System.currentTimeMillis();
            final Object result = solution.solve(problemRepresentation);
            System.out.println(String.format("Finished solve in %dms", System.currentTimeMillis() - time));

            if (solutionRenderer.supportsRecyclerView()) {
                post(new Runnable() {
                    public void run() {
                        altView.setAdapter(new TextRecyclerViewAdapter((List<String>) result));
                    }
                });
            }
            if (renderer.supportsCanvas() || solutionRenderer.supportsCanvas()) {
                canvas = lockCanvas();
                renderResult(canvas, result, renderer, solutionRenderer);
                unlockCanvasAndPost(canvas);
                canvas = null;
            }

            if (!Thread.currentThread().isInterrupted()) {
                rendererListener.onProgress(100);
            }
        }
        catch (Exception ex) {
            rendererListener.onRenderError(ex.getLocalizedMessage());
            System.out.println("ERROR: Unhandled exception during problem run: " + ex);
            ex.printStackTrace();
        }
        finally {
            if (canvas != null) {
                unlockCanvasAndPost(canvas);
            }
            rendererListener.onRenderDone();
        }
    }

    @Override
    public void onProgress(int progress) {
        rendererListener.onProgress(progress);
    }

    private void renderResult(Canvas c, Object result, IRenderer renderer, IRenderer solutionRenderer) {
        // TODO: Find way to avoid rerendering here.
        if (renderer.supportsCanvas()) {
            renderer.render(c);
        }
        if (solutionRenderer.supportsCanvas()) {
            solutionRenderer.render(c);
        }
        if (result instanceof String) {
            String s = (String)result;
            Paint p = new Paint();

            p.setTextSize(50);
            Rect bounds = new Rect();
            p.getTextBounds(s, 0, s.length(), bounds);
            p.setColor(0);
            c.drawText(s, 4, c.getHeight() - bounds.height() - 6, p);
            p.setColor(0xfff0a040);
            c.drawText(s, 6, c.getHeight() - bounds.height() - 4, p);
        }
    }
}
