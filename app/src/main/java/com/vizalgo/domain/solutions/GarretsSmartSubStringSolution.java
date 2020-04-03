package com.vizalgo.domain.solutions;

import android.graphics.Paint;

import com.vizalgo.domain.ISolution;
import com.vizalgo.primitives.IProgressListener;
import com.vizalgo.rendering.IRenderer;
import com.vizalgo.rendering.StringListRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GarretsSmartSubStringSolution implements ISolution {
    private StringListRenderer renderer;
    private StringListRenderer solutionRenderer;
    private IProgressListener listener;

    public String getName() {
        return "Garret's Smart Solution";
    }

    public Object solve(Object problem) {
        Map<String, Set<String>> solution = new HashMap<>();
        //Trie dictionaryTrie = new Trie();
        HashSet<String> dictionaryTrie = new HashSet<>();

        Set<String> dictionary = (Set<String>) problem;
        ArrayList<String> sortedList = new ArrayList<>(dictionary);

        // Sort from smallest to longest length so we can leverage solution lists from shorter
        // substrings.
        Collections.sort(sortedList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.length() - rhs.length();
            }
        });
        // Structure dictionary as a trie so it can be accessed easily.
        for (String s : sortedList) {
            dictionaryTrie.add(s);
        }
        // Look for existence all possible substrings of each word in trie-based dictionary.
        int prog = 1;
        int substrings = 0;
        for (String s : sortedList) {
            for (int subStrSize = s.length() - 1; subStrSize >= 0; subStrSize--) {
                for (int j = 0; j < s.length() - subStrSize; j++) {
                    String subString = s.substring(j, j + subStrSize);
                    if (dictionaryTrie.contains(subString)) {
                        Set<String> entry = solution.get(s);
                        if (entry != null) {
                            entry.add(subString);
                        } else {
                            entry = new HashSet<String>();
                            entry.add(subString);
                            solution.put(s, entry);
                        }
                        // Add all found substrings of the new match, since they are guaranteed to also
                        // be substrings of the original string.
                        Set<String> leafStrings = solution.get(subString);
                        if (leafStrings != null) {
                            entry.addAll(leafStrings);
                        }
                        // TODO: Save begin and end offset, check all offsets on future iterations.
                    }
                }
            }
            if (solution.get(s) != null) {
                substrings += solution.get(s).size();
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            listener.onProgress((int) (prog++ * 100.0 / dictionary.size()));
        }
        System.out.println(String.format("From %d words, found %d strings with %d substrings",
                dictionary.size(), solution.size(), substrings));

        ArrayList<String> render = new ArrayList<String>();
        for (Map.Entry<String, Set<String>> e : solution.entrySet()) {
            render.add(e.getKey());
            for (String v : e.getValue()) {
                render.add("  " + v);
            }
        }
        return render;
    }

    public IRenderer getRenderer(Paint p) {
        renderer = new StringListRenderer(true);
        return renderer;
    }

    public IRenderer getSolutionRenderer(Paint p) {
        solutionRenderer = new StringListRenderer(false);
        return solutionRenderer;
    }

    public void setProgressListener(IProgressListener listener) {
        this.listener = listener;
    }
}
