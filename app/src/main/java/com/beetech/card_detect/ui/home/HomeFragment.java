package com.beetech.card_detect.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.beetech.card_detect.R;
import com.beetech.card_detect.adapter.SearchAdapter;
import com.beetech.card_detect.base.BaseFragment;
import com.beetech.card_detect.databinding.HomeFragmentBinding;
import com.beetech.card_detect.ui.pdfview.PdfViewerFragment;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.HashMap;
import java.util.regex.Pattern;


public class HomeFragment extends BaseFragment<HomeFragmentBinding> {

    private HomeViewModel mViewModel;
    private SearchAdapter searchAdapter;

    public static final int REQUEST_PERMISSION_STORAGE_CODE = 1111;
    public static final int REQUEST_CODE_PICK_PDF_FILE = 1234;

    @Override
    protected int getLayoutId() {
        return R.layout.home_fragment;
    }

    @Override
    public void backFromAddFragment() {

    }

    @Override
    public boolean backPressed() {
        return true;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel.class);

        binding.btnSelectFile.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE_CODE);
            } else {
                openFolderDevice();
            }
        });

    }

    private void openFolderDevice() {
        new MaterialFilePicker()
                .withActivity(getActivity())
                .withRequestCode(REQUEST_CODE_PICK_PDF_FILE)
                .withFilter(Pattern.compile(".*\\.pdf$")) // Filtering files and directories by file name using regexp
                .withFilterDirectories(false) // Set directories filterable (false by default)
                .withTitle("Select Pdf File")
                .start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_STORAGE_CODE: {
                boolean hasPermission = true;
                for (int grant : grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        hasPermission = false;
                        break;
                    }
                }

                if (hasPermission) {
                    openFolderDevice();
                } else {
                    Toast.makeText(getContext(), "Permission Storage Denied", Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICK_PDF_FILE: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                        Log.v("ahuhu", "pdf :" + filePath);
                        HashMap<String, String> bundle = new HashMap<>();
                        bundle.put("pdf", filePath);
                        getViewController().addFragment(PdfViewerFragment.class, bundle);
                    }

                }
                break;
            }
        }
    }
}
