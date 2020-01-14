package com.beetech.card_detect.di.module;


import com.beetech.card_detect.ui.home.HomeFragment;
import com.beetech.card_detect.ui.pdfview.PdfViewerFragment;
import com.beetech.card_detect.ui.pdfview.SignedPdfViewerFragment;
import com.beetech.card_detect.ui.sign.GetSignatureFragment;
import com.beetech.card_detect.ui.splash.SplashFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentBindingModule {

    //TODO bind fragment
    @ContributesAndroidInjector
    abstract SplashFragment bindSplashFragment();

    @ContributesAndroidInjector
    abstract HomeFragment bindHomeFragment();

    @ContributesAndroidInjector
    abstract PdfViewerFragment bindPdfViewerFragment();

    @ContributesAndroidInjector
    abstract SignedPdfViewerFragment bindSignedPdfViewerFragment();

    @ContributesAndroidInjector
    abstract GetSignatureFragment bindGetSignatureFragment();
}
