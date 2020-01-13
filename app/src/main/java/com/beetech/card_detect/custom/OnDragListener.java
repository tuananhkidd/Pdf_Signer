package com.beetech.card_detect.custom;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;

public class OnDragListener implements View.OnTouchListener {
    //    private int screenHight;
//    private int screenWidth;
    private int lastAction;
    private float dX;
    private float dY;

    private PDFView pdfView;

    public OnDragListener(PDFView pdfView) {
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        this.screenHight = screenHight;
//        this.screenWidth = screenWidth;
        this.pdfView = pdfView;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float newX, newY;

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

                dX = view.getX() - motionEvent.getRawX();
                dY = view.getY() - motionEvent.getRawY();
                lastAction = MotionEvent.ACTION_DOWN;
                break;

            case MotionEvent.ACTION_MOVE:

                newX = motionEvent.getRawX() + dX;
                newY = motionEvent.getRawY() + dY;

                int screenWidth = (int) pdfView.getPageSize(pdfView.getCurrentPage()).getWidth();
                int screenHight = (int) pdfView.getPageSize(pdfView.getCurrentPage()).getHeight();
                // check if the view out of screen
                if ((newX <= 0 || newX >= screenWidth - view.getWidth()) || (newY <= 0 || newY >= screenHight - view.getHeight())) {
                    lastAction = MotionEvent.ACTION_MOVE;
                    break;
                }

                view.setX(newX);
                view.setY(newY);

                lastAction = MotionEvent.ACTION_MOVE;
                break;

            case MotionEvent.ACTION_UP:
                if (lastAction == MotionEvent.ACTION_DOWN) {
                    //TODO
                    Log.v("ahuhu","ahihi");
                }
                break;

            default:
                return false;
        }
        return true;
    }
}
