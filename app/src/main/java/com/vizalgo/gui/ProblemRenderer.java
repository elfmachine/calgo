package com.vizalgo.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
public class ProblemRenderer extends SurfaceView implements SurfaceHolder.Callback,
        Runnable, IProgressListener {
    private SurfaceHolder holder;

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
        holder = getHolder();
        holder.addCallback(this);
        this.problem = problem;
        this.solution = solution;
        this.rendererListener = listener;
        this.altView = altView;
    }

    public void updateProblem(IProblem problem) {
        this.problem = problem;
        initViews();
    }

    private void initViews() {
        setVisibility(VISIBLE);
        altView.setVisibility(VISIBLE);
        setZOrderOnTop(true);
        // TODO: Uhhh.. fix this.
        IRenderer tempSolutionRenderer = solution.getRenderer(new Paint());
        if (tempSolutionRenderer.supportsRecyclerView()) {
            altView.setAdapter(new TextRecyclerViewAdapter(new ArrayList<String>()));
            holder.setFormat(PixelFormat.TRANSPARENT);
            setBackgroundColor(Color.TRANSPARENT);
        } else {
            // Clear canvas
            Canvas canvas = holder.lockCanvas(null);
            if (canvas != null) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                canvas.drawColor(Color.argb(0, 0, 0, 0));
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void updateGeneratorMethod(int method) {
        generatorMethod = method;
    }

    public void updateSolution(ISolution solution) {
        this.solution = solution;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
    }

    private class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public TextViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
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
                canvas = holder.lockCanvas(null);
                // TODO: Fix this crap
                generator.setCoordinates(0, 0, canvas.getWidth(), canvas.getHeight());
                holder.unlockCanvasAndPost(canvas);
                canvas = null;
                renderer.setHolder(holder);
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
                solutionRenderer.setHolder(holder);
            }
            rendererListener.onProgress(0);
            rendererListener.onRenderStart();
            time = System.currentTimeMillis();
            final Object result = solution.solve(problemRepresentation);
            System.out.println(String.format("Finished solve in %dms", System.currentTimeMillis() - time));

            if (renderer.supportsRecyclerView()) {
                post(new Runnable() {
                    public void run() {
                        altView.setAdapter(new TextRecyclerViewAdapter((List<String>) result));
                    }
                });
            } else if (renderer.supportsCanvas()) {
                canvas = holder.lockCanvas(null);
                renderResult(canvas, result, renderer, solutionRenderer);
                holder.unlockCanvasAndPost(canvas);
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
                holder.unlockCanvasAndPost(canvas);
            }
            rendererListener.onRenderDone();
        }
    }

    @Override
    // This is always called at least once, after surfaceCreated
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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
