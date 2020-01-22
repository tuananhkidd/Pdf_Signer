package com.beetech.card_detect.ui.pdfview;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.lifecycle.ViewModelProviders;

import com.beetech.card_detect.BuildConfig;
import com.beetech.card_detect.R;
import com.beetech.card_detect.base.BaseFragment;
import com.beetech.card_detect.databinding.SignedPdfViewerFragmentBinding;

import java.util.HashMap;
import java.util.Map;

public class SignedPdfViewerFragment extends BaseFragment<SignedPdfViewerFragmentBinding> {

    private SignedPdfViewerViewModel mViewModel;
    private String PDF_VIEWER_URL = "http://drive.google.com/viewerng/viewer?embedded=true&url=";
    private String endPoint = "http://27.72.30.41:2021";

    @Override
    protected int getLayoutId() {
        return R.layout.signed_pdf_viewer_fragment;
    }

    @Override
    public void backFromAddFragment() {

    }

    @Override
    public boolean backPressed() {
        HashMap<String,Boolean> data = new HashMap<>();
        data.put("finish",true);
        getViewController().backFromAddFragment(data);
        return false;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initView() {
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(SignedPdfViewerViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null) {
//            try {
            String filePath = bundle.getString("pdf_sign_file");
            binding.pdfView.getSettings().setJavaScriptEnabled(true);
            binding.pdfView.getSettings().setBuiltInZoomControls(true);
            binding.pdfView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    binding.loading.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    binding.loading.setVisibility(View.GONE);
                }
            });
            binding.pdfView.loadUrl(PDF_VIEWER_URL + endPoint + filePath);
////                binding.pdfView.setPagePresentationMode(PDFViewCtrl.PagePresentationMode.SINGLE_VERT);
////                binding.pdfView.openPDFUri(Uri.fromFile(new File(filePath)), "");
//            } catch (PDFNetException e) {
//                e.printStackTrace();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void initData() {

    }


}
