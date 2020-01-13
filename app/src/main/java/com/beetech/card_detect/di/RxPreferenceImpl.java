package com.beetech.card_detect.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.beetech.card_detect.entity.SignEntity;
import com.beetech.card_detect.utils.Define;
import com.google.gson.Gson;

import javax.inject.Inject;

public class RxPreferenceImpl implements RxPreference {
    private final SharedPreferences mPrefs;

    private static final String PREF_KEY_SIGN_INFO = "PREF_KEY_SIGN_INFO";

    @Inject
    public RxPreferenceImpl(Context context) {
        mPrefs = context.getSharedPreferences(Define.PREF_FILE_NAME, Context.MODE_PRIVATE);
    }


    @Override
    public void saveSignPosition(SignEntity signEntity) {
        Gson gson = new Gson();
        if (signEntity == null) {
            mPrefs.edit().putString(PREF_KEY_SIGN_INFO, "").apply();
        }else {
            mPrefs.edit().putString(PREF_KEY_SIGN_INFO, gson.toJson(signEntity)).apply();
        }
    }

    @Override
    public SignEntity getSignPositionInfo() {
        String signJs = mPrefs.getString(PREF_KEY_SIGN_INFO, "");
        if (TextUtils.isEmpty(signJs)) {
            return null;
        }
        return new Gson().fromJson(signJs, SignEntity.class);
    }
}
