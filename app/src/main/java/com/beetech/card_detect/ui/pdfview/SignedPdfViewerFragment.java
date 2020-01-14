package com.beetech.card_detect.ui.pdfview;


import android.net.Uri;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.beetech.card_detect.R;
import com.beetech.card_detect.base.BaseFragment;
import com.beetech.card_detect.databinding.SignedPdfViewerFragmentBinding;
import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.PDFViewCtrl;

import java.io.File;
import java.io.FileNotFoundException;

public class SignedPdfViewerFragment extends BaseFragment<SignedPdfViewerFragmentBinding> {

    private SignedPdfViewerViewModel mViewModel;
    @Override
    protected int getLayoutId() {
        return R.layout.signed_pdf_viewer_fragment;
    }

    @Override
    public void backFromAddFragment() {

    }

    @Override
    public boolean backPressed() {
        getViewController().backFromAddFragment(null);
        return false;
    }

    @Override
    public void initView() {
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(SignedPdfViewerViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null) {
            try {
                String filePath = bundle.getString("pdf");
                binding.pdfView.setPagePresentationMode(PDFViewCtrl.PagePresentationMode.SINGLE_VERT);
                binding.pdfView.openPDFUri(Uri.fromFile(new File(filePath)), "");
            } catch (PDFNetException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initData() {

    }


}
