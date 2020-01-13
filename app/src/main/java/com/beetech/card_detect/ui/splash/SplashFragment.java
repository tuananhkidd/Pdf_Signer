package com.beetech.card_detect.ui.splash;

import android.os.Handler;

import androidx.lifecycle.ViewModelProviders;

import com.beetech.card_detect.R;
import com.beetech.card_detect.base.BaseFragment;
import com.beetech.card_detect.databinding.SplashFragmentBinding;
import com.beetech.card_detect.ui.home.HomeFragment;


public class SplashFragment extends BaseFragment<SplashFragmentBinding> {

    private SplashViewModel mViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.splash_fragment;
    }

    @Override
    public void backFromAddFragment() {

    }

    @Override
    public boolean backPressed() {
        return false;
    }

    @Override
    public void initView() {
        mViewModel = ViewModelProviders.of(this,viewModelFactory).get(SplashViewModel.class);
    }

    @Override
    public void initData() {
        new Handler().postDelayed(()->{
            getViewController().replaceFragment(HomeFragment.class,null);
        },2000);
    }

}
