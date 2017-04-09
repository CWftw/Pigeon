package io.github.jameswolfeoliver.library.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;


public class CutoutModel extends ImageView {

    CircleCutout circleCutout;
    private Rect rect;

    public CutoutModel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.rect = new Rect();
    }

    public void setCutoutView(CircleCutout circleCutout) {
        this.circleCutout = circleCutout;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (circleCutout != null) {
            getLocalVisibleRect(rect);
            circleCutout.setRadius(rect.width()/2);
        }
    }
}
