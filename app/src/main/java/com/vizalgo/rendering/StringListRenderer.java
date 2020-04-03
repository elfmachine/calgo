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
    // If this is false, use recycler view
    private boolean render;

    public StringListRenderer(boolean render) {
        this.render = render;
    }

    public void render(Canvas canvas) {
        if (!render) return;
        canvas.drawColor(Color.rgb(0, 0, 0));
        int x = left, y = 0;
        Paint p = new Paint();
        Typeface tf = Typeface.create("Arial", Typeface.NORMAL);
        p.setTypeface(tf);
        p.setColor(Color.BLUE);
        p.setTextSize(20);
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
        return render;
    }

    @Override
    public boolean supportsRecyclerView() {
        return !render;
    }

    public void setup(Collection<String> stringList, int left, int right) {
        this.stringList = stringList;
        this.left = left;
        this.right = right;
    }
}
