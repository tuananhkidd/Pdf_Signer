package com.beetech.card_detect.base;

import androidx.annotation.NonNull;


import com.beetech.card_detect.utils.Define;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.reactivex.annotations.Nullable;

public class ListResponse<T> extends BaseResponse{
    private int type;

    @SerializedName("total_page")
    private int totalPage;

    @Nullable
    private List<T> data;

    @Nullable
    private Throwable error;

    public ListResponse() {
    }

    public ListResponse(int type, List<T> data, Throwable error) {
        this.type = type;
        this.data = data;
        this.error = error;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getType() {
        return type;
    }

    public List<T> getData() {
        return data;
    }

    public Throwable getError() {
        return error;
    }

    public ListResponse<T> loading() {
        return new ListResponse<>(Define.ResponseStatus.LOADING, null, null);
    }

    public ListResponse<T> success(@NonNull List<T> data) {
        return new ListResponse<>(Define.ResponseStatus.SUCCESS, data, null);
    }

    public ListResponse<T> error(@NonNull Throwable throwable) {
        return new ListResponse<>(Define.ResponseStatus.ERROR, null, throwable);
    }
}
