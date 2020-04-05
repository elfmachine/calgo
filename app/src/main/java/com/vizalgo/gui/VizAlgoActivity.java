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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
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
import com.vizalgo.domain.problems.CheckableOption;

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

    private boolean cancelled;
    private boolean renderError;

    private Object dataModel;
    private SharedPreferences sharedPref;

    private DataModelTranslator translator;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        translator = new DataModelTranslator(sharedPref);

        setContentView(R.layout.activity_viz_algo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<String> problemNames = new LinkedList<>();
        for (Iterator it = problems.iterator();
             it.hasNext(); ) {
            problemNames.add(((Iterator<IProblem>) it).next().getName());
        }

        setupSpinner("problem", problemNames, R.id.problem_spinner);
        currentProblem = problems.get(sharedPref.getInt("problem", 0));
        initProblem();

        // Set up render view.
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.vizalgo_root_view);
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

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        for (Field f : dataModel.getClass().getFields()) {
            try {
                int group = Menu.FIRST;
                if (f.getType().equals(CheckableOption.class)) {
                    CheckableOption checkableOption = (CheckableOption) f.get(dataModel);
                    SubMenu subMenu = menu.addSubMenu(checkableOption.getTitle());
                    int i = Menu.FIRST;
                    for (String item : checkableOption.getOptions()) {
                        MenuItem menuItem = subMenu.add(group, i, i, item);
                        menuItem.setCheckable(true);
                        i++;
                    }
                    subMenu.getItem(checkableOption.Value).setChecked(true);
                    subMenu.setGroupCheckable(group, true, true);
                    subMenu.setGroupEnabled(group, true);
                    group++;
                }
            } catch (IllegalAccessException e) {
                System.out.println(
                        String.format("IllegalAccessException constructing menu for: %s\n%s"
                                + f.getName(), e));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.isCheckable()) {
            item.setChecked(true);
            translator.setOptionValue(dataModel, item.getTitle().toString());
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (parent.getId() == R.id.problem_spinner) {
            currentProblem = problems.get(pos);
            setPrefInt("problem", pos);
            translator.setProblemName(currentProblem.getName());
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

    public void startSolver(View view) {
        renderError = false;
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

    private TextWatcher updateAllFieldsWatcher =
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    translator.updateDataModelFromTextFields(findViewById(R.id.graphOptions), dataModel);
                    translator.writeDataModel(dataModel);
                }
            };

    private void initProblem() {
        dataModel = currentProblem.getDefaultDataModel();
        translator.setProblemName(currentProblem.getName());
        ;
        LinearLayout layout = (LinearLayout) findViewById(R.id.graphOptions);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.removeAllViews();
        translator.readDataModel(dataModel, f -> {
            // Add TextView which describes the object.
            TextView textView = new TextView(this);
            textView.setText(f.getName());
            layout.addView(textView);

            // Add EditText to edit the object.
            EditText editText = new EditText(this);
            try {
                editText.setText(f.get(dataModel).toString());
            } catch (IllegalAccessException iae) {
                System.out.println("Fuck, we got an IllegalAccessException in the lambda.");
            }
            editText.addTextChangedListener(updateAllFieldsWatcher);
            layout.addView(editText);
        });

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
        invalidateOptionsMenu();
    }

    private void setupSpinner(String pref, List<String> strings, int id) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                strings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(id);
        spinner.setAdapter(adapter);
        int value = sharedPref.getInt(pref, -1);
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
        SharedPreferences.Editor e = sharedPref.edit();
        e.putInt(option, value);
        e.apply();
    }
}
