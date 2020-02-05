package com.beetech.card_detect.ui.pdfview.v2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PdfViewerV2ViewModel extends BaseViewModel {
    private String pdfPath;
    private String signPath;
    private MutableLiveData<ObjectResponse<File>> file = new MutableLiveData<>();
    private Repository repository;
    private MutableLiveData<ObjectResponse<String>> signPdfWithUrl = new MutableLiveData<>();

    public MutableLiveData<ObjectResponse<String>> getSignPdfWithUrl() {
        return signPdfWithUrl;
    }
    public MutableLiveData<ObjectResponse<File>> getFile() {
        return file;
    }

    @Inject
    public PdfViewerV2ViewModel(Repository repository) {
        this.repository = repository;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }


    public void setSignPath(String path) {
        this.signPath = path;
    }

    public void processSignatureAction(Context context) {
        Callable<File> callable = () -> {
            PDFTools pdf = new PDFTools(context);
            File signatureFile = new File(pdfPath);
            File copyPdfFile = new File(context.getCacheDir(), "temp_file.pdf");
            FileUtil.copy(signatureFile, copyPdfFile);
            pdf.open(copyPdfFile);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(signPath, options);
            //fixme position signature
            pdf.insertImage(FileUtil.saveSignature(context, bitmap).getAbsolutePath(), 350, 257, 70, 23, 6);

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
                        },
                        throwable -> {
                            file.setValue(new ObjectResponse<File>().error(throwable));
                        }
                ));

    }

    public void signPdfFile() {
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

        //fixme position signature
        String signLocation = "350,257,420,280,5";
        RequestBody signLocationBody = RequestBody.create(MediaType.parse("text/plain"), signLocation);
        mDisposable.add(repository.signPdfWithUrl(signLocationBody, pdfBody, signBody)
                .doOnSubscribe(disposable -> {
                    signPdfWithUrl.setValue(new ObjectResponse<String>().loading());
                })
                .subscribe(
                        body -> {
                            signPdfWithUrl.setValue(new ObjectResponse<String>().success(body.string()));
                        },
                        throwable -> {
                            signPdfWithUrl.setValue(new ObjectResponse<String>().error(throwable));
                        }
                ));
    }
}
