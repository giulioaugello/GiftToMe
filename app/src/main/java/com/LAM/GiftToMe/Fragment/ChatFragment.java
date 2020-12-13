package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {

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

        final List<Timestamp> timestamps = new ArrayList<>();
        final List<String> messages = new ArrayList<>();

        Chat.getTimestamps("L_A_M98", "Giulio2803", "Prova8", new FirestoreListener() {
            @Override
            public void onMessageRetrieve(List<String> listenerMessages) {

            }

            @Override
            public void onDateRetrieve(final List<Timestamp> listenerTimestamps, List<String> listenerMessages) {

                timestamps.addAll(listenerTimestamps);
                messages.addAll(listenerMessages);
                //Log.i("chatchat", "Timestamps: " + timestamps + " ... " + messages);


                // sopra hanno le i
                //sotto le j






                Chat.getMyTimestamps("Giulio2803", "L_A_M98", "Prova8", new FirestoreListener() {
                    @Override
                    public void onMessageRetrieve(List<String> listenerMessages) {

                    }

                    @Override
                    public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
                        Log.i("chatchat", "Messages: " + listenerTimestamps + " ... " + listenerMessages + " ... " + timestamps + " ... " +  messages);

                        int fullSize = timestamps.size() + listenerTimestamps.size();
                        //Log.i("chatchat", "fullsize: " + fullSize);
                        int index = 0;

                        //fullSize = 3
                        for (int i = 0; i < timestamps.size(); i++){

                            if (listenerTimestamps.size() == 0) { //se hanno solo contattato un mio regalo ma ancora non rispondo

                                Log.i("chatchat", "Ancora non rispondo, mostro solo i suoi messaggi");
                                Log.i("chatchat", messages.get(i));

                            }else {


                                    for (int j = index; j < listenerTimestamps.size(); j++){

                                        //Log.i("chatchat", i + " ... " + j);

                                        if (timestamps.get(i).compareTo(listenerTimestamps.get(j)) < 0){ //se timestamps < listener
                                            //Log.i("chatchat", "J: " + j + " " + listenerTimestamps.size());

                                            if (i == timestamps.size()-1){ //se sono all'ultimo elemento di timestamps

                                                Log.i("chatchat", "Prima timestamps"); //mostro l'ultimo
                                                Log.i("chatchat", messages.get(i));

                                                if (j == listenerTimestamps.size()-1){ //se sono all'ultimo elemento di listener
                                                    Log.i("chatchat", "Ultimo elemento di listener"); //lo mostro
                                                    Log.i("chatchat", listenerMessages.get(j));

                                                }else { //altrimenti mostro tutti i rimanenti listener

                                                    for (int k = j; k < listenerTimestamps.size(); k++){
                                                        Log.i("chatchat", "Mostro tutti i rimanenti j");
                                                        Log.i("chatchat", listenerMessages.get(k));
                                                    }
                                                    break;

                                                }

                                            }else{ //se i non è l'ultimo elemento

                                                Log.i("chatchat", "Prima timestamps");
                                                Log.i("chatchat", messages.get(i));
                                                //Log.i("chatchat", "JJJ: " + j);
                                                index = j;
                                                break;

                                            }

                                        }else{ //se timestamps >= listener

                                            if (j == listenerTimestamps.size()-1){

                                                Log.i("chatchat", "Prima listenerTimestamps");
                                                Log.i("chatchat", listenerMessages.get(j));

                                                for (int k = i; k < timestamps.size(); k++){
                                                    Log.i("chatchat", "Mostro tutti i rimanenti i ");
                                                    Log.i("chatchat", messages.get(k));
                                                    i = k;
                                                }

                                            }else {

                                                Log.i("chatchat", "Prima listenerTimestamps");
                                                Log.i("chatchat", listenerMessages.get(j));

                                            }


                                        }
                                    }

//                                }

                            }



                        }

                    }

                    @Override
                    public void onChatRetrieve(List<Map<String, Object>> listenerChat) {

                    }

                    @Override
                    public void onReceiverRetrieve(String receiver) {

                    }

                    @Override
                    public void onTaskError(Exception taskException) {

                    }
                });

            }

            @Override
            public void onChatRetrieve(List<Map<String, Object>> listenerChat) {

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
