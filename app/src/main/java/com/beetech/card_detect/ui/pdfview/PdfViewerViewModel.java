package com.beetech.card_detect.ui.pdfview;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.beetech.card_detect.BaseApplication;
import com.beetech.card_detect.base.BaseViewModel;
import com.beetech.card_detect.base.ObjectResponse;
import com.beetech.card_detect.network.repository.Repository;
import com.beetech.card_detect.tool.PDFTools;
import com.beetech.card_detect.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class PdfViewerViewModel extends BaseViewModel {
    private String pdfPath;
    private String signPath;
    private double xFirstPosition;
    private double yFirtstPosition;
    private double xSecondPosition;
    private double ySecondPosition;
    private int signPage;
    private Repository repository;

    private MutableLiveData<ObjectResponse<File>> file = new MutableLiveData<>();

    private MutableLiveData<ObjectResponse> saveFile = new MutableLiveData<>();

    private MutableLiveData<ObjectResponse<ResponseBody>> signPdf = new MutableLiveData<>();

    public MutableLiveData<ObjectResponse<ResponseBody>> getSignPdf() {
        return signPdf;
    }

    public MutableLiveData<ObjectResponse<File>> getFile() {
        return file;
    }

    public MutableLiveData<ObjectResponse> getSaveFile() {
        return saveFile;
    }

    @Inject
    public PdfViewerViewModel(Repository repository) {
        this.repository = repository;
    }

    public void processSignatureAction(Context context, int currentPage) {
        Callable<File> callable = () -> {
            PDFTools pdf = new PDFTools(context);
            File signatureFile = new File(pdfPath);
            File copyPdfFile = new File(context.getCacheDir(), "temp_file.pdf");
            FileUtil.copy(signatureFile, copyPdfFile);
            pdf.open(copyPdfFile);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(signPath, options);
//            pdf.insertImage(FileUtil.saveSignature(context, bitmap).getAbsolutePath(), xPosition, yPosition, 100, 80, currentPage + 1);

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
//                            saveFile();
                        },
                        throwable -> {
                            file.setValue(new ObjectResponse<File>().error(throwable));
                        }
                ));

    }

    public void signPdfFile(int currentPage) {
        if (TextUtils.isEmpty(this.pdfPath)) {
            Toast.makeText(BaseApplication.getContext(), "Chưa chọn file pdf", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(this.signPath)) {
            Toast.makeText(BaseApplication.getContext(), "Chưa chọn chữ ký", Toast.LENGTH_SHORT).show();
            return;
        }

        File pdfFile = new File(pdfPath);
        File signFile = new File(signPath);
        RequestBody pdfRequestFile =
                RequestBody.create(MediaType.parse("application/pdf"), pdfFile);
        MultipartBody.Part pdfBody =
                MultipartBody.Part.createFormData("pdf-file", pdfFile.getName(), pdfRequestFile);

        RequestBody signRequestFile =
                RequestBody.create(MediaType.parse(FileUtil.getMimeType(signFile.getPath())), signFile);
        MultipartBody.Part signBody =
                MultipartBody.Part.createFormData("signed-image", signFile.getName(), signRequestFile);

        String signLocation = this.xFirstPosition + "," + (this.yFirtstPosition) + "," + (this.xSecondPosition) + "," + this.ySecondPosition + "," + signPage;
        RequestBody signLocationBody = RequestBody.create(MediaType.parse("text/plain"), signLocation);
        mDisposable.add(repository.signPdf(signLocationBody, pdfBody, signBody)
                .doOnSubscribe(disposable -> {
                    signPdf.setValue(new ObjectResponse<ResponseBody>().loading());
                })
                .subscribe(
                        body -> {
                            signPdf.setValue(new ObjectResponse<ResponseBody>().success(body));
                        },
                        throwable -> {
//                            Toast.makeText(BaseApplication.getContext(), "error " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            signPdf.setValue(new ObjectResponse<ResponseBody>().error(throwable));
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
                    File copyPdfFile = File.createTempFile("sign_file_", ".pdf", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
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

    public void setPositionSign(double xFirstPosition, double yFirtstPosition, double xSecondPosition, double ySecondPosition,int signPage) {
        this.xFirstPosition = xFirstPosition;
        this.yFirtstPosition = yFirtstPosition;

        this.xSecondPosition = xSecondPosition;
        this.ySecondPosition = ySecondPosition;

        this.signPage = signPage;
    }

//    public void initDataIfExist() {
//        SignEntity signEntity = repository.getSignPositionInfo();
//        if (signEntity != null) {
//            signPdf.setValue(signEntity);
//        }
//    }
//
//    public void saveCurrentPage(int currentPage) {
//        SignEntity signEntity = repository.getSignPositionInfo();
//        signEntity.setCurrentPage(currentPage);
//        repository.saveSignPositionInfo(signEntity);
//    }
//
//    public int getCurrentPageInStorage() {
//        if (signPdf.getValue() != null) {
//            return signPdf.getValue().getCurrentPage();
//        }
//        return 0;
//    }
}
