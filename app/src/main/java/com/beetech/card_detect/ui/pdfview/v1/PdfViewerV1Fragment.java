package com.beetech.card_detect.ui.pdfview.v1;

import androidx.lifecycle.ViewModelProviders;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beetech.card_detect.R;
import com.beetech.card_detect.adapter.PdfAdapter;
import com.beetech.card_detect.base.BaseFragment;
import com.beetech.card_detect.databinding.PdfViewerV1FragmentBinding;
import com.beetech.card_detect.entity.PdfFile;
import com.bumptech.glide.Glide;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PdfViewerV1Fragment extends BaseFragment<PdfViewerV1FragmentBinding> {

    private PdfViewerV1ViewModel mViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.pdf_viewer_v1_fragment;
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
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(PdfViewerV1ViewModel.class);
        PDFBoxResourceLoader.init(getContext());

        Bundle bundle = getArguments();
        if (bundle != null) {
            String pdfPath = bundle.getString("pdf");
            try {
                InputStream inputStream = new FileInputStream(pdfPath);
                PDDocument document = PDDocument.load(inputStream);
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                PdfAdapter pdfAdapter = new PdfAdapter(getContext(), document.getNumberOfPages());
                binding.rcvPdf.setAdapter(pdfAdapter);
                LinearSnapHelper snapHelper = new LinearSnapHelper();
                snapHelper.attachToRecyclerView(binding.rcvPdf);
                pdfAdapter.setOnRenderPdfListener(position -> {
                    Callable<Bitmap> callable = () -> pdfRenderer.renderImage(position, 1, Bitmap.Config.RGB_565);
                    Single.fromCallable(callable)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    image -> {
                                        pdfAdapter.getLsPdf().set(position, image);
                                        pdfAdapter.notifyItemChanged(position);
                                    },
                                    throwable -> {

                                    }
                            );
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void initData() {

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        }
    }
}
