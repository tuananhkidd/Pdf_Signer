package com.beetech.card_detect.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    public static String saveBitmapToPNG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();

        return photo.getPath();
    }

    public static File saveSignature(Context context, Bitmap signature) {
        try {
            File output = new File(context.getCacheDir(), "signer.png");
            saveBitmapToPNG(signature, output);
            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File("");
    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public static String getPathFromUri(Context context, Uri uri) {
        try {

            String filePathCol[] = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, filePathCol, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int colIndex = cursor.getColumnIndex(filePathCol[0]);
                return cursor.getString(colIndex);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

}
