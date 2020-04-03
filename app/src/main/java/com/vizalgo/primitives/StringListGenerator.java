package com.vizalgo.primitives;


import android.content.Context;

import com.garretware.graphtestapp.R;
import com.vizalgo.domain.problems.StringListDataModel;
import com.vizalgo.rendering.StringListRenderer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class StringListGenerator implements IGenerator {
    public enum SupportedMethods {RANDOM_FROM_FILE, FIRST_N}

    private Context context;
    private ArrayList<String> dictionary = new ArrayList<String>();
    private Set<String> stringList = new HashSet<String>();
    private StringListDataModel dataModel;
    private StringListRenderer renderer;
    private int x0, y0, x1, y1;

    public StringListGenerator(Context context, StringListDataModel dataModel, StringListRenderer renderer) {
        this.context = context;
        this.dataModel = dataModel;
        this.renderer = renderer;
    }

    @Override
    public Object generate(int method) {
        InputStream is = null;
        try {
            is = context.getResources().openRawResource(R.raw.words);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = reader.readLine()) != null) {
                dictionary.add(str);
            }
        }
        catch (FileNotFoundException ex) {
            System.out.println("Could not open words.txt: " + ex);
        } catch (IOException ex) {
            System.out.println("Error reading from words.txt: " + ex);
        } finally {
            try {if (is != null) { is.close(); } } catch (Throwable ignore) {}
        }
        int numberOfWords = dataModel.NumberOfStrings;

        if (numberOfWords > dictionary.size()) {
            throw new InvalidParameterException(
                    String.format("Requested more words (%d) than available in dictionary (%d)",
                            numberOfWords, dictionary.size()));
        }

        if (method == SupportedMethods.RANDOM_FROM_FILE.ordinal()) {
            HashSet<Integer> items = new HashSet<>();
            for (int i = 0; i < numberOfWords; i++) {
                int n;
                do {
                    n = (int) (Math.random() * dictionary.size()) % dictionary.size();
                } while (items.contains(n));
                items.add(n);
                System.out.println(String.format("Adding item at %d", n));
                stringList.add(dictionary.get(n));
            }
            renderer.setup(stringList, (x1 - x0) / 2, x1);
            return stringList;
        } else if (method == SupportedMethods.FIRST_N.ordinal()) {
            stringList.addAll(dictionary.subList(0, numberOfWords));
            renderer.setup(stringList, (x1 - x0) / 2, x1);
            return stringList;
        }
        throw new InvalidParameterException("Generate method " + method + " not supported.");
    }

    @Override
    public void setCoordinates(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
    }

    @Override
    public Dictionary<Integer, String> getMethods() {
        Hashtable<Integer, String> result = new Hashtable<>();
        int index = 0;
        for (SupportedMethods val : SupportedMethods.values()) {
            result.put(index++, val.toString());
        }
        return result;
    }
}
