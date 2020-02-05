package com.beetech.card_detect.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.beetech.card_detect.R;
import com.beetech.card_detect.databinding.ItemPdfBinding;
import com.beetech.card_detect.entity.PdfFile;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.PdfViewHolder> {
    private List<Bitmap> lsPdf;
    private Context context;
    OnRenderPdfListener onRenderPdfListener;
    private int pageCount;

    public void setOnRenderPdfListener(OnRenderPdfListener onRenderPdfListener) {
        this.onRenderPdfListener = onRenderPdfListener;
    }

    public PdfAdapter(Context context, int pageCount) {
        this.context = context;
        this.pageCount = pageCount;
        lsPdf = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            lsPdf.add((null));
        }
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new PdfViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.item_pdf, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        Bitmap bitmap = lsPdf.get(position);
        Glide.with(context)
                .load(bitmap)
                .into(holder.getBinding().img);
        if (bitmap == null) {
            holder.getBinding().loading.setVisibility(View.VISIBLE);
            if (onRenderPdfListener != null) {
                onRenderPdfListener.onRender(position);
            }
        } else {
            holder.getBinding().loading.setVisibility(View.GONE);
            Glide.with(context)
                    .load(bitmap)
                    .into(holder.getBinding().img);
        }

    }

    public List<Bitmap> getLsPdf() {
        return lsPdf;
    }

    @Override
    public int getItemCount() {
        return pageCount;
    }

    public class PdfViewHolder extends RecyclerView.ViewHolder {
        ItemPdfBinding binding;

        public PdfViewHolder(ItemPdfBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemPdfBinding getBinding() {
            return binding;
        }
    }

    public interface OnRenderPdfListener {
        void onRender(int position);
    }
}
