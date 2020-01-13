package com.beetech.card_detect.ui.home;

import androidx.lifecycle.MutableLiveData;

import com.beetech.card_detect.base.BaseViewModel;
import com.beetech.card_detect.base.ListLoadmoreReponse;
import com.beetech.card_detect.entity.SearchResponse;
import com.beetech.card_detect.network.repository.Repository;

import javax.inject.Inject;

public class HomeViewModel extends BaseViewModel {
    private Repository repository;

    @Inject
    public HomeViewModel(Repository repository) {
        this.repository = repository;
    }


}
