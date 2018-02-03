package com.example.common.widget.recycler;

/**
 * Created by EvanShu on 2018/1/30.
 */

public interface AdapterCallback<Data> {
    void update(Data data, RecyclerAdapter.ViewHolder<Data> viewHolder);
}
