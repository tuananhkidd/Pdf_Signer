package com.beetech.card_detect.ui.pdfview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.beetech.card_detect.R;
import com.beetech.card_detect.base.BaseFragment;
import com.beetech.card_detect.custom.OnDragTouchListener;
import com.beetech.card_detect.databinding.PdfViewerFragmentBinding;
import com.beetech.card_detect.ui.sign.GetSignatureFragment;
import com.beetech.card_detect.utils.Define;
import com.beetech.card_detect.utils.FileUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;
import java.util.HashMap;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

public class PdfViewerFragment extends BaseFragment<PdfViewerFragmentBinding> {

    private PdfViewerViewModel mViewModel;
    private String pdfPath;
    public static final int REQUEST_SELECT_IMAGE_IN_ALBUM = 1996;

    @Override
    protected int getLayoutId() {
        return R.layout.pdf_viewer_fragment;
    }

    @Override
    public void backFromAddFragment() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("sign")) {
            String path = bundle.getString("sign");
            mViewModel.setSignPath(path);

            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true);
            Glide.with(getContext())
                    .load(path)
                    .apply(requestOptions)
                    .into(binding.imgSign);
            binding.layoutSign.setVisibility(View.VISIBLE);
            binding.btnDone.setVisibility(View.VISIBLE);
            bundle.remove("sign");
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
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(PdfViewerViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null) {
            pdfPath = bundle.getString("pdf");
            mViewModel.setPdfPath(pdfPath);

            loadFilePdf(new File(pdfPath));
        }

        binding.layoutSign.setOnTouchListener(new OnDragTouchListener(binding.layoutSign, binding.pdfView, new OnDragTouchListener.OnDragActionListener() {
            @Override
            public void onDragStart(View view) {

            }

            @Override
            public void onDragEnd(View view, boolean isClickDetected) {
                int pageHeight = (int) binding.pdfView.getPageSize(binding.pdfView.getCurrentPage()).getHeight();
                Log.v("ahuhu", "coordinate : " + view.getX() + "  " + view.getY());
                mViewModel.setxPosition((int) view.getX()); //margin
                mViewModel.setyPosition(pageHeight - (int) view.getY() - 80); // sub height of sign image and margin
            }
        }));

    }

    @Override
    public void initData() {
        mViewModel.getFile().observe(getViewLifecycleOwner(), this::handleObjectResponse);
        mViewModel.getSaveFile().observe(getViewLifecycleOwner(), this::handleObjectResponse);
    }

    @Override
    protected <U> void getObjectResponse(U data) {
        if (data instanceof File) {
//            loadFilePdf((File) data);
//            binding.btnDone.setVisibility(View.VISIBLE);
        } else if (data instanceof String) {
            Toast.makeText(getContext(), "Lưu thành công !", Toast.LENGTH_SHORT).show();
            getViewController().backFromAddFragment(null);
        }

    }

    private void loadFilePdf(File file) {
        binding.pdfView.fromFile(file)
                .autoSpacing(true)
                .pageFling(true)
                .spacing(10)
                .defaultPage(0)
                .pageFitPolicy(FitPolicy.BOTH)
                .fitEachPage(true)
                .pageFling(true)
                .load();

    }

    @Override
    public void initListener() {
        binding.btnBack.setOnClickListener(view -> getViewController().backFromAddFragment(null));

        binding.btnSign.setOnClickListener(view -> showPopChooseSignOption());

        binding.btnDone.setOnClickListener(view -> mViewModel.processSignatureAction(getContext(), binding.pdfView.getCurrentPage()));

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
//                            DialogUtil.getInstance(getContext()).show();
//                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
//                            String path = FileUtil.saveSignature(getActivity(), bitmap).getAbsolutePath();
//                            int lastPage = binding.pdfView.getPageCount();
//                            int xPos = binding.pdfView.getWidth() - 100;
//                            mViewModel.processSignatureAction(getContext(), path, xPos, lastPage);
//                            Log.v("ahuhu", "uri " + path);

                            String signPath = FileUtil.getPathFromUri(getContext(), uri);
                            if (TextUtils.isEmpty(signPath)) {
                                Toast.makeText(getContext(), "Có lỗi xảy ra.Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mViewModel.setSignPath(signPath);
                            Glide.with(getContext())
                                    .load(uri)
                                    .into(binding.imgSign);
                            binding.layoutSign.setVisibility(View.VISIBLE);
                            binding.btnDone.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
