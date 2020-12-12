package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.LAM.GiftToMe.Adapter.ReceiverChatAdapter;
import com.LAM.GiftToMe.FCMFirebase.Chat;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ReceiverChatFragment extends Fragment {

    private Context mContext;
    private RecyclerView recyclerView;
    private ReceiverChatAdapter receiverChatAdapterl;

    private String receiverName, giftName;

    private EditText text;
    private ImageView send, twPhoto;

    public ReceiverChatFragment(String receiverName, String giftName){
        this.receiverName = receiverName;
        this.giftName = giftName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.receiver_chat_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        recyclerView = v.findViewById(R.id.message_recyclerview);

        text = v.findViewById(R.id.message_text);
        send = v.findViewById(R.id.send_message);
        twPhoto = v.findViewById(R.id.tw_photo);

        TextView messageUsername = v.findViewById(R.id.tw_username);
        TextView messageGiftName = v.findViewById(R.id.mes_gift_name);

        messageUsername.setText(receiverName);
        messageGiftName.setText(giftName);
        getTwitterProfileImage(receiverName, twPhoto);


        return v;
    }

    private void getTwitterProfileImage(String username, final ImageView profileImage){
        TwitterRequests.getUserInfo(mContext, username, new VolleyListener() {

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
}
