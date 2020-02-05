package com.beetech.card_detect.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.beetech.card_detect.R;
import com.beetech.card_detect.base.BaseActivity;
import com.beetech.card_detect.databinding.ActivityMainBinding;
import com.beetech.card_detect.ui.home.HomeFragment;
import com.beetech.card_detect.ui.pdfview.PdfViewerFragment;
import com.beetech.card_detect.ui.splash.SplashFragment;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import static com.beetech.card_detect.ui.home.HomeFragment.REQUEST_CODE_PICK_PDF_FILE;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public int getFragmentContainerId() {
        return R.id.flMainContainer;
    }

    @Override
    public void initView() {
        mViewController.addFragment(SplashFragment.class, null);
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewController.getCurrentFragment().onActivityResult(requestCode,resultCode,data);
//        switch (requestCode) {
//
//            case REQUEST_CODE_PICK_PDF_FILE: {
//                if (resultCode == Activity.RESULT_OK) {
//                    if (data != null) {
////                        Uri selectedPdf = data.getData();
////                        HashMap<String, String> bundle = new HashMap<>();
////                        bundle.put("pdf", selectedPdf.toString());
////                        mViewController.addFragment(PdfViewerFragment.class, bundle);
//
//                        String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
//                        Log.v("ahuhu", "pdf :" + filePath);
//
//                        if(mViewController.getCurrentFragment() instanceof HomeFragment){
//                            ((HomeFragment)mViewController.getCurrentFragment()).goToPdfViewer(filePath);
//                        }
//                    }
//
//                }
//                break;
//            }
//        }
    }
}
