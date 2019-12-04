package com.vizalgo.domain;

import java.util.ArrayList;

import com.vizalgo.domain.problems.CyclesInADirectedGraph;

/**
 * Created by garret on 12/11/15.
 */
public class ProblemFactory {
    static public ArrayList<IProblem> getProblemSet() {

        ArrayList<IProblem> problemSet = new ArrayList<IProblem>();

        // TODO: Use reflection, probably need a third party library to find all instances
        // of IProblem in solution domain (including installed JREs)

        problemSet.add(new CyclesInADirectedGraph());

        return problemSet;
    }
}
