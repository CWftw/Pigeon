package io.github.jameswolfeoliver.library.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class CircleCutout extends FrameLayout {
    private Paint paint;
    private float radius;
    private Rect rect;

    public CircleCutout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setColor(Color.TRANSPARENT);
        this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        this.rect = new Rect();
    }

    public void setRadius(float radius) {
        this.radius = radius;
        postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        getLocalVisibleRect(rect);
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius, paint);
        super.onDraw(canvas);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }
}
