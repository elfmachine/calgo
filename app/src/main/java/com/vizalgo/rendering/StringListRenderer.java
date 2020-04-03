package com.vizalgo.rendering;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.TextureView;

import java.util.Collection;

public class StringListRenderer implements IRenderer {
    private TextureView textureView;
    private Collection<String> stringList;
    private int left, right;

    public void render(Canvas canvas) {
        // TODO: Figure out what to do with this.
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.rgb(0, 0, 0));
        int x = left, y = 0;
        Paint p = new Paint();
        Typeface tf = Typeface.create("Arial", Typeface.NORMAL);
        p.setTypeface(tf);
        p.setColor(Color.BLUE);
        p.setTextSize(12);
        Rect bounds = new Rect();
        for (String s : stringList) {
            canvas.drawText(s, x, y, p);
            p.getTextBounds(s, 0, s.length(), bounds);
            y += bounds.bottom;
        }
    }

    public void setTextureView(TextureView tv) {
        textureView = tv;
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
