package com.google.firebase.udacity.friendlychat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.udacity.friendlychat.data.dto.FriendlyMessage;
import com.google.firebase.udacity.friendlychat.databinding.ItemMessageBinding;
import com.google.firebase.udacity.friendlychat.view.MainActivity;

import java.util.List;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.MyViewHolder> {
    private List<FriendlyMessage> mDataset;
    private int currentListSize = 0;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MessageRecyclerViewAdapter(List<FriendlyMessage> myDataset) {
        mDataset = myDataset;
    }

    public void setDataset(List<FriendlyMessage> mDataset) {
        this.mDataset = mDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MessageRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                      int viewType) {
        return new MyViewHolder(
                ItemMessageBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        Log.d(MainActivity.TAG, "onDetachedFromRecyclerView");
    }

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        Log.d(MainActivity.TAG, "registerAdapterDataObserver");
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(MainActivity.TAG, "onAttachedToRecyclerView");
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // - get element from dataset at this position
        // - replace the contents of the view with that element
        FriendlyMessage message = mDataset.get(position);
        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            holder.binding.messageTextView.setVisibility(View.GONE);
            holder.binding.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.binding.photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(holder.binding.photoImageView);
        } else {
            holder.binding.messageTextView.setVisibility(View.VISIBLE);
            holder.binding.photoImageView.setVisibility(View.GONE);
            holder.binding.messageTextView.setText(message.getText());
        }
        holder.binding.nameTextView.setText(message.getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemMessageBinding binding;

        public MyViewHolder(ItemMessageBinding view) {
            super(view.getRoot());
            binding = view;
        }
    }
}

