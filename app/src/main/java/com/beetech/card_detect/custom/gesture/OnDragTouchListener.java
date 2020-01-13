package com.beetech.card_detect.custom.gesture;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.beetech.card_detect.utils.Define;
import com.github.barteksc.pdfviewer.PDFView;


public class OnDragTouchListener implements View.OnTouchListener {

    /**
     * Callback used to indicate when the drag is finished
     */
    public interface OnDragActionListener {
        /**
         * Called when drag event is started
         *
         * @param view The view dragged
         */
        void onDragStart(View view);

        /**
         * Called when drag event is completed
         *
         * @param view The view dragged
         */
        void onDragEnd(View view, boolean isClickDetected);
    }

    public interface OnClickDetectedListener {
        void onClickDetected(boolean isClickDetected);
    }

    private OnClickDetectedListener onClickDetectedListener;

    public long lastTouchTime = System.currentTimeMillis();

    private View mView;
    private PDFView mParent;
    private boolean isDragging;
    private boolean isInitialized = false;

    private int width;
    private float xWhenAttached;
    private float maxLeft;
    private float maxRight;
    private float dX;

    private int height;
    private float yWhenAttached;
    private float maxTop;
    private float maxBottom;
    private float dY;

    private int paddingView = 0;

    private float minimumScale = 0.5f;
    private float maximumScale = 10.0f;
    private float scale = 1f;
    private boolean isPreventDrag = false;

    private OnDragActionListener mOnDragActionListener;
    private ScaleGestureDetector detector;

    public OnDragTouchListener(Context context,View view, PDFView parent, OnDragActionListener onDragActionListener) {
        initListener(view, parent);
        setOnDragActionListener(onDragActionListener);
        detector = new ScaleGestureDetector(context, new MyScaleListener(view));
    }

    public void setOnDragActionListener(OnDragActionListener onDragActionListener) {
        mOnDragActionListener = onDragActionListener;
    }

    public void initListener(View view, PDFView parent) {
        mView = view;
        mParent = parent;
        isDragging = false;
        isInitialized = false;
    }

    public void updateBounds() {
        updateViewBounds();
        updateParentBounds();
        isInitialized = true;
    }

    public void updateViewBounds() {
        width = mView.getWidth();
        xWhenAttached = mView.getX();
        dX = 0;

        height = mView.getHeight();
        yWhenAttached = mView.getY();
        dY = 0;
    }

    public void updateParentBounds() {
        maxLeft = 0 - paddingView;
        maxRight = maxLeft + mParent.getPageSize(mParent.getCurrentPage()).getWidth() + paddingView + paddingView;

        maxTop = 0 - paddingView;
        maxBottom = maxTop + mParent.getPageSize(mParent.getCurrentPage()).getHeight() + paddingView + paddingView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        detector.onTouchEvent(event);
        if(isPreventDrag){
            return true;
        }
        if (isDragging) {
            float[] bounds = new float[4];
            // LEFT
            bounds[0] = event.getRawX() + dX;
            if (bounds[0] < maxLeft) {
                bounds[0] = maxLeft;
            }
            // RIGHT
            bounds[2] = bounds[0] + width;
            if (bounds[2] > maxRight) {
                bounds[2] = maxRight;
                bounds[0] = bounds[2] - width;
            }
            // TOP
            bounds[1] = event.getRawY() + dY;
            if (bounds[1] < maxTop) {
                bounds[1] = maxTop;
            }
            // BOTTOM
            bounds[3] = bounds[1] + height;
            if (bounds[3] > maxBottom) {
                bounds[3] = maxBottom;
                bounds[1] = bounds[3] - height;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    boolean isClickDetected = false;
                    long now = System.currentTimeMillis();
                    if (now - lastTouchTime < Define.TOUCH_TIME_INTERVAL) {
                        if (onClickDetectedListener != null) {
                            isClickDetected = true;
                            onClickDetectedListener.onClickDetected(true);
                        }
                    }
                    lastTouchTime = now;
                    onDragFinish(isClickDetected);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mView.animate().x(bounds[0]).y(bounds[1]).setDuration(0).start();
                    break;
            }
            return true;
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchTime = System.currentTimeMillis();
                    isDragging = true;
                    if (!isInitialized) {
                        updateBounds();
                    }
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    if (mOnDragActionListener != null) {
                        mOnDragActionListener.onDragStart(mView);
                    }
                    return true;
            }
        }
        return false;
    }

    private void onDragFinish(boolean isClickDetected) {
        if (mOnDragActionListener != null) {
            mOnDragActionListener.onDragEnd(mView, isClickDetected);
        }

        dX = 0;
        dY = 0;
        isDragging = false;
    }

    public void setPaddingView(int paddingView) {
        this.paddingView = paddingView;
    }

    public void setPreventDrag(boolean preventDrag) {
        isPreventDrag = preventDrag;
    }

    public void setOnClickDetectedListener(OnClickDetectedListener onClickDetectedListener) {
        this.onClickDetectedListener = onClickDetectedListener;
    }

    private class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        float onScaleBegin = 0;
        float onScaleEnd = 0;
        View imageView;

        public MyScaleListener(View imageView) {
            this.imageView = imageView;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            // Toast.makeText(getApplicationContext(), "Scale Begin", Toast.LENGTH_SHORT).show();
            Log.i("scale_tag", "Scale Begin");
            onScaleBegin = scale;

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            // Toast.makeText(getApplicationContext(), "Scale Ended", Toast.LENGTH_SHORT).show();
            Log.i("scale_tag", "Scale End");
            onScaleEnd = scale;

            if (onScaleEnd > onScaleBegin) {
                // Toast.makeText(getApplicationContext(), "Scaled Up by a factor of  " + String.valueOf(onScaleEnd / onScaleBegin), Toast.LENGTH_SHORT).show();
                Log.i("scale_tag", "Scaled Up by a factor of  " + String.valueOf(onScaleEnd / onScaleBegin));
            }

            if (onScaleEnd < onScaleBegin) {
                // Toast.makeText(getApplicationContext(), "Scaled Down by a factor of  " + String.valueOf(onScaleBegin / onScaleEnd), Toast.LENGTH_SHORT).show();
                Log.i("scale_tag", "Scaled Down by a factor of  " + String.valueOf(onScaleBegin / onScaleEnd));
            }

            super.onScaleEnd(detector);
        }
    }
}
