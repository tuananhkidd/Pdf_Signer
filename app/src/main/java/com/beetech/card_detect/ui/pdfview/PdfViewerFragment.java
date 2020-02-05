package com.beetech.card_detect.ui.pdfview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
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
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
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
            String mode = bundle.getString("mode");
            int width = bundle.getInt("width");
            int height = bundle.getInt("height");
            changeImageScaleType(mode,width,height);
            mViewModel.setSignPath(path);

            setDragListener(mode);
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

        if (bundle != null && bundle.containsKey("finish")) {
            getViewController().backFromAddFragment(null);
            bundle.remove("finish");
            setArguments(null);
        }
    }

    private void changeImageScaleType(String mode,int width,int height) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.imgSign.getLayoutParams();
        if (Define.SIGN_MODE.HANDWRITING.equals(mode)) {
            binding.imgSign.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            layoutParams.width = DeviceUtil.widthScreenPixel(getContext()) / 2;
        } else {
            binding.imgSign.setScaleType(ImageView.ScaleType.CENTER_CROP);
            layoutParams.width = DeviceUtil.convertDpToPx(getContext(), 100);
        }
        layoutParams.height = layoutParams.width * height / width;
        if (Define.SIGN_MODE.DRAW.equals(mode)) {
            binding.imgSign.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        binding.imgSign.setLayoutParams(layoutParams);
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


    }

    private boolean validatePositionSignature(View view) {
        Rect rect = new Rect();
        view.getHitRect(rect);

        float yPosition = binding.pdfView.getCurrentYOffset();

        if (view.getY() < yPosition) {
            return false;
        }

        if (yPosition > 0 && (view.getY() + rect.height()) >
                yPosition + binding.pdfView.getPageSize(binding.pdfView.getCurrentPage()).getHeight() * binding.pdfView.getZoom()) {
            return false;
        }
        return true;
    }

    private void setDragListener(String mode) {
        OnDragTouchListener onDragTouchListener = new OnDragTouchListener(binding.imgSign, binding.container, new OnDragTouchListener.OnDragActionListener() {
            @Override
            public void onDragStart(View view) {

            }

            @Override
            public void onDragEnd(View view, boolean isClickDetected) {
                if (!validatePositionSignature(view)) {
                    moveImageToRoot();
                } else {
                    getPositionSignatureV2(view);
                }
            }
        }, mode);
        onDragTouchListener.setOnGestureControl(isBiggerScale -> onDragTouchListener.scaleView(isBiggerScale));
        binding.imgSign.setOnTouchListener(onDragTouchListener);
    }

    private void getPositionSignatureV2(View view) {
        Rect rect = new Rect();
        view.getHitRect(rect);
        int pageHeight = (int) binding.pdfView.getPageSize(binding.pdfView.getCurrentPage()).getHeight();
        int pageWidth = (int) binding.pdfView.getPageSize(binding.pdfView.getCurrentPage()).getWidth();
        int currentPage = binding.pdfView.getCurrentPage();

        float xPos = view.getX() - binding.pdfView.getCurrentXOffset();
        float yPos = pageHeight * binding.pdfView.getZoom() - (view.getY() - binding.pdfView.getCurrentYOffset() + rect.height());

        xPos = (xPos / binding.pdfView.getZoom()) - 2;
        mViewModel.setPositionSign(xPos / pageWidth, (double) yPos / (double) (pageHeight * binding.pdfView.getZoom()),
                (xPos + rect.width() / binding.pdfView.getZoom()) / pageWidth, (double) (yPos + rect.height()) / (double) (pageHeight * binding.pdfView.getZoom()), currentPage);
    }

    private void getPositionSignature(View view) {
        Rect rect = new Rect();
        view.getHitRect(rect);
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
                checkHeadPosition = getEndPagePosition(checkHeadPosition, fistPage);
                yPosition = checkEndPosition - (int) view.getY() - rect.height();
                Log.v("ahihi", "TH3 : " + checkHeadPosition);
            }


            Log.v("ahihi", "pos => " + yPosition);
            float xDelta = (view.getX() + binding.pdfView.getCurrentXOffset());
            mViewModel.setPositionSign(xDelta / pageWidth, (double) yPosition / (double) pageHeight,
                    (xDelta + rect.width()) / pageWidth, (double) (yPosition + rect.height()) / (double) pageHeight, fistPage);
        }
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
        mViewModel.getSignPdfWithUrl().observe(getViewLifecycleOwner(), this::handleObjectResponse);
    }

    @Override
    protected <U> void getObjectResponse(U data) {
        if (data instanceof File) {
//            loadFilePdf((File) data);
//            binding.btnDone.setVisibility(View.VISIBLE);
        } else if (data instanceof String) {
            HashMap<String, String> bundle = new HashMap<>();
            bundle.put("pdf_sign_file", (String) data);
            getViewController().addFragment(SignedPdfViewerFragment.class, bundle);
        } else if (data instanceof ResponseBody) {
//            HashMap<String, String> bundle = new HashMap<>();
//            try {
//                bundle.put("pdf_sign_file",  ((ResponseBody) data).string());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            getViewController().replaceFragment(SignedPdfViewerFragment.class, bundle);


//            String path = FileUtil.saveFile((ResponseBody) data);
//            if (TextUtils.isEmpty(path)) {
//                Toast.makeText(getContext(), "Có lỗi xảy ra.Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
//            } else {
////                loadFilePdf(new File(path));
////                binding.btnDone.setVisibility(View.GONE);
//                getViewController().backFromAddFragment(null);
//                HashMap<String, String> bundle = new HashMap<>();
//                bundle.put("pdf_sign_file", path);
//                ViewerConfig viewerConfig = new ViewerConfig.Builder().documentEditingEnabled(false)
//                        .autoHideToolbarEnabled(true)
//                        .multiTabEnabled(false)
//                        .useSupportActionBar(false)
//                        .showDocumentSettingsOption(false)
//                        .rightToLeftModeEnabled(true)
//                        .showTopToolbar(false)
//                        .build();
//                DocumentActivity.openDocument(getContext(), Uri.fromFile(new File(path)), viewerConfig);
//            }
        }
    }

    private void loadFilePdf(File file) {
        binding.pdfView.fromFile(file)
                .swipeHorizontal(true)
                .pageFling(true)
                .pageSnap(true)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

                    }
                })
                .load();

        binding.pdfView.setBackgroundColor(Color.LTGRAY);
        binding.pdfView.setMaxZoom(2.0f);
    }

    private int pageFromView(float y) {
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

//    public PointF convertDocToView(float x, float y, int page) throws IOException {
//        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(new File(pdfPath), ParcelFileDescriptor.MODE_READ_ONLY);
//        PdfiumCore core = new PdfiumCore(getActivity());
//        PdfDocument doc = core.newDocument(pfd);
//        int viewPage = binding.pdfView.getCurrentPage();
//        float zoom = binding.pdfView.getZoom();
//
//        float viewWidth = binding.pdfView.getPageSize(page).getWidth();
//        float viewHeight = binding.pdfView.getPageSize(page).getHeight();
//
//        if (page > viewPage) {
//            for (int i = viewPage; i < page; i++) {
//                core.openPage(doc, i);
//                float spacing = binding.pdfView.getSpacingPx() / viewHeight * core.getPageHeightPoint(doc, i);
//                y += core.getPageHeightPoint(doc, i) + spacing;
//            }
//        } else if (page < viewPage) {
//            for (int i = viewPage; i > page; i--) {
//                core.openPage(doc, i);
//                float spacing = binding.pdfView.getSpacingPx() / viewHeight * core.getPageHeightPoint(doc, i);
//                y -= core.getPageHeightPoint(doc, i) + spacing;
//            }
//        }
//
//        core.openPage(doc, page);
//        float pageWidth = core.getPageWidthPoint(doc, page);
//        float pageHeight = core.getPageHeightPoint(doc, page);
//
//        float midX = x * viewWidth / pageWidth;
//        float midY = y * viewHeight / pageHeight;
//
//        float resultX = midX * zoom;
//        float resultY = midY * zoom;
//
//        return new PointF(resultX, resultY);
//    }

    @Override
    public void initListener() {
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
//            Rect rect = new Rect();
//            binding.imgSign.getHitRect(rect);
//            int fistPage = pageFromView(binding.imgSign.getY());
//            int secondPage = pageFromView(binding.imgSign.getY() + rect.height());

            if (!validatePositionSignature(binding.imgSign)) {
                Toast.makeText(getContext(), "Invalid Signature Position!", Toast.LENGTH_SHORT).show();
            } else {
                getPositionSignatureV2(binding.imgSign);
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
                            changeImageScaleType(Define.SIGN_MODE.PHOTO,1,1);
                            RequestOptions requestOptions = new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true);
                            setDragListener(Define.SIGN_MODE.PHOTO);
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
