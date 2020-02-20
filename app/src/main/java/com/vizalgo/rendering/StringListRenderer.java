package com.vizalgo.rendering;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.SurfaceHolder;

import java.util.Collection;

public class StringListRenderer implements IRenderer {
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Collection<String> stringList;
    private int left, right;

    public void render() {
        if (canvas == null) {
            return;
        }
        int x = left, y = 0;
        Paint p = new Paint();
        Typeface tf = Typeface.create("Arial", Typeface.NORMAL);
        p.setTypeface(tf);
        p.setColor(Color.WHITE);
        p.setTextSize(12);
        Rect bounds = new Rect();
        for (String s : stringList) {
            canvas.drawText(s, x, y, p);
            p.getTextBounds(s, 0, s.length(), bounds);
            y += bounds.bottom;
        }
    }
    public void setCanvas(Canvas c) {
        canvas = c;
    }
    public void setHolder(SurfaceHolder s) {
        this.surfaceHolder = s;
    }
    public IRenderer getBaseRenderer() {
        return null;
    }

    @Override
    public boolean supportsCanvas() {
        return true;
    }

    @Override
    public boolean supportsRecyclerView() {
        return true;
    }

    public void setup(Collection<String> stringList, int left, int right) {
        this.stringList = stringList;
        this.left = left;
        this.right = right;
    }
}
