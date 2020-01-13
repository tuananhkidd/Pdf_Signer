package com.beetech.card_detect.di.module;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.beetech.card_detect.di.ViewModelFactory;
import com.beetech.card_detect.ui.home.HomeViewModel;
import com.beetech.card_detect.ui.main.MainViewModel;
import com.beetech.card_detect.ui.pdfview.PdfViewerViewModel;
import com.beetech.card_detect.ui.sign.GetSignatureViewModel;
import com.beetech.card_detect.ui.splash.SplashViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {

    //bind ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel.class)
    abstract ViewModel bindSplashViewModel(SplashViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel.class)
    abstract ViewModel bindHomeViewModel(HomeViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    abstract ViewModel bindMainViewModel(MainViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PdfViewerViewModel.class)
    abstract ViewModel bindPdfViewerViewModel(PdfViewerViewModel viewModel);


    @Binds
    @IntoMap
    @ViewModelKey(GetSignatureViewModel.class)
    abstract ViewModel bindGetSignatureViewModel(GetSignatureViewModel viewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);
}
