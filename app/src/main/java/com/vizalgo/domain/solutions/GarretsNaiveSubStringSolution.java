package com.vizalgo.domain.solutions;

import android.graphics.Paint;

import com.vizalgo.domain.ISolution;
import com.vizalgo.primitives.IProgressListener;
import com.vizalgo.primitives.Trie;
import com.vizalgo.rendering.IRenderer;
import com.vizalgo.rendering.StringListRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GarretsNaiveSubStringSolution implements ISolution {
    private StringListRenderer renderer;
    private StringListRenderer solutionRenderer;
    private IProgressListener listener;

    public String getName() {
        return "Garret's Naive Solution";
    }
    public Object solve(Object problem) {
        Map<String, Set<String>> solution = new HashMap<>();
        Trie dictionaryTrie = new Trie();
        Set<String> dictionary = (Set<String>)problem;

        // Structure dictionary as a trie so it can be accessed easily.
        for (String s: dictionary) {
            dictionaryTrie.add(s);
        }
        // Look for existence all possible substrings of each word in trie-based dictionary.
        int prog = 1;
        int substrings = 0;
        for (String s: dictionary) {
            for (int subStrSize = 0; subStrSize < s.length(); subStrSize++) {
                for (int j = 0; j < s.length() - subStrSize; j++) {
                    String subString = s.substring(j, j + subStrSize);
                    if (dictionaryTrie.contains(subString)) {
                        Set<String> entry = solution.get(s);
                        if (entry != null) {
                            entry.add(subString);
                        } else {
                            entry = new HashSet<>();
                            entry.add(subString);
                            solution.put(s, entry);
                        }
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
        for(Map.Entry<String,Set<String>> e : solution.entrySet()) {
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
