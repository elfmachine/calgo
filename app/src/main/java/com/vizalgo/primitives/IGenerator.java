package com.vizalgo.primitives;

import java.security.InvalidKeyException;
import java.util.Dictionary;

/**
 * Created by garret on 12/11/15.
 */
public interface IGenerator {
    Object generate(int method);
    void setCoordinates(int x0, int y0, int x1, int y1);
    Dictionary<Integer, String> getMethods();
}
