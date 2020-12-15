package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.LAM.GiftToMe.Adapter.ReplyChatAdapter;
import com.LAM.GiftToMe.FCMFirebase.Chat;
import com.LAM.GiftToMe.FCMFirebase.FirestoreListener;
import com.LAM.GiftToMe.FCMFirebase.ModelUserMessage;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.Picasso.CircleTransformation;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterRequests;
import com.LAM.GiftToMe.Twitter.VolleyListener;
import com.android.volley.VolleyError;
import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReplyChatFragment extends Fragment {

    private Context mContext;
    private RecyclerView recyclerView;
    private ReplyChatAdapter replyChatAdapter;
    private ArrayList<ModelUserMessage> arrayMessages = new ArrayList<>();

    private String receiverName, giftName;

    private EditText text;
    private ImageView send, twPhoto;

    public ReplyChatFragment(String receiverName, String giftName){
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


        TextView messageUsername = v.findViewById(R.id.tw_username);
        TextView messageGiftName = v.findViewById(R.id.mes_gift_name);

        messageUsername.setText(receiverName);
        messageGiftName.setText(giftName);

        twPhoto = v.findViewById(R.id.tw_photo);
        getTwitterProfileImage(receiverName, twPhoto);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String yourReply = text.getText().toString();

                if (yourReply.isEmpty()){
                    Toast.makeText(mContext, "Inserisci un messaggio", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!ChatFragment.isMyGift){
                    Chat.sendMessage(MainActivity.userName, receiverName, yourReply, giftName);
                }else {
                    Chat.sendMessageMyGift(MainActivity.userName, receiverName, yourReply, giftName);
                }

                ModelUserMessage newMessage = new ModelUserMessage(MainActivity.userName, receiverName, yourReply);
                arrayMessages.add(newMessage);
                replyChatAdapter.updateList(arrayMessages); //aggiorna la recyclerview
                Log.i("chatchat", "COUNT: " + replyChatAdapter.getItemCount() + " ... " + arrayMessages.size());
                text.setText("");
                recyclerView.scrollToPosition(replyChatAdapter.getItemCount() - 1); //mi serve per tenere il focus sull'ultimo messaggio inviato

            }
        });




        if (!ChatFragment.isMyGift){


            Chat.getTimestamps2(MainActivity.userName, receiverName, giftName, new FirestoreListener() {
                @Override
                public void onMessageRetrieve(List<String> listenerMessages) {

                }

                @Override
                public void onDateRetrieve(final List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
                    final List<Timestamp> timestamps = new ArrayList<>();
                    final List<String> messages = new ArrayList<>();
                    //Chat.provaSnapshot(MainActivity.userName);

                    timestamps.addAll(listenerTimestamps);
                    messages.addAll(listenerMessages);
                    //Log.i("chatchat", "MMM: " + messages);
                    Log.i("chatchat", "Timestamps: " + timestamps.size());
                    //Log.i("chatchat", "Gift: " + giftName);

                    // sopra hanno le i
                    //sotto le j

                    Chat.getMyTimestamps2(receiverName, MainActivity.userName, giftName, new FirestoreListener() {
                        @Override
                        public void onMessageRetrieve(List<String> listenerMessages) {

                        }

                        @Override
                        public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
                            arrayMessages.clear();
                            Log.i("chatchat", "AAA: " + arrayMessages.size());
                            //Log.i("chatchat", "MMM1: " + listenerMessages);
                            //Log.i("chatchat", "Messages: " + listenerTimestamps + " ... " + listenerMessages + " ... " + timestamps + " ... " +  messages);
                            //Log.i("chatchat", "Gift: " + giftName);

                            int fullSize = timestamps.size() + listenerTimestamps.size();


                            int index = 0;

                            for (int i = 0; i < timestamps.size(); i++){

                                if (listenerTimestamps.size() == 0) { //se hanno solo contattato un mio regalo ma ancora non rispondo

                                    //Log.i("chatchat", "Ancora non rispondo, mostro solo i suoi messaggi");
                                    //Log.i("chatchat", messages.get(i));
                                    ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(i));
                                    arrayMessages.add(modelUserMessage);

                                }else {


                                    for (int j = index; j < listenerTimestamps.size(); j++){


                                        if (timestamps.get(i).compareTo(listenerTimestamps.get(j)) < 0){ //se timestamps < listener

                                            if (i == timestamps.size()-1){ //se sono all'ultimo elemento di timestamps

                                                //Log.i("chatchat", "Prima timestamps"); //mostro l'ultimo
                                                //Log.i("chatchat", messages.get(i));
                                                ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(i));
                                                arrayMessages.add(modelUserMessage);

                                                if (j == listenerTimestamps.size()-1){ //se sono all'ultimo elemento di listener
                                                    //Log.i("chatchat", "Ultimo elemento di listener"); //lo mostro
                                                    //Log.i("chatchat", listenerMessages.get(j));
                                                    ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(j));
                                                    arrayMessages.add(modelUserMessageJ);

                                                }else { //altrimenti mostro tutti i rimanenti listener

                                                    for (int k = j; k < listenerTimestamps.size(); k++){
                                                        //Log.i("chatchat", "Mostro tutti i rimanenti j");
                                                        //Log.i("chatchat", listenerMessages.get(k));
                                                        ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(k));
                                                        arrayMessages.add(modelUserMessageJ);
                                                    }
                                                    break;

                                                }

                                            }else{ //se i non è l'ultimo elemento

                                                //Log.i("chatchat", "Prima timestamps");
                                                //Log.i("chatchat", messages.get(i));
                                                //Log.i("chatchat", "JJJ: " + j);
                                                ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(i));
                                                arrayMessages.add(modelUserMessage);
                                                index = j;
                                                break;

                                            }

                                        }else{ //se timestamps >= listener

                                            if (j == listenerTimestamps.size()-1){

                                                //Log.i("chatchat", "Prima listenerTimestamps");
                                                //Log.i("chatchat", listenerMessages.get(j));
                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(j));
                                                arrayMessages.add(modelUserMessageJ);

                                                for (int k = i; k < timestamps.size(); k++){
                                                    //Log.i("chatchat", "Mostro tutti i rimanenti i ");
                                                    //Log.i("chatchat", messages.get(k));
                                                    ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(k));
                                                    arrayMessages.add(modelUserMessage);
                                                    i = k;
                                                }

                                            }else {

                                                //Log.i("chatchat", "Prima listenerTimestamps");
                                                //Log.i("chatchat", listenerMessages.get(j));
                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(j));
                                                arrayMessages.add(modelUserMessageJ);

                                            }


                                        }
                                    }

