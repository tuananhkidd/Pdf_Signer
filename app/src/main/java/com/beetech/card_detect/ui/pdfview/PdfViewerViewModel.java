package com.beetech.card_detect.ui.pdfview;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import androidx.lifecycle.MutableLiveData;

import com.beetech.card_detect.base.BaseViewModel;
import com.beetech.card_detect.base.ObjectResponse;
import com.beetech.card_detect.tool.PDFTools;
import com.beetech.card_detect.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PdfViewerViewModel extends BaseViewModel {
    private String pdfPath;
    private String signPath;
    private int xPosition;
    private int yPosition;

    private MutableLiveData<ObjectResponse<File>> file = new MutableLiveData<>();

    private MutableLiveData<ObjectResponse> saveFile = new MutableLiveData<>();


    public MutableLiveData<ObjectResponse<File>> getFile() {
        return file;
    }

    public MutableLiveData<ObjectResponse> getSaveFile() {
        return saveFile;
    }

    @Inject
    public PdfViewerViewModel() {
    }

    public void processSignatureAction(Context context,int currentPage) {
        Callable<File> callable = () -> {
            PDFTools pdf = new PDFTools(context);
            File signatureFile = new File(pdfPath);
            File copyPdfFile = File.createTempFile("temp_file", ".pdf", context.getCacheDir());
            FileUtil.copy(signatureFile, copyPdfFile);
            pdf.open(copyPdfFile);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(signPath, options);
            pdf.insertImage(FileUtil.saveSignature(context, bitmap).getAbsolutePath(), xPosition, yPosition, 100, 80, currentPage+1);

            return copyPdfFile;
        };

        mDisposable.add(Single.fromCallable(callable)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    file.setValue(new ObjectResponse<File>().loading());
                })
                .subscribe(
                        tempfile -> {
                            file.setValue(new ObjectResponse<File>().success(tempfile));
                            saveFile();
                        },
                        throwable -> {
                            file.setValue(new ObjectResponse<File>().error(throwable));
                        }
                ));

    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public void saveFile() {
        if (file.getValue() != null) {
            File savePdfFile = file.getValue().getData();
            Callable<String> callable = () -> {
                try {
                    File copyPdfFile = File.createTempFile("sign_file", ".pdf", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
                    FileUtil.copy(savePdfFile, copyPdfFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            };
            mDisposable.add(Single.fromCallable(callable)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> {
                        saveFile.setValue(new ObjectResponse<>().loading());
                    })
                    .subscribe(
                            str -> {
                                saveFile.setValue(new ObjectResponse<>().success(str));
                            },
                            throwable -> {
                                saveFile.setValue(new ObjectResponse<>().error(throwable));
                            }
                    ));
        }

    }

    public void setSignPath(String path) {
        this.signPath = path;
    }

    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }
}
