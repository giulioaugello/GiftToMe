package com.LAM.GiftToMe.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.LAM.GiftToMe.FCMFirebase.ModelUserMessage;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ReplyChatAdapter extends RecyclerView.Adapter<ReplyChatAdapter.ViewHolder> {

    private Context mContext;
    private Activity activity;
    private Fragment fragment;
    private ArrayList<ModelUserMessage> arrayMessage;


    public ReplyChatAdapter(Context mContext, ArrayList<ModelUserMessage> arrayMessage, Activity activity, Fragment fragment) {
        this.mContext = mContext;
        this.activity = activity;
        this.fragment = fragment;
        this.arrayMessage = arrayMessage;
    }

    @NonNull
    @Override
    public ReplyChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.single_message_textview, parent, false);
        return new ReplyChatAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyChatAdapter.ViewHolder holder, int position) {

        GradientDrawable shapeBackground;

        if (arrayMessage.get(position).getSender().equals(MainActivity.userName)){
            shapeBackground = (GradientDrawable)mContext.getDrawable(R.drawable.user_message_item_background);
            holder.linearLayout.setGravity(Gravity.END);
        }else{
            shapeBackground = (GradientDrawable)mContext.getDrawable(R.drawable.interlocutor_message_item_background);
            holder.linearLayout.setGravity(Gravity.START);
        }

        holder.message.setBackground(shapeBackground);

        holder.message.setText(arrayMessage.get(position).getMessage());

    }

    @Override
    public int getItemCount() {
        return arrayMessage.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout linearLayout;
        private TextView message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.message_layout);
            message = itemView.findViewById(R.id.message_text);

        }
    }

    public void updateList(ArrayList<ModelUserMessage> list){
        arrayMessage = list;
        notifyDataSetChanged();
    }
}
