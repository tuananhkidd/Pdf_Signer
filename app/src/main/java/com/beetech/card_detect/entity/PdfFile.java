package com.beetech.card_detect.entity;

import android.graphics.Bitmap;

public class PdfFile {
    private Bitmap bitmap;
    private boolean isLoaded;

    public PdfFile(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.isLoaded = false;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }
}
