package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.LAM.GiftToMe.Adapter.ChatListAdapter;
import com.LAM.GiftToMe.Adapter.MyGiftTweetsAdapter;
import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.FCMFirebase.FirestoreListener;
import com.LAM.GiftToMe.FCMFirebase.ReceiverModel;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.LAM.GiftToMe.UsefulClass.UsersGift;

import com.airbnb.lottie.L;
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

    private EditText searchGift;
    private ImageView searchButton;
    private ArrayList<String> arrayListGiftName;
    private TextView noChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.chat_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        chatmodel = new ArrayList<>();

        recyclerView = v.findViewById(R.id.recyclerview_chat);

        searchGift = v.findViewById(R.id.gift_search_edit);

        noChat = v.findViewById(R.id.no_chat);

        //Chat.createSender("Giulio2803", "Prova");

        Chat.getArrayGift(MainActivity.userName, mContext, new FirestoreListener() {
            @Override
            public void onMessageRetrieve(List<String> listenerMessages) {

            }

            @Override
            public void onDateRetrieve(List<Date> listenerTimestamps) {

            }

            @Override
            public void onChatRetrieve(final List<Map<String, Object>> listenerChat) {

                //Log.i("chatchat", "listener: " + listenerChat.size());
                if (listenerChat.size() == 0){
                    noChat.setVisibility(View.VISIBLE);
                    return;
                }

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
                    chatmodel.add(receiverModel);

                }
                //Log.i("chatchat", "ChatModel: " +  arrayListGiftName);




                setupRecyclerView(chatmodel);

                //per la ricerca dei regali
                searchGift.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        // filter your list from your input
                        filter(s.toString());
                        //you can use runnable postDelayed like 500 ms to delay search text
                    }
                });

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

        //in testa esce l'ultima persona aggiunta nel database (tranne se già esisteva)
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(chatListAdapter);
    }

    //filtra sul nome dei regali
    private void filter(String text){
        ArrayList<ReceiverModel> temp = new ArrayList();
        for(ReceiverModel receiverModel: chatmodel){

            //controllo se il text che scrivo (tutto minuscolo, tutto maiuscolo o con la prima maiuscola) si trova nel giftName del receiverModel
            if(receiverModel.getGiftName().toLowerCase().contains(text) || receiverModel.getGiftName().contains(text) || receiverModel.getGiftName().toUpperCase().contains(text)){
                temp.add(receiverModel);
            }

        }
        //update recyclerview
        chatListAdapter.updateList(temp);
    }
}
