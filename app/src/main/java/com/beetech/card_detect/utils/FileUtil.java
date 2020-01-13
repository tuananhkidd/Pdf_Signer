package com.beetech.card_detect.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

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
            File output = new File(context.getCacheDir(), Define.SIGN_IMAGE_PATH);
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

    public static String getMimeType(String url) {
        String type = "";
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String saveFile(ResponseBody responseBody) {
        try {
            File copyPdfFile = File.createTempFile("sign_file_", ".pdf", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
            InputStream inputStream = responseBody.byteStream();
            FileOutputStream outputStream = new FileOutputStream(copyPdfFile);
            byte[] byteData = new byte[4096];
            while (true) {
                int content = inputStream.read(byteData);
                if (content == -1) {
                    break;
                }
                outputStream.write(byteData,0,content);
            }
            outputStream.flush();
            return copyPdfFile.getPath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
