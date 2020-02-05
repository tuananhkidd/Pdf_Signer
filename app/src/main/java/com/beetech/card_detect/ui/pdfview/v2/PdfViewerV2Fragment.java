package com.beetech.card_detect.ui.pdfview.v2;

import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.beetech.card_detect.R;
import com.beetech.card_detect.base.BaseFragment;
import com.beetech.card_detect.databinding.PdfViewerV1FragmentBinding;
import com.beetech.card_detect.databinding.PdfViewerV2FragmentBinding;
import com.beetech.card_detect.tool.PDFTools;
import com.beetech.card_detect.ui.pdfview.SignedPdfViewerFragment;
import com.beetech.card_detect.ui.sign.GetSignatureFragment;
import com.beetech.card_detect.utils.Define;
import com.beetech.card_detect.utils.FileUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.scroll.ScrollHandle;

import java.io.File;
import java.util.HashMap;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

public class PdfViewerV2Fragment extends BaseFragment<PdfViewerV2FragmentBinding> {

    private PdfViewerV2ViewModel mViewModel;
    public static final int REQUEST_SELECT_IMAGE_IN_ALBUM = 1991;
    private String pdfPath;
    private String signPath;

    @Override
    protected int getLayoutId() {
        return R.layout.pdf_viewer_v2_fragment;
    }

    @Override
    public void backFromAddFragment() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("sign")) {
            String path = bundle.getString("sign");
            String mode = bundle.getString("sign");
            mViewModel.setSignPath(path);

            binding.btnDone.setVisibility(View.VISIBLE);
            bundle.remove("sign");
            setArguments(null);

            mViewModel.processSignatureAction(getContext());
        }

        if (bundle != null && bundle.containsKey("finish")) {
            getViewController().backFromAddFragment(null);
            bundle.remove("finish");
            setArguments(null);
        }
    }

    @Override
    public boolean backPressed() {
        getViewController().backFromAddFragment(null);
        return false;
    }

    @Override
    public void initView() {
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(PdfViewerV2ViewModel.class);
        Bundle bundle = getArguments();
        if (bundle != null) {
            pdfPath = bundle.getString("pdf");
            mViewModel.setPdfPath(pdfPath);
            loadFilePdf(new File(pdfPath));
        }
    }

    private void loadFilePdf(File file) {
        binding.pdfView.fromFile(file)
                .scrollHandle(new DefaultScrollHandle(getContext()))
                .spacing(10)
                .onDraw((canvas, pageWidth, pageHeight, displayedPage) -> {
                    if (displayedPage == 8) {
//                        if(!TextUtils.isEmpty(this.signPath)){
//                            Paint paint = new Paint();
//                            Bitmap bitmap = BitmapFactory.decodeFile(signPath);
//                            canvas.drawBitmap(bitmap,100,100,paint);
//                        }
                    }
                })
                .load();

        binding.pdfView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void initData() {
        mViewModel.getFile().observe(getViewLifecycleOwner(), this::handleObjectResponse);
        mViewModel.getSignPdfWithUrl().observe(getViewLifecycleOwner(), this::handleObjectResponse);
    }

    @Override
    protected <U> void getObjectResponse(U data) {
        if (data instanceof File) {
            loadFilePdf((File) data);
        }else if(data instanceof String){
            HashMap<String, String> bundle = new HashMap<>();
            bundle.put("pdf_sign_file", (String) data);
            getViewController().addFragment(SignedPdfViewerFragment.class, bundle);
        }
    }

    @Override
    public void initListener() {
        super.initListener();
        binding.btnBack.setOnClickListener(view -> {
            if (avoidDuplicateClick()) {
                return;
            }
            getViewController().backFromAddFragment(null);
        });

        binding.btnSign.setOnClickListener(view -> {
            if (avoidDuplicateClick()) {
                return;
            }
            showPopChooseSignOption();
        });

        binding.btnDone.setOnClickListener(view -> {
            if (avoidDuplicateClick()) {
                return;
            }
            mViewModel.signPdfFile();
        });
    }

    private void showPopChooseSignOption() {
        PopupMenu popupMenu = new PopupMenu(getContext(), binding.btnSign);

        popupMenu.getMenuInflater().inflate(R.menu.menu_sign_option, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.item_select_image: {
                    selectImageInAlbum();
                    break;
                }
                case R.id.item_draw: {
                    HashMap<String, String> data = new HashMap<>();
                    data.put(Define.SIGN_MODE.SIGN_MODE, Define.SIGN_MODE.DRAW);
                    getViewController().addFragment(GetSignatureFragment.class, data);
                    break;
                }
                case R.id.item_hand_writing: {
                    HashMap<String, String> data = new HashMap<>();
                    data.put(Define.SIGN_MODE.SIGN_MODE, Define.SIGN_MODE.HANDWRITING);
                    getViewController().addFragment(GetSignatureFragment.class, data);
                    break;
                }
            }
            return true;
        });

        popupMenu.show();
    }

    private void selectImageInAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
        if (getActivity() != null) {
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    try {
                        if (getActivity() != null) {
                            String signPath = FileUtil.getPathFromUri(getContext(), uri);
                            if (TextUtils.isEmpty(signPath)) {
                                Toast.makeText(getContext(), "Có lỗi xảy ra.Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            this.signPath = signPath;
                            mViewModel.setSignPath(signPath);
                            mViewModel.processSignatureAction(getContext());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
