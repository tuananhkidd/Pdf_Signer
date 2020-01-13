package com.beetech.card_detect.base;

import androidx.annotation.NonNull;


import com.beetech.card_detect.utils.Define;

import io.reactivex.annotations.Nullable;

public class ObjectResponse<T> extends BaseResponse {
    private int type;

    @Nullable
    private T data;

    @Nullable
    private Throwable error;

    public ObjectResponse() {
    }

    public int getType() {
        return type;
    }

    public T getData() {
        return data;
    }

    public Throwable getError() {
        return error;
    }

    private ObjectResponse(int status, T data, Throwable error) {
        this.type = status;
        this.data = data;
        this.error = error;
    }

    public ObjectResponse<T> loading() {
        return new ObjectResponse<>(Define.ResponseStatus.LOADING, null, null);
    }

    public ObjectResponse<T> success(@NonNull T data) {
        return new ObjectResponse<>(Define.ResponseStatus.SUCCESS, data, null);
    }

    public ObjectResponse<T> error(@NonNull Throwable throwable) {
        return new ObjectResponse<>(Define.ResponseStatus.ERROR, null, throwable);
    }
}
