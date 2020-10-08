package com.google.firebase.udacity.friendlychat.Observer;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.udacity.friendlychat.View.MainActivity;

public class MessageRVAdapterDataObserver extends RecyclerView.AdapterDataObserver {
    private RecyclerView mRecyclerView;

    public MessageRVAdapterDataObserver(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);
        Log.d(MainActivity.TAG, "Scrolling at the bottom...");
        mRecyclerView.smoothScrollToPosition(positionStart);
    }
}
