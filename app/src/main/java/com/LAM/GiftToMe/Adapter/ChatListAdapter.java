package com.LAM.GiftToMe.Adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.FCMFirebase.ReceiverModel;
import com.LAM.GiftToMe.Picasso.CircleTransformation;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private Context mContext;
    private Activity activity;
    private Fragment fragment;
    private ArrayList<ReceiverModel> arrayChat;

    public ChatListAdapter(Context mContext, ArrayList<ReceiverModel> arrayChat, Activity activity, Fragment fragment) {
        this.mContext = mContext;
        this.activity = activity;
        this.fragment = fragment;
        this.arrayChat = arrayChat;
    }

    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cardview_chat_fragment, parent, false);
        return new ChatListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ViewHolder holder, int position) {

        holder.userName.setText(arrayChat.get(position).getUsername());
        holder.nameGift.setText(arrayChat.get(position).getGiftName());

//        String category = usersGifts.get(position).getCategory();
//        MyGiftTweetsAdapter.changeCategoryImage(category, holder.imageCategory);

        getTwitterProfileImage(position, arrayChat, holder.imageTwitterUser);

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Pazzesco", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayChat.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private CardView card;
        private ImageView imageCategory, imageTwitterUser;
        private TextView userName, nameGift;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.card_chat);
            imageTwitterUser = itemView.findViewById(R.id.image_twitter);
            imageCategory = itemView.findViewById(R.id.image_category);
            userName = itemView.findViewById(R.id.user_name_gift);
            nameGift = itemView.findViewById(R.id.name_gift);

        }
    }

    private void getTwitterProfileImage(int position, ArrayList<ReceiverModel> usersGifts, final ImageView profileImage){
        TwitterRequests.getUserInfo(mContext, usersGifts.get(position).getUsername(), new VolleyListener() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject userObject = new JSONObject(response);

                    //replace serve per prendere l'immagine con le dimensioni (width e height) originali
                    Uri profileImgUri = Uri.parse((userObject.getString(mContext.getResources().getString(R.string.json_profile_image_url_https))).replace(mContext.getResources().getString(R.string.json_profile_image_normal),""));
                    Picasso.with(mContext).load(profileImgUri).transform(new CircleTransformation()).into(profileImage);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    //aggiorna la lista
    public void updateList(ArrayList<ReceiverModel> list){
        arrayChat = list;
        notifyDataSetChanged();
    }

}
