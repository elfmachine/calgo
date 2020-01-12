package com.vizalgo.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
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
import com.vizalgo.rendering.IRenderer;
import com.vizalgo.rendering.StringListRenderer;

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

    private Context context;

    private IGenerator generator;

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
        // Test
        Canvas canvas;
        IRenderer renderer = solution.getRenderer(new Paint());
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        int renderOption = sharedPref.getInt("renderOption", -1);

        System.out.println("ProblemRenderer: Render option is " + renderOption);
        IRenderer solutionRenderer = solution.getSolutionRenderer(new Paint());
        canvas = holder.lockCanvas(null);
        try
        {
            // Run the problem
            problem.setRenderer(renderer);
            generator = problem.getGenerator(this);
            if (canvas != null) {
                generator.setCoordinates(0, 0, canvas.getWidth(), canvas.getHeight());
            }
            renderer.setCanvas(canvas);
            renderer.setHolder(holder);
            switch(renderOption) {
                case R.id.draw_everything:
                    renderer.setRenderOptions(true, false, true);
                    solutionRenderer.setRenderOptions(true, true, true);
                    break;

                case R.id.only_completion:
                    renderer.setRenderOptions(false, false, true);
                    solutionRenderer.setRenderOptions(false, false, true);
                    break;

                case R.id.only_traversal:
                    renderer.setRenderOptions(false, false, true);
                    solutionRenderer.setRenderOptions(true, true, true);
                    break;
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
            canvas = renderer.getCanvas();
            solutionRenderer.setCanvas(canvas);
            solutionRenderer.setHolder(holder);
            rendererListener.onProgress(0);
            rendererListener.onRenderStart();
            time = System.currentTimeMillis();
            final Object result = solution.solve(problemRepresentation);
            System.out.println(String.format("Finished solve in %dms", System.currentTimeMillis() - time));

            if (renderer instanceof StringListRenderer) {
                // Egregious hack: mudge on a shoestring recycler view to show results
                // TODO: Move this into a subclass that renders directly to the RV.
                post(new Runnable() {
                    public void run() {
                        altView.setAdapter(new TextRecyclerViewAdapter((List<String>) result));
                        altView.requestLayout();
                    }
                });
            } else {
                canvas = solutionRenderer.getCanvas();
                renderResult(canvas, result, renderer, solutionRenderer);
            }

            if (!Thread.currentThread().isInterrupted()) {
                rendererListener.onProgress(100);
            }
        }
        catch (Exception ex) {
            System.out.println("ERROR: Unhandled exception during problem run: " + ex);
            ex.printStackTrace();
        }
        finally {
            //System.out.println("Tearing down surface");
            if (!(renderer instanceof StringListRenderer)) {
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
        renderer.render();
        solutionRenderer.render();
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