//                                }

                                }



                            }


                            Log.i("chatchat", "ARRAYMESSAGE: " + arrayMessages.size());
                            setupRecyclerView(arrayMessages);

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
        }else {


            Chat.getMyTimestamps2(MainActivity.userName, receiverName, giftName, new FirestoreListener() {
                @Override
                public void onMessageRetrieve(List<String> listenerMessages) {

                }

                @Override
                public void onDateRetrieve(final List<Timestamp> listenerTimestamps, List<String> listenerMessages) {

                    final List<Timestamp> timestamps = new ArrayList<>();
                    final List<String> messages = new ArrayList<>();
                    //Chat.provaSnapshot(MainActivity.userName);

                    timestamps.addAll(listenerTimestamps);
                    messages.addAll(listenerMessages);
                    //Log.i("chatchat", "Timestamps: " + timestamps + " ... " + messages);
                    //Log.i("chatchat", "Gift: " + giftName);


                    Chat.getTimestamps2(receiverName, MainActivity.userName, giftName, new FirestoreListener() {
                        @Override
                        public void onMessageRetrieve(List<String> listenerMessages) {

                        }

                        @Override
                        public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
                            //Log.i("chatchat", "Messages: " + listenerTimestamps + " ... " + listenerMessages + " ... " + timestamps + " ... " +  messages);
                            //Log.i("chatchat", "Gift: " + giftName);

                            arrayMessages.clear();

                            int fullSize = timestamps.size() + listenerTimestamps.size();

                            int index = 0;

                            for (int i = 0; i < listenerTimestamps.size(); i++){

                                if (timestamps.size() == 0) { //se hanno solo contattato un mio regalo ma ancora non rispondo

                                    //Log.i("chatchat", "Ancora non rispondo, mostro solo i suoi messaggi");
                                    //Log.i("chatchat", listenerMessages.get(i));
                                    ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(i));
                                    arrayMessages.add(modelUserMessage);

                                }else {


                                    for (int j = index; j < timestamps.size(); j++){


                                        if (listenerTimestamps.get(i).compareTo(timestamps.get(j)) < 0){ //se timestamps < listener

                                            if (i == listenerTimestamps.size()-1){ //se sono all'ultimo elemento di timestamps

                                                //Log.i("chatchat", "Prima timestamps"); //mostro l'ultimo
                                                //Log.i("chatchat", listenerMessages.get(i));
                                                ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(i));
                                                arrayMessages.add(modelUserMessage);

                                                if (j == timestamps.size()-1){ //se sono all'ultimo elemento di listener
                                                    //Log.i("chatchat", "Ultimo elemento di listener"); //lo mostro
                                                    //Log.i("chatchat", messages.get(j));
                                                    ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(j));
                                                    arrayMessages.add(modelUserMessageJ);

                                                }else { //altrimenti mostro tutti i rimanenti listener

                                                    for (int k = j; k < timestamps.size(); k++){
                                                        //Log.i("chatchat", "Mostro tutti i rimanenti j");
                                                        //Log.i("chatchat", messages.get(k));
                                                        ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(k));
                                                        arrayMessages.add(modelUserMessageJ);
                                                    }
                                                    break;

                                                }

                                            }else{ //se i non è l'ultimo elemento

                                                //Log.i("chatchat", "Prima timestamps");
                                                //Log.i("chatchat", listenerMessages.get(i));
                                                //Log.i("chatchat", "JJJ: " + j);
                                                ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(i));
                                                arrayMessages.add(modelUserMessage);
                                                index = j;
                                                break;

                                            }

                                        }else{ //se timestamps >= listener

                                            if (j == timestamps.size()-1){

                                                //Log.i("chatchat", "Prima listenerTimestamps");
                                                //Log.i("chatchat", messages.get(j));
                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(j));
                                                arrayMessages.add(modelUserMessageJ);

                                                for (int k = i; k < listenerTimestamps.size(); k++){
                                                    //Log.i("chatchat", "Mostro tutti i rimanenti i ");
                                                    //Log.i("chatchat", listenerMessages.get(k));
                                                    ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(k));
                                                    arrayMessages.add(modelUserMessage);
                                                    i = k;
                                                }

                                            }else {

                                                //Log.i("chatchat", "Prima listenerTimestamps");
                                                //Log.i("chatchat", messages.get(j));
                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(j));
                                                arrayMessages.add(modelUserMessageJ);

                                            }


                                        }
                                    }

//                                }

                                }



                            }


                            //Log.i("chatchat", "ARRAYMESSAGE: " + arrayMessages);
                            setupRecyclerView(arrayMessages);

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

        }







        return v;
    }

    private void setupRecyclerView(ArrayList<ModelUserMessage> modelUserMessages){
        replyChatAdapter = new ReplyChatAdapter(mContext, modelUserMessages, getActivity(), this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.scrollToPosition(replyChatAdapter.getItemCount() - 1);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(replyChatAdapter);
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
