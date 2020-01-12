package com.vizalgo.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.*;

import com.garretware.graphtestapp.R;
import com.vizalgo.domain.IProblem;
import com.vizalgo.domain.ISolution;
import com.vizalgo.domain.ProblemFactory;
import com.vizalgo.domain.problems.AdjacencyListGraphDataModel;

public class VizAlgoActivity extends AppCompatActivity implements IRendererListener,
        AdapterView.OnItemSelectedListener {
    private ArrayList<IProblem> problems = ProblemFactory.getProblemSet(this);

    private IProblem currentProblem;

    private ISolution currentSolution;

    private ProblemRenderer problemRenderer = null;

    private Thread runThread;

    private ProgressBar progressBar;

    private int renderOption;

    private boolean cancelled;

    private Handler messageHandler;

    private Object dataModel;

    private final static int SHOW_MESSAGE = 1;
    private final static String MESSAGE_TEXT = "message_text";

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
        // TODO: Interpret data model and update accordingly rather than hardcoding to specific
        // data model
        if (dataModel instanceof AdjacencyListGraphDataModel) {
            AdjacencyListGraphDataModel algdm = (AdjacencyListGraphDataModel) dataModel;
            EditText et = (EditText) findViewById(R.id.number_nodes);
            algdm.Nodes = Integer.valueOf(et.getText().toString());
            et = (EditText) findViewById(R.id.number_edges);
            algdm.Edges = Integer.valueOf(et.getText().toString());
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

        // Load render option
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        renderOption = sharedPref.getInt("renderOption", -1);

        System.out.println("Render option is " + renderOption);

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

        System.out.println("onOptionsItemSelected " + id);
        if (item.isCheckable()) {
            item.setChecked(true);
            SharedPreferences.Editor e  = getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
            e.putInt("renderOption", id);
            e.apply();
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
        setContentView(R.layout.activity_viz_algo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupSpinners();

        LinearLayout rootLayout = (LinearLayout)findViewById(R.id.vizalgo_root_view);
        problemRenderer = new ProblemRenderer(this, currentProblem, currentSolution, this);
        rootLayout.addView(problemRenderer);
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
        if (parent.getId() == R.id.generator_spinner) {
            problemRenderer.updateGeneratorMethod(pos);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void setupSpinners() {
        List<String> problemNames = new LinkedList<>();
        for (Iterator it = problems.iterator();
                it.hasNext(); ) {
            problemNames.add(((Iterator<IProblem>)it).next().getName());
        }

        setupSpinner(problemNames, R.id.problem_spinner);

        // TODO: Save and read from storage
        // TODO: Create problem selector
        currentProblem = problems.get(1);

        // TODO: Interpret data model and set up GUI accordingly rather than hardcoding to
        // specific data model and representation
        dataModel = currentProblem.getDefaultDataModel();

        if (dataModel instanceof AdjacencyListGraphDataModel) {
            AdjacencyListGraphDataModel algdm = (AdjacencyListGraphDataModel) dataModel;
            EditText et = (EditText) findViewById(R.id.number_nodes);
            et.setText(new Integer(algdm.Nodes).toString());
            et = (EditText) findViewById(R.id.number_edges);
            et.setText(new Integer(algdm.Edges).toString());
        }

        List<ISolution> solutions = currentProblem.getSolutions(problemRenderer);
        List<String> solutionNames = new LinkedList<>();
        for (Iterator it = solutions.iterator();
             it.hasNext(); ) {
            solutionNames.add(((Iterator<ISolution>) it).next().getName());
        }

        setupSpinner(solutionNames, R.id.solution_spinner);

        // TODO: This is ugly.  Have a class with generator meta info.
        Dictionary<Integer, String> generators = currentProblem.getGenerator(null).getMethods();
        LinkedList<String> names = new LinkedList<>();
        for (int i = 0; i < generators.size(); i++) {
            names.add(generators.get(i));
        }

        setupSpinner(names, R.id.generator_spinner);

        // TODO: Save and read selected solution from storage
        currentSolution = solutions.get(0);
    }

    private void setupSpinner(List<String> strings, int id) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                strings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(id);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    private void handleStop() {
        if (runThread != null && runThread.isAlive()) {
            runThread.interrupt();
            runThread = null;
        }
    }

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
    public void onRenderDone() {
        runThread = null;
        if (!cancelled) {
            setStatusText("Done!", 0xff00a080);
        }
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
}
