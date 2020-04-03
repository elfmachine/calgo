package com.vizalgo.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.garretware.graphtestapp.R;
import com.vizalgo.domain.IProblem;
import com.vizalgo.domain.ISolution;
import com.vizalgo.domain.ProblemFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class VizAlgoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final static int SHOW_MESSAGE = 1;

    private ArrayList<IProblem> problems = ProblemFactory.getProblemSet(this);
    private List<ISolution> solutions;

    private IProblem currentProblem;
    private ISolution currentSolution;

    private ProblemRenderer problemRenderer;
    private RecyclerView recyclerView;

    private Thread runThread;
    private Handler messageHandler;

    private ProgressBar progressBar;

    private int renderOption;
    private boolean cancelled;

    private Object dataModel;

    private boolean renderError;

    private SharedPreferences sharedPref;

    public VizAlgoActivity() {
        messageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case SHOW_MESSAGE:
                        doSetStatusText((String)inputMessage.obj, inputMessage.arg1);
                        break;
                    default:
                        super.handleMessage(inputMessage);
                        break;
                }
            }
        };
    }

    public void startSolver(View view) {
        renderError = false;

        LinearLayout layout = (LinearLayout) findViewById(R.id.graphOptions);
        int index = 1;
        for (Field f : dataModel.getClass().getFields()) {
            // Find the corresponding EditText.  Note that this assumes the field order is
            // preserved.
            // TODO: Create masked sequential id based on ordering of field in class.
            // TODO: Move to separate class
            EditText editText = (EditText) layout.getChildAt(index);
            index += 2;
            String textValue = editText.getText().toString();

            try {
                if (f.getType() == int.class || f.getType() == Integer.class) {
                    f.set(dataModel, Integer.valueOf(textValue));
                } else if (f.getType() == String.class) {
                    f.set(dataModel, textValue);
                } else {
                    System.out.println(
                            String.format(
                                    "WARNING: Don't know what to do with field of type %s for %s",
                                    f.getType(), f.getName()));
                }
            } catch (IllegalAccessException e) {
                System.out.println(
                        String.format("IllegalAccessException reading view for: %s\n%s"
                                + f.getName(), e));
            }
        }

        currentProblem.setDataModel(dataModel);

        if (runThread == null) {
            runThread = new Thread(problemRenderer);
            runThread.start();
            cancelled = false;
        }
    }

    public void cancelSolve(View view) {
        if (runThread != null) {
            setStatusText("Cancelled", 0xffff8000);
            cancelled = true;
            handleStop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);

        renderOption = sharedPref.getInt("renderOption", -1);
        if (renderOption != -1 && menu.findItem(renderOption) != null) {
            menu.findItem(renderOption).setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.isCheckable()) {
            item.setChecked(true);
            setPrefInt("renderOption", id);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_viz_algo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<String> problemNames = new LinkedList<>();
        for (Iterator it = problems.iterator();
             it.hasNext(); ) {
            problemNames.add(((Iterator<IProblem>) it).next().getName());
        }

        setupSpinner("problem", problemNames, R.id.problem_spinner);
        initProblem();

        // Set up render view.
        LinearLayout rootLayout = (LinearLayout)findViewById(R.id.vizalgo_root_view);
        FrameLayout renderLayout = new FrameLayout(this);
        renderLayout.setLayoutParams(new FrameLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT));
        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        problemRenderer = new ProblemRenderer(this, currentProblem, currentSolution, new RenderListener(), recyclerView);
        renderLayout.addView(problemRenderer);
        renderLayout.addView(recyclerView);
        rootLayout.addView(renderLayout);
        currentSolution.setProgressListener(problemRenderer);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setMax(100);
    }

    @Override
    public void onBackPressed() {
        System.out.println("onBackPressed");
        handleStop();
        super.onBackPressed();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        System.out.println("onItemSelected " + pos + "," + id + "on adapter " + parent.getId());
        if (parent.getId() == R.id.problem_spinner) {
            setPrefInt("problem", pos);
            initProblem();
            problemRenderer.updateProblem(currentProblem);
            problemRenderer.updateSolution(currentSolution);
            problemRenderer.updateGeneratorMethod(
                    sharedPref.getInt(getSpinnerName("generator"), 0));
            problemRenderer.initViews();
        }
        if (parent.getId() == R.id.generator_spinner) {
            setPrefInt(getSpinnerName("generator"), pos);
            problemRenderer.updateGeneratorMethod(pos);
        }
        if (parent.getId() == R.id.solution_spinner) {
            setPrefInt(getSpinnerName("solution"), pos);
            currentSolution = solutions.get(pos);
            currentSolution.setProgressListener(problemRenderer);
            problemRenderer.updateSolution(solutions.get(pos));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private class RenderListener implements IRendererListener {
        @Override
        public void onProgress(int progress) {
            progressBar.setProgress(progress);
        }

        @Override
        public void onGenerateStart() {
            progressBar.setProgress(0);
            setStatusText("Generating..", 0xff00c040);
        }

        @Override
        public void onRenderStart() {
            progressBar.setProgress(0);
            setStatusText("Solving..", 0xff0000c0);
        }

        @Override
        public void onRenderError(String error) {
            renderError = true;
        }

        @Override
        public void onRenderDone() {
            runThread = null;
            if (renderError) {
                setStatusText("Error!", 0xff800080);
            }
            if (!cancelled) {
                setStatusText("Done!", 0xff00a080);
            }
        }
    }

    private void initProblem() {
        currentProblem = problems.get(sharedPref.getInt("problem", 0));
        dataModel = currentProblem.getDefaultDataModel();

        LinearLayout layout = (LinearLayout) findViewById(R.id.graphOptions);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.removeAllViews();
        for (Field f : dataModel.getClass().getFields()) {
            // Add TextView which describes the object.
            TextView textView = new TextView(this);
            textView.setText(f.getName());
            layout.addView(textView);

            // Add EditText to edit the object.
            EditText editText = new EditText(this);
            try {
                editText.setText(f.get(dataModel).toString());
            } catch (IllegalAccessException e) {
                System.out.println(
                        String.format("IllegalAccessException constructing view for: %s\n%s"
                                + f.getName(), e));
            }
            layout.addView(editText);
        }

        solutions = currentProblem.getSolutions(problemRenderer);
        List<String> solutionNames = new LinkedList<>();
        for (Iterator it = solutions.iterator();
             it.hasNext(); ) {
            solutionNames.add(((Iterator<ISolution>) it).next().getName());
        }

        currentSolution = solutions.get(sharedPref.getInt(getSpinnerName("solution"), 0));
        setupSpinner(getSpinnerName("solution"), solutionNames, R.id.solution_spinner);

        // TODO: This is ugly.  Have a class with generator meta info.
        Dictionary<Integer, String> generators = currentProblem.getGenerator(null).getMethods();
        LinkedList<String> names = new LinkedList<>();
        for (int i = 0; i < generators.size(); i++) {
            names.add(generators.get(i));
        }
        setupSpinner(getSpinnerName("generator"), names, R.id.generator_spinner);
    }

    private void setupSpinner(String pref, List<String> strings, int id) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                strings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(id);
        spinner.setAdapter(adapter);
        int value = sharedPref.getInt(pref, -1);
        System.out.println(String.format("Spinner for %s is %d", pref, value));
        if (value != -1) {
            spinner.setSelection(value);
        }
        spinner.setOnItemSelectedListener(this);
    }

    private void handleStop() {
        if (runThread != null && runThread.isAlive()) {
            runThread.interrupt();
            runThread = null;
        }
    }

    private String getSpinnerName(String id) {
        return "problem_" + currentProblem.getClass().getName() + "_spinner_" + id;
    }

    private void setStatusText(String text, int color) {
        Message m = messageHandler.obtainMessage(SHOW_MESSAGE, color, 0, text);
        m.sendToTarget();
    }

    private void doSetStatusText(String text, int color) {
        TextView tv = (TextView)findViewById(R.id.status_text);
        tv.setText(text);
        tv.setTextColor(color);
    }

    private void setPrefInt(String option, int value) {
        SharedPreferences.Editor e = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
        e.putInt(option, value);
        e.apply();
    }
}
