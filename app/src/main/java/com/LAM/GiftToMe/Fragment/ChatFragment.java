package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.LAM.GiftToMe.Adapter.ChatListAdapter;
import com.LAM.GiftToMe.Adapter.MyGiftTweetsAdapter;
import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.FCMFirebase.FirestoreListener;
import com.LAM.GiftToMe.FCMFirebase.ReceiverModel;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.LAM.GiftToMe.UsefulClass.UsersGift;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatFragment extends Fragment {

    private Context mContext;
    private ArrayList<ReceiverModel> chatmodel;
    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;

    private String user = "";
    private ArrayList<String> strings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.chat_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        chatmodel = new ArrayList<>();
        strings = new ArrayList<>();

        recyclerView = v.findViewById(R.id.recyclerview_chat);

        Chat.getArrayGift("L_A_M98", new FirestoreListener() {
            @Override
            public void onMessageRetrieve(List<String> listenerMessages) {

            }

            @Override
            public void onDateRetrieve(List<Date> listenerTimestamps) {

            }

            @Override
            public void onChatRetrieve(List<Map<String, Object>> listenerChat) {
                final ArrayList<String> prova = new ArrayList<>();

                for (int prove = 0; prove < listenerChat.size(); prove++){

                    List<String> messages = (List<String>) listenerChat.get(prove).get("messages");
                    List<Timestamp> timestamps = (List<Timestamp>) listenerChat.get(prove).get("timestamps");

                    Chat.getReceiverUsernameFromGift("L_A_M98", (String) listenerChat.get(prove).get("giftName"), messages.get(0), timestamps.get(0), new FirestoreListener() {
                        @Override
                        public void onMessageRetrieve(List<String> listenerMessages) {

                        }

                        @Override
                        public void onDateRetrieve(List<Date> listenerTimestamps) {

                        }

                        @Override
                        public void onChatRetrieve(List<Map<String, Object>> listenerChat) {

                        }

                        @Override
                        public void onReceiverRetrieve(String receiver) {
                            Log.i("chatchat", "onReceiverRetrieve: " + receiver);
                            prova.add(receiver);
                        }

                        @Override
                        public void onTaskError(Exception taskException) {

                        }
                    });
                }
                Log.i("chatchat", "Array: " + prova);

                for (int k = 0; k < listenerChat.size(); k++){

                    //String gifts = (String) listenerChat.get(k).get("giftName");
                    List<String> messages = (List<String>) listenerChat.get(k).get("messages");
                    List<Timestamp> timestamps = (List<Timestamp>) listenerChat.get(k).get("timestamps");

//                    Log.i("chatchat", "gifts: " + gifts);
//                    Log.i("chatchat", "messages: " + messages + messages.get(0));
//                    Log.i("chatchat", "timestamps: " + timestamps);

                    final ReceiverModel receiverModel = new ReceiverModel();

                    //receiverModel.setUsername();

                    receiverModel.setGiftName((String) listenerChat.get(k).get("giftName"));
                    receiverModel.setMessages((ArrayList<String>) listenerChat.get(k).get("messages"));
                    receiverModel.setTimestamps((ArrayList<Date>) listenerChat.get(k).get("timestamps"));

                    chatmodel.add(receiverModel);
//                    for (ReceiverModel model: chatmodel){
//                        Log.i("chatchat", "Model: " + model.getUsername());
//                    }
                }


                setupRecyclerView(chatmodel);

            }

            @Override
            public void onReceiverRetrieve(String receiver) {

            }

            @Override
            public void onTaskError(Exception taskException) {

            }
        });

        return v;
    }

    private void setupRecyclerView(ArrayList<ReceiverModel> receiverModels){
        chatListAdapter = new ChatListAdapter(mContext, receiverModels, getActivity(), this);

        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        recyclerView.setAdapter(chatListAdapter);
    }
}
