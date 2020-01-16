package com.beetech.card_detect.custom.gesture;

import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

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
    private GestureDetector mGestureListener;

    private View mView;
    private View mParent;
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
    private float maximumScale = 1.5f;
    private boolean isPreventDrag = false;

    private OnDragActionListener mOnDragActionListener;
    private ScaleGestureDetector detector;
    private OnGestureControl mOnGestureControl;
    private boolean mIsTextPinchZoomable = true;
    private boolean isBiggerScale = true;

    public interface OnGestureControl {
        void onDoubleTab(boolean isBiggerScale);
    }

    public OnDragTouchListener( View view, View parent, OnDragActionListener onDragActionListener) {
        initListener(view, parent);
        setOnDragActionListener(onDragActionListener);
        detector = new ScaleGestureDetector(new ScaleGestureListener());
        mGestureListener = new GestureDetector(new GestureListener());
    }

    public void setOnDragActionListener(OnDragActionListener onDragActionListener) {
        mOnDragActionListener = onDragActionListener;
    }

    public void initListener(View view, View parent) {
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

        Rect rect = new Rect();
        mView.getHitRect(rect);

        width = rect.width();
        height = rect.height();
    }

    public void updateParentBounds() {
        maxLeft = 0 - paddingView;
        maxRight = maxLeft + mParent.getWidth() + paddingView + paddingView;

        maxTop = 0 - paddingView;
        maxBottom = maxTop + mParent.getHeight() + paddingView + paddingView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        detector.onTouchEvent(v,event);
        mGestureListener.onTouchEvent(event);
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
                    if (now - lastTouchTime < 200) {
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

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mOnGestureControl != null) {
                mOnGestureControl.onDoubleTab(isBiggerScale);
                isBiggerScale = !isBiggerScale;
            }
            return true;
        }
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mPivotX;
        private float mPivotY;
        private Vector2D mPrevSpanVector = new Vector2D();

        @Override
        public boolean onScaleBegin(View view, ScaleGestureDetector detector) {
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mPrevSpanVector.set(detector.getCurrentSpanVector());
            return mIsTextPinchZoomable;
        }

        @Override
        public boolean onScale(View view, ScaleGestureDetector detector) {
            TransformInfo info = new TransformInfo();
            info.deltaScale =  detector.getScaleFactor();
            info.deltaAngle =  Vector2D.getAngle(mPrevSpanVector, detector.getCurrentSpanVector()) ;
            info.deltaX =  detector.getFocusX() - mPivotX;
            info.deltaY =  detector.getFocusY() - mPivotY;
            info.pivotX = mPivotX;
            info.pivotY = mPivotY;
            info.minimumScale = minimumScale;
            info.maximumScale = maximumScale;
            move(view, info);
            return !mIsTextPinchZoomable;
        }
    }

    private void move(View view, TransformInfo info) {
        float scale = view.getScaleX() * info.deltaScale;
        scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale));
        view.setScaleX(scale);
        view.setScaleY(scale);
        mView.setPivotX(0);
        mView.setPivotY(0);
        updateRestrictDrag(view);
    }

    public void scaleView(boolean isBiggerScale) {
//        if (isBiggerScale) {
//            mView.setScaleX(1.1f);
//            mView.setScaleY(1.1f);
//        } else {
//            mView.setScaleX(0.8f);
//            mView.setScaleY(0.8f);
//        }
//
//        mView.setPivotX(0);
//        mView.setPivotY(0);
//
//        updateRestrictDrag(mView);
    }

    private void updateRestrictDrag(View view) {
        Rect rect = new Rect();
        view.getHitRect(rect);

        width = rect.width();
        height = rect.height();
    }

    public void setOnGestureControl(OnGestureControl onGestureControl) {
        mOnGestureControl = onGestureControl;
    }

    private class TransformInfo {
        float deltaX;
        float deltaY;
        float deltaScale;
        float deltaAngle;
        float pivotX;
        float pivotY;
        float minimumScale;
        float maximumScale;
    }
}
