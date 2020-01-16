package com.beetech.card_detect.ui.pdfview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
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
import com.beetech.card_detect.custom.gesture.OnDragTouchListener;
import com.beetech.card_detect.databinding.PdfViewerFragmentBinding;
import com.beetech.card_detect.ui.sign.GetSignatureFragment;
import com.beetech.card_detect.utils.Define;
import com.beetech.card_detect.utils.DeviceUtil;
import com.beetech.card_detect.utils.FileUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.pdftron.pdf.config.ViewerConfig;
import com.pdftron.pdf.controls.DocumentActivity;

import java.io.File;
import java.util.HashMap;

import okhttp3.ResponseBody;

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
            binding.imgSign.setVisibility(View.VISIBLE);
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

//        binding.imgSign.animate().translationX(0).translationY(0).setDuration(300).start();
        final OnDragTouchListener onDragTouchListener = new OnDragTouchListener(binding.imgSign, binding.container, new OnDragTouchListener.OnDragActionListener() {
            @Override
            public void onDragStart(View view) {

            }

            @Override
            public void onDragEnd(View view, boolean isClickDetected) {
                Rect rect = new Rect();
                view.getHitRect(rect);

                //check position beetween two page

                int fistPage = pageFromView(view.getY());
                int secondPage = pageFromView(view.getY() + rect.height());
                int currentPage = binding.pdfView.getCurrentPage();
                int pageHeight = (int) binding.pdfView.getPageSize(binding.pdfView.getCurrentPage()).getHeight();
                int pageWidth = (int) binding.pdfView.getPageSize(binding.pdfView.getCurrentPage()).getWidth();

//                Log.v("ahuhu", "page : " + binding.pdfView.getCurrentPage() + " , 1: " + fistPage + " , 2:" + secondPage + "  ,mid:" + middlePage);
//                Log.v("ahuhu", "coordinate : " + view.getY() + "  " + rect.height() + "   " + (view.getY() + rect.height() / 2));
                if (fistPage != secondPage) {
                    binding.pdfView.jumpTo(currentPage, true);
                    moveImageToRoot();
                } else {
                    int checkHeadPosition = (int) view.getY();
                    int checkEndPosition = (int) view.getY() + rect.height();
                    int yPosition = 0;
                    if (fistPage < currentPage) {
                        checkEndPosition = getEndPagePosition(checkEndPosition, fistPage);
                        yPosition = checkEndPosition - (int) view.getY() - rect.height();
//                        Log.v("ahihi", "TH1 : " + checkEndPosition);
                    } else if (fistPage > currentPage) {
                        int pageCurrentHeight = (int) binding.pdfView.getPageSize(fistPage).getHeight();
                        checkHeadPosition = getStartPagePosition(checkHeadPosition, fistPage);
                        yPosition = pageCurrentHeight - ((int) view.getY() + rect.height() - checkHeadPosition);
//                        Log.v("ahihi", "TH2 : " + checkHeadPosition);

                    } else {
                        checkEndPosition = getEndPagePosition(checkEndPosition, fistPage);
                        yPosition = checkEndPosition - (int) view.getY() - rect.height();
//                        Log.v("ahihi", "TH3 : " + checkEndPosition);
                    }


                    Log.v("ahihi", "pos => " + yPosition);
                    mViewModel.setPositionSign(view.getX() / pageWidth, (double) yPosition / (double) pageHeight,
                            (view.getX() + rect.width()) / pageWidth, (double) (yPosition + rect.height()) / (double) pageHeight, fistPage);
                }


            }
        });
        onDragTouchListener.setOnGestureControl(isBiggerScale -> onDragTouchListener.scaleView(isBiggerScale));
        binding.imgSign.setOnTouchListener(onDragTouchListener);
    }

    private int getStartPagePosition(int checkHeadPosition, int page) {
        while (true) {
            int checkStartPage = pageFromView(checkHeadPosition);
            Log.v("ahihi", "checkStartPage : " + checkStartPage);
            if (checkStartPage != page) {
                break;
            }
            checkHeadPosition--;
        }

        return checkHeadPosition;
    }

    private int getEndPagePosition(int checkEndPosition, int page) {
        while (true) {
            int checkEndPage = pageFromView(checkEndPosition);
            Log.v("ahihi", "checkEndPage : " + checkEndPage);
            if (checkEndPage != page) {
                break;
            }
            checkEndPosition++;
        }

        return checkEndPosition;
    }

    private void moveImageToRoot() {
        binding.imgSign.animate()
                .translationY(0)
                .translationX(0).setDuration(300)
                .start();
    }

    @Override
    public void initData() {
        mViewModel.getFile().observe(getViewLifecycleOwner(), this::handleObjectResponse);
        mViewModel.getSaveFile().observe(getViewLifecycleOwner(), this::handleObjectResponse);
        mViewModel.getSignPdf().observe(getViewLifecycleOwner(), this::handleObjectResponse);
    }

    @Override
    protected <U> void getObjectResponse(U data) {
        if (data instanceof File) {
//            loadFilePdf((File) data);
//            binding.btnDone.setVisibility(View.VISIBLE);
        } else if (data instanceof String) {
            Toast.makeText(getContext(), "Lưu thành công !", Toast.LENGTH_SHORT).show();
            getViewController().backFromAddFragment(null);
        } else if (data instanceof ResponseBody) {
            String path = FileUtil.saveFile((ResponseBody) data);
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(getContext(), "Có lỗi xảy ra.Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            } else {
//                loadFilePdf(new File(path));
//                binding.btnDone.setVisibility(View.GONE);
                getViewController().backFromAddFragment(null);
                HashMap<String, String> bundle = new HashMap<>();
                bundle.put("pdf_sign_file", path);
                ViewerConfig viewerConfig = new ViewerConfig.Builder().documentEditingEnabled(false)
                        .autoHideToolbarEnabled(true)
                        .multiTabEnabled(false)
                        .useSupportActionBar(false)
                        .showDocumentSettingsOption(false)
                        .rightToLeftModeEnabled(true)
                        .showTopToolbar(false)
                        .build();
                DocumentActivity.openDocument(getContext(), Uri.fromFile(new File(path)), viewerConfig);
            }
        }
    }

    private boolean isLoaded = false;

    private void loadFilePdf(File file) {
        binding.pdfView.fromFile(file)
                .spacing(2)
//                .onPageChange(new OnPageChangeListener() {
//                    @Override
//                    public void onPageChanged(int page, int pageCount) {
//                        if (isLoaded) {
//                            binding.pdfView.jumpTo(page, true);
//                        }
//                    }
//                })
////                .defaultPage(0)
////                .pageFitPolicy(FitPolicy.BOTH)
////                .fitEachPage(true)
                .swipeHorizontal(false)
                .load();

        binding.pdfView.setBackgroundColor(Color.LTGRAY);
    }

    public int pageFromView(float y) {
        float offsetY = binding.pdfView.getCurrentYOffset();
        float zoom = binding.pdfView.getZoom();


        float zoomY = (y - offsetY) / zoom;
        float spacing = binding.pdfView.getSpacingPx();

        int page = 0;
        for (int i = 0; i < binding.pdfView.getPageCount(); i++) {

            if (zoomY < binding.pdfView.getPageSize(i).getHeight()) {
                page = i;
                break;
            } else {
                zoomY -= binding.pdfView.getPageSize(i).getHeight() + spacing;
            }
        }
        return page;
    }

    @Override
    public void initListener() {
        binding.btnBack.setOnClickListener(view -> getViewController().backFromAddFragment(null));

        binding.btnSign.setOnClickListener(view -> showPopChooseSignOption());

        binding.btnDone.setOnClickListener(view -> {
            Rect rect = new Rect();
            binding.imgSign.getHitRect(rect);
            int fistPage = pageFromView(binding.imgSign.getY());
            int secondPage = pageFromView(binding.imgSign.getY() + rect.height());

            if (fistPage != secondPage) {
                Toast.makeText(getContext(), "Invalid Signature Position!", Toast.LENGTH_SHORT).show();
            } else {
                mViewModel.signPdfFile(binding.pdfView.getCurrentPage());
            }
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
//                            adjustImageSize(Define.SIGN_MODE.PHOTO);
                            RequestOptions requestOptions = new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true);
                            String signPath = FileUtil.getPathFromUri(getContext(), uri);
                            if (TextUtils.isEmpty(signPath)) {
                                Toast.makeText(getContext(), "Có lỗi xảy ra.Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mViewModel.setSignPath(signPath);
                            Glide.with(getActivity())
                                    .load(uri)
                                    .apply(requestOptions)
                                    .into(binding.imgSign);
                            binding.imgSign.setVisibility(View.VISIBLE);
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
