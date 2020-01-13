package com.beetech.card_detect.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.beetech.card_detect.R;
import com.beetech.card_detect.databinding.ItemSearchBinding;
import com.beetech.card_detect.entity.SearchResponse;

import java.util.ArrayList;
import java.util.List;

public class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.DemoViewHoler> {
    ItemSearchBinding binding;
    private List<SearchResponse> searchResponses;

    public DemoAdapter() {
        this.searchResponses = new ArrayList<>();
    }

    @NonNull
    @Override
    public DemoViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_search, parent, false);
        return new DemoViewHoler(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DemoViewHoler holder, int position) {
        holder.bind(searchResponses.get(position));
    }

    public void setList(List<SearchResponse> searchResponses) {
        this.searchResponses.clear();
        this.searchResponses.addAll(searchResponses);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return searchResponses.size();
    }

    class DemoViewHoler extends RecyclerView.ViewHolder {

        public DemoViewHoler(@NonNull ItemSearchBinding itemSearchBinding) {
            super(itemSearchBinding.getRoot());
        }

        public void bind(SearchResponse searchResponse) {
            binding.tvPrice.setText(searchResponse.getPrice());
            binding.tvName.setText(searchResponse.getName());
        }
    }
}
