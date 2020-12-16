package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.LAM.GiftToMe.Adapter.ChatListAdapter;
import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.FCMFirebase.FirestoreListener;
import com.LAM.GiftToMe.FCMFirebase.ReceiverModel;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatFragment extends Fragment {

    private Context mContext;
    private ArrayList<ReceiverModel> chatModel, chatModelMyGift;
    private RecyclerView recyclerViewReply, recyclerViewMyGift;
    private ChatListAdapter chatListAdapter;

    private EditText searchGift;
    private ImageView searchButton;
    private ArrayList<String> arrayListGiftName, arrayListMyGift;

    public static boolean isMyGift;
    private Button replyArrayButton, myGiftArrayButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.chat_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        chatModel = new ArrayList<>();

        recyclerViewReply = v.findViewById(R.id.recyclerview_chat);
        recyclerViewMyGift = v.findViewById(R.id.recyclerview_mychat);

        searchGift = v.findViewById(R.id.gift_search_edit);

        isMyGift = false;

        replyArrayButton = v.findViewById(R.id.reply_array_button);
        myGiftArrayButton = v.findViewById(R.id.mygift_array_button);



        Chat.getArrayGift(MainActivity.userName, mContext, new FirestoreListener() {
            @Override
            public void onMessageRetrieve(List<String> listenerMessages) {

            }

            @Override
            public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {

            }

            @Override
            public void onChatRetrieve(final List<Map<String, Object>> listenerChat) {

                //Log.i("chatchat", "listener: " + listenerChat.size());

                arrayListGiftName = new ArrayList<>();

                for (int k = 0; k < listenerChat.size(); k++){

                    //String gifts = (String) listenerChat.get(k).get("giftName");
                    List<String> messages = (List<String>) listenerChat.get(k).get("messages");
                    List<Timestamp> timestamps = (List<Timestamp>) listenerChat.get(k).get("timestamps");

//                    Log.i("chatchat", "gifts: " + gifts);
//                    Log.i("chatchat", "messages: " + messages + messages.get(0));
//                    Log.i("chatchat", "timestamps: " + timestamps);

                    final ReceiverModel receiverModel = new ReceiverModel();

                    receiverModel.setUsername(Chat.listString.get(k));
                    receiverModel.setGiftName((String) listenerChat.get(k).get("giftName"));
                    receiverModel.setMessages((ArrayList<String>) listenerChat.get(k).get("messages"));
                    receiverModel.setTimestamps((ArrayList<Date>) listenerChat.get(k).get("timestamps"));

                    arrayListGiftName.add((String) listenerChat.get(k).get("giftName"));
                    chatModel.add(receiverModel);

                }
                //Log.i("chatchat", "ChatModel: " +  arrayListGiftName);


                setupRecyclerView(chatModel, recyclerViewReply);



            }

            @Override
            public void onReceiverRetrieve(String receiver) {

            }

            @Override
            public void onTaskError(Exception taskException) {

            }
        });


        Chat.getArrayMyGift2(MainActivity.userName, mContext, new FirestoreListener() {
            @Override
            public void onMessageRetrieve(List<String> listenerMessages) {

            }

            @Override
            public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {

            }

            @Override
            public void onChatRetrieve(List<Map<String, Object>> listenerChat) {
                arrayListMyGift = new ArrayList<>();
                chatModelMyGift = new ArrayList<>();
                //Log.i("chatchat", "getArrayMyGift: " + chatModelMyGift.size() + " ... " + listenerChat.size());

                for (int k = 0; k < listenerChat.size(); k++){

                    //String gifts = (String) listenerChat.get(k).get("giftName");
//                    List<String> messages = (List<String>) listenerChat.get(k).get("messages");
//                    List<Timestamp> timestamps = (List<Timestamp>) listenerChat.get(k).get("timestamps");

//                    Log.i("chatchat", "gifts: " + gifts);
//                    Log.i("chatchat", "messages: " + messages + messages.get(0));
//                    Log.i("chatchat", "timestamps: " + timestamps);

                    final ReceiverModel receiverModel = new ReceiverModel();

                    receiverModel.setUsername((String) listenerChat.get(k).get("sender"));
                    receiverModel.setGiftName(Chat.listNameMyGift.get(k));
                    receiverModel.setMessages((ArrayList<String>) listenerChat.get(k).get("messages"));
                    receiverModel.setTimestamps((ArrayList<Date>) listenerChat.get(k).get("timestamps"));

                    arrayListMyGift.add((String) listenerChat.get(k).get("sender"));
                    chatModelMyGift.add(receiverModel);

                }
                //Log.i("chatchat", "ChatModel: " +  arrayListGiftName);


                setupRecyclerView(chatModelMyGift, recyclerViewMyGift);
                listenerChat.clear();
                Chat.listNameMyGift.clear();
                chatListAdapter.updateList(chatModelMyGift);
            }

            @Override
            public void onReceiverRetrieve(String receiver) {

            }

            @Override
            public void onTaskError(Exception taskException) {

            }
        });



        replyArrayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isMyGift = false;

                myGiftArrayButton.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                recyclerViewMyGift.setVisibility(View.GONE);

                replyArrayButton.setTextColor(getResources().getColor(R.color.colorChipSelected, null));
                recyclerViewReply.setVisibility(View.VISIBLE);
            }
        });

        myGiftArrayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isMyGift = true;

                replyArrayButton.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                recyclerViewReply.setVisibility(View.GONE);

                myGiftArrayButton.setTextColor(getResources().getColor(R.color.colorChipSelected, null));
                recyclerViewMyGift.setVisibility(View.VISIBLE);

            }
        });

        //per la ricerca dei regali
        searchGift.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                // filter your list from your input
                filter(s.toString());
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        });


        return v;
    }


    private void setupRecyclerView(ArrayList<ReceiverModel> receiverModels, RecyclerView recyclerView){
        chatListAdapter = new ChatListAdapter(mContext, receiverModels, getActivity(), this);

        //in testa esce l'ultima persona aggiunta nel database (tranne se già esisteva)
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(chatListAdapter);
    }

    //filtra sul nome dei regali
    private void filter(String text){
        ArrayList<ReceiverModel> temp = new ArrayList();
        ArrayList<ReceiverModel> temp1 = new ArrayList();

        myGiftArrayButton.setTextColor(getResources().getColor(R.color.colorPrimary, null));
        recyclerViewMyGift.setVisibility(View.GONE);

        replyArrayButton.setTextColor(getResources().getColor(R.color.colorChipSelected, null));
        recyclerViewReply.setVisibility(View.VISIBLE);

        for(ReceiverModel receiverModel: chatModel){

            //controllo se il text che scrivo (tutto minuscolo, tutto maiuscolo o con la prima maiuscola) si trova nel giftName del receiverModel
            if(receiverModel.getGiftName().toLowerCase().contains(text) || receiverModel.getGiftName().contains(text) || receiverModel.getGiftName().toUpperCase().contains(text)){
                temp.add(receiverModel);
            }

        }

        if (temp.size() == 0){

            replyArrayButton.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            recyclerViewReply.setVisibility(View.GONE);

            myGiftArrayButton.setTextColor(getResources().getColor(R.color.colorChipSelected, null));
            recyclerViewMyGift.setVisibility(View.VISIBLE);

            for(ReceiverModel receiverModel: chatModelMyGift){

                //controllo se il text che scrivo (tutto minuscolo, tutto maiuscolo o con la prima maiuscola) si trova nel giftName del receiverModel
                if(receiverModel.getGiftName().toLowerCase().contains(text) || receiverModel.getGiftName().contains(text) || receiverModel.getGiftName().toUpperCase().contains(text)){
                    temp1.add(receiverModel);
                }

            }
            //update recyclerview
            chatListAdapter.updateList(temp1);

        }else {
            //update recyclerview
            chatListAdapter.updateList(temp);
        }


    }
}
