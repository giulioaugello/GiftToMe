package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.LAM.GiftToMe.Adapter.ChatListAdapter;
import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.FCMFirebase.FirestoreListener;
import com.LAM.GiftToMe.FCMFirebase.ReceiverModel;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.LAM.GiftToMe.UsefulClass.UsersGift;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ChatFragment extends Fragment {

    private Context mContext;
    private ArrayList<ReceiverModel> receiverModels;
    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.chat_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        recyclerView = v.findViewById(R.id.recyclerview_chat);

//        Chat.getArrayGift("L_A_M98", new FirestoreListener() {
//            @Override
//            public void onMessageRetrieve(List<String> listenerMessages) {
//
//            }
//
//            @Override
//            public void onDateRetrieve(List<Date> listenerTimestamps) {
//
//            }
//
//            @Override
//            public void onChatRetrieve(List<Map<String, Object>> listenerChat) {
//
//                for (int k = 0; k < listenerChat.size(); k++){
//                    ReceiverModel receiverModel = new ReceiverModel()
//                }
//
//            }
//
//            @Override
//            public void onReceiverRetrieve(String receiver) {
//
//            }
//
//            @Override
//            public void onTaskError(Exception taskException) {
//
//            }
//        });

        return v;
    }
}
