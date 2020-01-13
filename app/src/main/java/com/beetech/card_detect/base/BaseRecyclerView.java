package com.beetech.card_detect.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.beetech.card_detect.R;

import java.util.List;

public class BaseRecyclerView<T> extends RelativeLayout implements EndlessLoadingRecyclerViewAdapter.OnLoadingMoreListener {
    private Context context;
    private EndlessLoadingRecyclerViewAdapter mAdapter;
    private OnLoadmoreListener listener;
    RecyclerView rcv;
    SwipeRefreshLayout swipeRefresh;
    RelativeLayout rlNoResult;
    TextView tvNoResult;


    public BaseRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public BaseRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setParams(attrs);
    }

    public BaseRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        setParams(attrs);
    }

    private void setParams(AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BaseRecyclerView, 0, 0);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_base_recyclerview,this,true);
        rcv = view.findViewById(R.id.rcv);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rlNoResult = view.findViewById(R.id.layout_no_result);
        tvNoResult = view.findViewById(R.id.tv_no_result);
        float padding = a.getDimension(R.styleable.BaseRecyclerView_brv_padding, 0);
        String textNoResult = a.getString(R.styleable.BaseRecyclerView_brv_text_no_result);
        tvNoResult.setText(textNoResult);
        if (padding != 0) {
            rcv.setPadding((int) padding, (int) padding, (int) padding, (int) padding);
        } else {
            float paddingStart = a.getDimension(R.styleable.BaseRecyclerView_brv_padding_start, 0);
            float paddingEnd = a.getDimension(R.styleable.BaseRecyclerView_brv_padding_end, 0);
            float paddingTop = a.getDimension(R.styleable.BaseRecyclerView_brv_padding_top, 0);
            float paddingBottom = a.getDimension(R.styleable.BaseRecyclerView_brv_padding_bottom, 0);
            rcv.setPadding((int) paddingStart, (int) paddingTop, (int) paddingEnd, (int) paddingBottom);
        }

        boolean enableRefresh = a.getBoolean(R.styleable.BaseRecyclerView_brv_enable_refresh, true);
        swipeRefresh.setEnabled(enableRefresh);
    }

    public void setEnableRefresh(boolean enableRefresh) {
        swipeRefresh.setEnabled(enableRefresh);
    }

    public void enableLoadmore(boolean enableLoadmore){
        mAdapter.enableLoadingMore(enableLoadmore);
    }

    public void enableRefresh(boolean enableRefresh){
        swipeRefresh.setRefreshing(enableRefresh);
    }

    private void init(Context context) {
        this.context = context;
    }

    public void setListLayoutManager(int orientation) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.context, orientation, false);
        rcv.setLayoutManager(layoutManager);
    }

    public void setGridLayoutManager(int spanCount) {
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mAdapter.getItemViewType(position) == EndlessLoadingRecyclerViewAdapter.VIEW_TYPE_LOADING) {
                    return spanCount;
                } else return 1;
            }
        });

        rcv.setLayoutManager(layoutManager);
    }

    public void refresh(List<T> data) {
        if(data.size() == 0){
            rlNoResult.setVisibility(View.VISIBLE);
        }else {
            rlNoResult.setVisibility(View.GONE);
            mAdapter.refresh(data);
        }
        swipeRefresh.setRefreshing(false);
    }

    public void addItem(List<T> data) {
        mAdapter.hideLoadingItem();
        mAdapter.addModels(data, false);
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener refreshListener) {
        swipeRefresh.setOnRefreshListener(refreshListener);
    }

    public void setOnLoadingMoreListener(OnLoadmoreListener loadingMoreListener) {
        this.listener = loadingMoreListener;
    }

    public void setOnItemClickListener(RecyclerViewAdapter.OnItemClickListener onItemClickListener) {
        mAdapter.addOnItemClickListener(onItemClickListener);
    }

    public void setAdapter(EndlessLoadingRecyclerViewAdapter adapter) {
        this.mAdapter = adapter;
        mAdapter.setLoadingMoreListener(this);
        rcv.setAdapter(adapter);
    }

    @Override
    public void onLoadMore() {
        mAdapter.showLoadingItem(true);
        listener.onLoadmore();
    }

    public interface OnLoadmoreListener{
        void onLoadmore();
    }
}
