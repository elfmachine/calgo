package com.vizalgo.domain.problems;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by garrethamann on 12/25/15.
 */
public class AdjacencyListGraphDataModel {
    public CheckableOption RenderOption = new CheckableOption() {
        @Override
        public String getTitle() {
            return "Render Options";
        }

        @Override
        public List<String> getOptions() {
            // TODO: Make this nicer.
            LinkedList ll = new LinkedList();
            ll.add("Draw Everything");
            ll.add("Only Traversal");
            ll.add("Only On Completion");
            return ll;
        }
    };

    public int Nodes;
    public int Edges;
}

