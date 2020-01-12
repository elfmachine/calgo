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

public class GarretsSubStringSolution implements ISolution {
    private StringListRenderer renderer;
    private StringListRenderer solutionRenderer;
    private IProgressListener listener;

    public String getName() {
        return new String("Ye Solutione of Substring d'Garret");
    }
    public Object solve(Object problem) {
        Map<String,Set<String>> solution = new HashMap<String,Set<String>>();
        Trie dictionaryTrie = new Trie();

        Set<String> dictionary = (Set<String>)problem;
        for (String s: dictionary) {
            dictionaryTrie.add(s);
        }
        for (String s: dictionary) {
            for (int i=0; i<s.length()-1; i++) {
                for (int j=0; j<s.length()-i; j++) {
                    String subString = s.substring(j,j+i);
                    if (dictionaryTrie.contains(subString)) {
                        Set<String> entry = solution.get(subString);
                        if (entry != null) {
                            entry.add(subString);
                        } else {
                            entry = new HashSet<String>();
                            entry.add(subString);
                            solution.put(s, entry);
                        }
                    }
                }
            }
        }

        ArrayList<String> render = new ArrayList<String>();
        for(Map.Entry<String,Set<String>> e : solution.entrySet()) {
            render.add(e.getKey());
            for (String v : e.getValue()) {
                render.add("  " + v);
            }
        }
        System.out.println(String.format("Solution (%d results):", render.size()));
        for (String line : render) {
            System.out.println(line);
        }
        solutionRenderer.setup(render, 100, 200);
        return solution;
    }

    public IRenderer getRenderer(Paint p) {
        renderer = new StringListRenderer();
        return renderer;
    }
    public IRenderer getSolutionRenderer(Paint p) {
        solutionRenderer = new StringListRenderer();
        return solutionRenderer;
    }
    public void setProgressListener(IProgressListener listener) {
        this.listener = listener;
    }
}
