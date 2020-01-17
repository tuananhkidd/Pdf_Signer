package com.beetech.card_detect.network.repository;

import com.beetech.card_detect.BaseApplication;
import com.beetech.card_detect.base.ListResponse;
import com.beetech.card_detect.di.RxPreference;
import com.beetech.card_detect.di.RxPreferenceImpl;
import com.beetech.card_detect.entity.SearchResponse;
import com.beetech.card_detect.entity.SignEntity;
import com.beetech.card_detect.network.ApiInterface;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class Repository {
    private final ApiInterface apiInterface;
    private final RxPreference rxPreference;

    @Inject
    Repository(ApiInterface apiInterface) {
        this.apiInterface = apiInterface;
        this.rxPreference = new RxPreferenceImpl(BaseApplication.getContext());
    }


    public void saveSignPositionInfo(SignEntity signEntity){
        rxPreference.saveSignPosition(signEntity);
    }

    public SignEntity getSignPositionInfo(){
        return rxPreference.getSignPositionInfo();
    }

    public Single<ResponseBody> signPdf(RequestBody signLocation,
                                        MultipartBody.Part pdfFile,
                                        MultipartBody.Part signFile){
        return apiInterface.signPdfFile(signLocation,pdfFile,signFile)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<ResponseBody> signPdfWithUrl(RequestBody signLocation,
                                        MultipartBody.Part pdfFile,
                                        MultipartBody.Part signFile){
        return apiInterface.signPdfFileWithUrl(signLocation,pdfFile,signFile)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
