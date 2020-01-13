package com.beetech.card_detect.di;


import com.beetech.card_detect.entity.SignEntity;

import javax.inject.Singleton;

@Singleton
public interface RxPreference {
    void saveSignPosition(SignEntity signEntity);

    SignEntity getSignPositionInfo();
}
