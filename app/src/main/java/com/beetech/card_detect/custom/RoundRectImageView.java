package com.beetech.card_detect.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class RoundRectImageView extends AppCompatImageView {
    private Path path;
    private RectF rectF;

    public RoundRectImageView(Context context) {
        super(context);
        init();
    }

    public RoundRectImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundRectImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rectF = new RectF(0f, 0f, this.getWidth(), this.getHeight());
        path.addRoundRect(rectF, 0, 0, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
