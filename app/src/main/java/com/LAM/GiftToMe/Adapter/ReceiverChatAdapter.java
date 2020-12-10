package com.LAM.GiftToMe.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.LAM.GiftToMe.R;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ReceiverChatAdapter extends RecyclerView.Adapter<ReceiverChatAdapter.ViewHolder> {

    private Context mContext;

    //fare costruttore

    @NonNull
    @Override
    public ReceiverChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cardview_chat_fragment, parent, false);
        return new ReceiverChatAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiverChatAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{


        public ViewHolder(@NonNull View itemView) {
            super(itemView);


        }
    }
}
