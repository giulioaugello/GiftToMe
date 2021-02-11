package com.LAM.GiftToMe.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.LAM.GiftToMe.Adapter.ChatListAdapter;
import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.FCMFirebase.FirestoreListener;
import com.LAM.GiftToMe.FCMFirebase.ReceiverModel;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;

import com.LAM.GiftToMe.UsefulClass.OnSwipeTouchListener;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragmentTAG";
    private Context mContext;
    private ArrayList<ReceiverModel> chatModel, chatModelMyGift;
    private RecyclerView recyclerViewReply, recyclerViewMyGift;
    private ChatListAdapter chatListAdapterReply, chatListAdapterMyGift;

    private EditText searchGiftReply, searchMyGift;
    private ImageView searchButton;
    private ArrayList<String> arrayListGiftName, arrayListMyGift;

    public static boolean isMyGift;
    private Button replyArrayButton, myGiftArrayButton;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.chat_fragment, container, false);

        mContext = getActivity().getApplicationContext();

//        chatModel = new ArrayList<>();

        recyclerViewReply = v.findViewById(R.id.recyclerview_chat);
        recyclerViewMyGift = v.findViewById(R.id.recyclerview_mychat);

        searchGiftReply = v.findViewById(R.id.gift_search_edit);
        searchMyGift = v.findViewById(R.id.gift_search_edit_mygift);

        isMyGift = false;

        replyArrayButton = v.findViewById(R.id.reply_array_button);
        myGiftArrayButton = v.findViewById(R.id.mygift_array_button);

        final Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.chat_fragment_tag));

        //ritorna le chat delle risposte
        Chat.getArrayGift2(MainActivity.userName, mContext, new FirestoreListener() {
            @Override
            public void onMessageRetrieve(List<String> listenerMessages) {

            }

            @Override
            public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {

            }

            @Override
            public void onChatRetrieve(final List<Map<String, Object>> listenerChat) {

                arrayListGiftName = new ArrayList<>();
                chatModel = new ArrayList<>();

                for (int k = 0; k < listenerChat.size(); k++){

                    final ReceiverModel receiverModel = new ReceiverModel();

                    receiverModel.setUsername(Chat.listString.get(k));
                    receiverModel.setGiftName((String) listenerChat.get(k).get("giftName"));
                    receiverModel.setMessages((ArrayList<String>) listenerChat.get(k).get("messages"));
                    receiverModel.setTimestamps((ArrayList<Date>) listenerChat.get(k).get("timestamps"));

                    arrayListGiftName.add((String) listenerChat.get(k).get("giftName"));
                    chatModel.add(receiverModel);

                }
                Log.i(TAG, "ChatModel: " +  chatModel);

                chatListAdapterReply = new ChatListAdapter(mContext, chatModel, getActivity(), fragment);

                setupRecyclerView(recyclerViewReply);
                recyclerViewReply.setAdapter(chatListAdapterReply);

                listenerChat.clear();
                Chat.listString.clear();
                chatListAdapterReply.updateList(chatModel);

            }

            @Override
            public void onReceiverRetrieve(String receiver) {

            }

            @Override
            public void onTaskError(Exception taskException) {

            }
        });

        //ritorna le chat dei miei regali
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

                for (int k = 0; k < listenerChat.size(); k++){

                    final ReceiverModel receiverModel = new ReceiverModel();

                    receiverModel.setUsername((String) listenerChat.get(k).get("sender"));
                    receiverModel.setGiftName(Chat.listNameMyGift.get(k));
                    receiverModel.setMessages((ArrayList<String>) listenerChat.get(k).get("messages"));
                    receiverModel.setTimestamps((ArrayList<Date>) listenerChat.get(k).get("timestamps"));

                    arrayListMyGift.add((String) listenerChat.get(k).get("sender"));
                    chatModelMyGift.add(receiverModel);

                }
                Log.i(TAG, "ChatModel: " +  chatModelMyGift);

                chatListAdapterMyGift = new ChatListAdapter(mContext, chatModelMyGift, getActivity(), fragment);

                setupRecyclerView(recyclerViewMyGift);
                recyclerViewMyGift.setAdapter(chatListAdapterMyGift);

                listenerChat.clear();
                Chat.listNameMyGift.clear();

                chatListAdapterMyGift.updateList(chatModelMyGift);
            }

            @Override
            public void onReceiverRetrieve(String receiver) {

            }

            @Override
            public void onTaskError(Exception taskException) {

            }
        });

        //cambio recyclerview tramite buttons

        replyArrayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isMyGift = false;

                myGiftArrayButton.setTextColor(getResources().getColor(R.color.primaryAndWhite, null));
                recyclerViewMyGift.setVisibility(View.GONE);

                replyArrayButton.setTextColor(getResources().getColor(R.color.colorChipSelected, null));
                recyclerViewReply.setVisibility(View.VISIBLE);

                searchGiftReply.setVisibility(View.VISIBLE);
                searchMyGift.setVisibility(View.GONE);
                Log.i("MYGIFTMYGIFT", "isMyGift2: " +  isMyGift);
            }
        });

        myGiftArrayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isMyGift = true;

                replyArrayButton.setTextColor(getResources().getColor(R.color.primaryAndWhite, null));
                recyclerViewReply.setVisibility(View.GONE);

                myGiftArrayButton.setTextColor(getResources().getColor(R.color.colorChipSelected, null));
                recyclerViewMyGift.setVisibility(View.VISIBLE);

                searchGiftReply.setVisibility(View.GONE);
                searchMyGift.setVisibility(View.VISIBLE);
                Log.i("MYGIFTMYGIFT", "isMyGift3: " +  isMyGift);
            }
        });

        //per lo swipe

        recyclerViewReply.setOnTouchListener(new OnSwipeTouchListener(mContext) {
            public void onSwipeLeft() {
                isMyGift = true;

                replyArrayButton.setTextColor(getResources().getColor(R.color.primaryAndWhite, null));
                recyclerViewReply.setVisibility(View.GONE);

                myGiftArrayButton.setTextColor(getResources().getColor(R.color.colorChipSelected, null));
                recyclerViewMyGift.setVisibility(View.VISIBLE);

                searchGiftReply.setVisibility(View.GONE);
                searchMyGift.setVisibility(View.VISIBLE);
            }

        });

        recyclerViewMyGift.setOnTouchListener(new OnSwipeTouchListener(mContext) {
            public void onSwipeRight() {
                isMyGift = false;

                myGiftArrayButton.setTextColor(getResources().getColor(R.color.primaryAndWhite, null));
                recyclerViewMyGift.setVisibility(View.GONE);

                replyArrayButton.setTextColor(getResources().getColor(R.color.colorChipSelected, null));
                recyclerViewReply.setVisibility(View.VISIBLE);

                searchGiftReply.setVisibility(View.VISIBLE);
                searchMyGift.setVisibility(View.GONE);
            }

        });

        //per la ricerca dei regali

        searchGiftReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                // filter your list from your input
                filter(s.toString(), chatModel, chatListAdapterReply);
            }
        });

        searchMyGift.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                // filter your list from your input
                filter(s.toString(), chatModelMyGift, chatListAdapterMyGift);

            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setupRecyclerView(RecyclerView recyclerView){

        //in testa esce l'ultima persona aggiunta nel database (tranne se già esisteva)
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

    }

    private void filter(String text, ArrayList<ReceiverModel> model, ChatListAdapter chatListAdapter){
        ArrayList<ReceiverModel> temp = new ArrayList();

        for(ReceiverModel receiverModel: model){

            //controllo se il text che scrivo (tutto minuscolo, tutto maiuscolo o con la prima maiuscola) si trova nel giftName del receiverModel
            if(receiverModel.getGiftName().toLowerCase().contains(text) || receiverModel.getGiftName().contains(text) || receiverModel.getGiftName().toUpperCase().contains(text)){
                temp.add(receiverModel);
                Log.i(TAG, "MYMY: " +  temp + " " + receiverModel.getGiftName());
            }

        }

        //update recyclerview
        chatListAdapter.updateList(temp);

    }

}
