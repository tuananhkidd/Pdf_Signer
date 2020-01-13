package com.beetech.card_detect.base;

import androidx.annotation.NonNull;

import com.beetech.card_detect.utils.Define;

import java.util.List;

public class ListLoadmoreReponse<T> extends ListResponse<T> {
    private boolean isRefresh;
    private boolean canLoadmore;

    public ListLoadmoreReponse() {
    }

    public boolean isCanLoadmore() {
        return canLoadmore;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public ListLoadmoreReponse(int status, List<T> data, Throwable error, boolean isRefresh, boolean canLoadmore) {
        super(status, data, error);
        this.isRefresh = isRefresh;
        this.canLoadmore = canLoadmore;
    }

    public ListLoadmoreReponse<T> success(@NonNull List<T> data, boolean isRefresh, boolean canLoadmore) {
        this.isRefresh = isRefresh;
        this.canLoadmore = canLoadmore;
        return new ListLoadmoreReponse<>(Define.ResponseStatus.SUCCESS, data, null, isRefresh, canLoadmore);
    }

    public ListLoadmoreReponse<T> loading() {
        return new ListLoadmoreReponse<>(Define.ResponseStatus.LOADING, null, null, false, false);
    }

    public ListLoadmoreReponse<T> error(@NonNull Throwable throwable) {
        return new ListLoadmoreReponse<>(Define.ResponseStatus.ERROR, null, throwable, false, false);
    }
}
