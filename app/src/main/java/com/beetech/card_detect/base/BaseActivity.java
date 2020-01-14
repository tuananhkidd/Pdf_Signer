package com.beetech.card_detect.base;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;


import com.beetech.card_detect.network.NetworkCheckerInterceptor;
import com.beetech.card_detect.network.model.ApiObjectResponse;
import com.beetech.card_detect.network.model.RequestError;
import com.beetech.card_detect.utils.Define;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;

import dagger.android.support.DaggerAppCompatActivity;
import retrofit2.HttpException;

public abstract class BaseActivity<T extends ViewDataBinding> extends DaggerAppCompatActivity {

    protected T binding;

    protected ViewController mViewController;

    public static long lastClickTime = System.currentTimeMillis();

    private View focusedViewOnActionDown;
    private boolean touchWasInsideFocusedView, hasMove;
    private float rawX, rawY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, getLayoutId());
        mViewController = new ViewController(getSupportFragmentManager(), getFragmentContainerId());

        initView();
        initData();
    }

    public ViewController getViewController() {
        if (mViewController == null) {
            mViewController = new ViewController(getSupportFragmentManager(), getFragmentContainerId());
        }
        return mViewController;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public abstract int getLayoutId();

    public abstract int getFragmentContainerId();

    @Override
    public void onBackPressed() {
        if (mViewController != null && mViewController.getCurrentFragment() != null) {
            if (mViewController.getCurrentFragment().backPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Used for hide softKeyboard when touch outside
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rawX = ev.getRawX();
                rawY = ev.getRawY();
                hasMove = false;
                focusedViewOnActionDown = getCurrentFocus();
                if (focusedViewOnActionDown != null) {
                    final Rect rect = new Rect();
                    final int[] coordinates = new int[2];

                    focusedViewOnActionDown.getLocationOnScreen(coordinates);

                    rect.set(coordinates[0], coordinates[1],
                            coordinates[0] + focusedViewOnActionDown.getWidth(),
                            coordinates[1] + focusedViewOnActionDown.getHeight());

                    final int x = (int) ev.getX();
                    final int y = (int) ev.getY();

                    touchWasInsideFocusedView = rect.contains(x, y);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!hasMove) {
                    float delta = (float) Math.hypot(rawX - ev.getRawX(), rawY - ev.getRawY());
                    hasMove = delta > 6f;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (focusedViewOnActionDown != null) {
                    final boolean consumed = super.dispatchTouchEvent(ev);
                    final View currentFocus = getCurrentFocus();
                    if (hasMove) {
                        return consumed;
                    }
                    if (currentFocus.equals(focusedViewOnActionDown)) {
                        if (touchWasInsideFocusedView) {
                            return consumed;
                        }
                    } else if (currentFocus instanceof EditText) {
                        return consumed;
                    }

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(focusedViewOnActionDown.getWindowToken(), 0);
                    return consumed;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Nullable
    public RequestError handleNetworkError(Throwable throwable, boolean isShowDialog) {
        RequestError requestError = new RequestError();

        if (throwable instanceof NetworkCheckerInterceptor.NoConnectivityException) {
            requestError.setErrorCode(Define.Api.Error.NO_NETWORK_CONNECTION);
            requestError.setErrorMessage(throwable.getMessage());
        } else if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            String errorBody;
            try {
                errorBody = Objects.requireNonNull(httpException.response().errorBody()).string();
                Gson gson = new GsonBuilder().create();
                ApiObjectResponse apiResponse = gson.fromJson(errorBody, ApiObjectResponse.class);
                if (apiResponse != null && apiResponse.getRequestError() != null) {
                    requestError = apiResponse.getRequestError();
                } else {
                    requestError.setErrorCode(String.valueOf(httpException.code()));
                }
            } catch (IOException e) {
                requestError.setErrorCode(Define.Api.Error.TIME_OUT);
            } catch (IllegalStateException e) {
                requestError.setErrorCode(Define.Api.Error.TIME_OUT);
            } catch (JsonSyntaxException e) {
                requestError.setErrorCode(Define.Api.Error.TIME_OUT);
            }
        } else if (throwable instanceof ConnectException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof UnknownHostException
                || throwable instanceof IOException) {
            requestError.setErrorCode(Define.Api.Error.TIME_OUT);
        } else {
            requestError.setErrorCode(Define.Api.Error.UNKNOWN);
        }

        if (isShowDialog && requestError != null) {
            Toast.makeText(this, "Có lỗi xảy ra.Vui lòng thử lại", Toast.LENGTH_LONG).show();
        }

        return requestError;
    }

    protected boolean avoidDuplicateClick() {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < Define.CLICK_TIME_INTERVAL) {
            return true;
        }
        lastClickTime = now;
        return false;
    }

    public abstract void initView();

    public abstract void initData();
}
