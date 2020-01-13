package com.beetech.card_detect.ui.sign;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.beetech.card_detect.R;
import com.beetech.card_detect.base.BaseFragment;
import com.beetech.card_detect.databinding.GetSignatureFragmentBinding;
import com.beetech.card_detect.utils.Define;
import com.beetech.card_detect.utils.FileUtil;
import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.util.HashMap;

public class GetSignatureFragment extends BaseFragment<GetSignatureFragmentBinding> {

    private GetSignatureViewModel mViewModel;
    String signMode;

    @Override
    protected int getLayoutId() {
        return R.layout.get_signature_fragment;
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
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(GetSignatureViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null) {
            signMode = bundle.getString(Define.SIGN_MODE.SIGN_MODE);
            if (Define.SIGN_MODE.DRAW.equals(signMode)) {
                binding.edtSign.setVisibility(View.GONE);
                binding.signaturePad.setVisibility(View.VISIBLE);
            } else if (Define.SIGN_MODE.HANDWRITING.equals(signMode)) {
                binding.edtSign.setVisibility(View.VISIBLE);
                binding.signaturePad.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void initData() {
        binding.signaturePad.setPenColor(Color.RED);
        binding.signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
            }

            @Override
            public void onSigned() {
                enableAction(true);
            }

            @Override
            public void onClear() {
                enableAction(false);
            }
        });

        binding.edtSign.addTextChangedListener(textWatcher);

        binding.clearButton.setOnClickListener(v -> binding.signaturePad.clear());

        binding.saveButton.setOnClickListener(v -> {
            Bitmap signatureBitmap;
            if (Define.SIGN_MODE.DRAW.equals(signMode)) {
                signatureBitmap = binding.signaturePad.getTransparentSignatureBitmap();
            }else {
                signatureBitmap = Bitmap.createBitmap(150,150, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(signatureBitmap);
                Paint paint = new Paint();
                paint.setColor(Color.TRANSPARENT);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPaint(paint);

                paint.setColor(Color.BLACK);
                paint.setTextSize(20);
                canvas.drawText(binding.edtSign.getText().toString(),75,75,paint);
                canvas.setBitmap(signatureBitmap);
            }
            File signatureFile = FileUtil.saveSignature(getContext(), signatureBitmap);

            HashMap<String, String> data = new HashMap<>();
            data.put("sign", signatureFile.getAbsolutePath());
            getViewController().backFromAddFragment(data);
        });
    }

    private void enableAction(boolean enable) {
        binding.clearButton.setEnabled(enable);
        binding.saveButton.setEnabled(enable);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if ((start + count) == 0) {
                enableAction(false);
            } else if (before == 0) {
                enableAction(true);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
}
