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
import com.LAM.GiftToMe.FCMFirebase.DBFirestore;
import com.LAM.GiftToMe.FCMFirebase.FirestoreListener;
import com.LAM.GiftToMe.FCMFirebase.ModelUserMessage;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.Picasso.CircleTransformation;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.Twitter.TwitterFunctions;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReplyChatFragment extends Fragment {

    private static final String TAG = "ReplyChatFragmentTAG";
    private Context mContext;
    private RecyclerView recyclerView;
    private ReplyChatAdapter replyChatAdapter;
    private ArrayList<ModelUserMessage> arrayMessages = new ArrayList<>();

    private String receiverName, giftName;

    private EditText text;
    private ImageView send, twPhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.reply_chat_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        recyclerView = v.findViewById(R.id.message_recyclerview);

        text = v.findViewById(R.id.message_text);
        send = v.findViewById(R.id.send_message);

        TextView messageUsername = v.findViewById(R.id.tw_username);
        TextView messageGiftName = v.findViewById(R.id.mes_gift_name);

        receiverName = getArguments().getString("receiverName");
        giftName = getArguments().getString("giftName");

        messageUsername.setText(receiverName);
        messageGiftName.setText(giftName);

        twPhoto = v.findViewById(R.id.tw_photo);
        getTwitterProfileImage(receiverName, twPhoto);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String yourReply = text.getText().toString();

                if (yourReply.isEmpty()){
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.write), Toast.LENGTH_SHORT).show();
                    return;
                }

                //se non è un mio regalo uso sendMessage altrimenti sendMessageMyGift (servono per inviare il messaggio nella chat e aggiungerli nel posto giusto nel DB)
                if (!ChatFragment.isMyGift){
                    Chat.sendMessage(MainActivity.userName, receiverName, yourReply, giftName);
                }else {
                    Chat.sendMessageMyGift(MainActivity.userName, receiverName, yourReply, giftName);
                }

                //creo la notifica
                final ArrayList<String> message = new ArrayList<>();
                String notificationTitle = mContext.getResources().getString(R.string.reply_notification_title);
                String notificationText = mContext.getResources().getString(R.string.reply_notification_text, MainActivity.userName, giftName);

                message.add(notificationTitle);
                message.add(notificationText);

                DBFirestore.getToken(receiverName, message, mContext); //prende i token e invia la notifica

                //crea l'utente il nuovo messaggio e lo aggiunge nel posto giusto
                ModelUserMessage newMessage = new ModelUserMessage(MainActivity.userName, receiverName, yourReply);
                arrayMessages.add(newMessage);
                replyChatAdapter.updateList(arrayMessages); //aggiorna la recyclerview
                text.setText("");

                recyclerView.scrollToPosition(replyChatAdapter.getItemCount() - 1); //mi serve per tenere il focus sull'ultimo messaggio inviato

            }
        });

        //confronta i timestamp dei messaggi e inserisce i messaggi nell'ordine giusto
//        if (!ChatFragment.isMyGift){
//
//            Chat.getTimestamps2(MainActivity.userName, receiverName, giftName, getActivity(), new FirestoreListener() {
//                @Override
//                public void onMessageRetrieve(List<String> listenerMessages) {
//
//                }
//
//                @Override
//                public void onDateRetrieve(final List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
//                    final List<Timestamp> timestamps = new ArrayList<>();
//                    final List<String> messages = new ArrayList<>();
//
//                    timestamps.addAll(listenerTimestamps);
//                    messages.addAll(listenerMessages);
//
//                    Log.i(TAG, "Timestamps: " + timestamps.size());
//
//                    Chat.getMyTimestamps2(receiverName, MainActivity.userName, giftName, getActivity(), new FirestoreListener() {
//                        @Override
//                        public void onMessageRetrieve(List<String> listenerMessages) {
//
//                        }
//
//                        @Override
//                        public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
//                            arrayMessages.clear();
//
//                            int index = 0;
//
//                            for (int i = 0; i < timestamps.size(); i++){
//
//                                if (listenerTimestamps.size() == 0) { //se hanno solo contattato un mio regalo ma ancora non rispondo
//
//                                    ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(i));
//                                    arrayMessages.add(modelUserMessage);
//
//                                }else {
//
//
//                                    for (int j = index; j < listenerTimestamps.size(); j++){
//
//
//                                        if (timestamps.get(i).compareTo(listenerTimestamps.get(j)) < 0){ //se timestamps < listener
//
//                                            if (i == timestamps.size()-1){ //se sono all'ultimo elemento di timestamps
//
//                                                ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(i));
//                                                arrayMessages.add(modelUserMessage);
//
//                                                if (j == listenerTimestamps.size()-1){ //se sono all'ultimo elemento di listener
//
//                                                    ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(j));
//                                                    arrayMessages.add(modelUserMessageJ);
//
//                                                }else { //altrimenti mostro tutti i rimanenti listener
//
//                                                    for (int k = j; k < listenerTimestamps.size(); k++){
//
//                                                        ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(k));
//                                                        arrayMessages.add(modelUserMessageJ);
//                                                    }
//                                                    break;
//
//                                                }
//
//                                            }else{ //se i non è l'ultimo elemento
//
//                                                ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(i));
//                                                arrayMessages.add(modelUserMessage);
//                                                index = j;
//                                                break;
//
//                                            }
//
//                                        }else{ //se timestamps >= listener
//
//                                            if (j == listenerTimestamps.size()-1){
//
//                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(j));
//                                                arrayMessages.add(modelUserMessageJ);
//
//                                                for (int k = i; k < timestamps.size(); k++){
//
//                                                    ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(k));
//                                                    arrayMessages.add(modelUserMessage);
//                                                    i = k;
//
//                                                }
//
//                                            }else {
//
//                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(j));
//                                                arrayMessages.add(modelUserMessageJ);
//
//                                            }
//
//                                        }
//
//                                    }
//
//                                }
//
//                            }
//
//
//                            Log.i(TAG, "ARRAYMESSAGE: " + arrayMessages.size());
//                            setupRecyclerView(arrayMessages);
//
//                        }
//
//                        @Override
//                        public void onChatRetrieve(List<Map<String, Object>> listenerChat) {
//
//                        }
//
//                        @Override
//                        public void onReceiverRetrieve(String receiver) {
//
//                        }
//
//                        @Override
//                        public void onTaskError(Exception taskException) {
//
//                        }
//                    });
//
//                }
//
//                @Override
//                public void onChatRetrieve(List<Map<String, Object>> listenerChat) {
//
//                }
//
//                @Override
//                public void onReceiverRetrieve(String receiver) {
//
//                }
//
//                @Override
//                public void onTaskError(Exception taskException) {
//
//                }
//            });
//
//        }else {
//
//
//            Chat.getMyTimestamps2(MainActivity.userName, receiverName, giftName, getActivity(), new FirestoreListener() {
//                @Override
//                public void onMessageRetrieve(List<String> listenerMessages) {
//
//                }
//
//                @Override
//                public void onDateRetrieve(final List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
//
//                    final List<Timestamp> timestamps = new ArrayList<>();
//                    final List<String> messages = new ArrayList<>();
//
//                    timestamps.addAll(listenerTimestamps);
//                    messages.addAll(listenerMessages);
//
//
//                    Chat.getTimestamps2(receiverName, MainActivity.userName, giftName, getActivity(), new FirestoreListener() {
//                        @Override
//                        public void onMessageRetrieve(List<String> listenerMessages) {
//
//                        }
//
//                        @Override
//                        public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
//
//                            arrayMessages.clear();
//
//                            int index = 0;
//
//                            for (int i = 0; i < listenerTimestamps.size(); i++){
//
//                                if (timestamps.size() == 0) { //se hanno solo contattato un mio regalo ma ancora non rispondo
//
//                                    ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(i));
//                                    arrayMessages.add(modelUserMessage);
//
//                                }else {
//
//
//                                    for (int j = index; j < timestamps.size(); j++){
//
//
//                                        if (listenerTimestamps.get(i).compareTo(timestamps.get(j)) < 0){ //se timestamps < listener
//
//                                            if (i == listenerTimestamps.size()-1){ //se sono all'ultimo elemento di timestamps
//
//                                                ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(i));
//                                                arrayMessages.add(modelUserMessage);
//
//                                                if (j == timestamps.size()-1){ //se sono all'ultimo elemento di listener
//
//                                                    ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(j));
//                                                    arrayMessages.add(modelUserMessageJ);
//
//                                                }else { //altrimenti mostro tutti i rimanenti listener
//
//                                                    for (int k = j; k < timestamps.size(); k++){
//
//                                                        ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(k));
//                                                        arrayMessages.add(modelUserMessageJ);
//
//                                                    }
//                                                    break;
//
//                                                }
//
//                                            }else{ //se i non è l'ultimo elemento
//
//                                                ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(i));
//                                                arrayMessages.add(modelUserMessage);
//                                                index = j;
//                                                break;
//
//                                            }
//
//                                        }else{ //se timestamps >= listener
//
//                                            if (j == timestamps.size()-1){
//
//                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(j));
//                                                arrayMessages.add(modelUserMessageJ);
//
//                                                for (int k = i; k < listenerTimestamps.size(); k++){
//
//                                                    ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(k));
//                                                    arrayMessages.add(modelUserMessage);
//                                                    i = k;
//
//                                                }
//
//                                            }else {
//
//                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(j));
//                                                arrayMessages.add(modelUserMessageJ);
//
//                                            }
//
//
//                                        }
//
//                                    }
//
//                                }
//
//                            }
//
//
//                            Log.i(TAG, "ARRAYMESSAGE2: " + arrayMessages);
//                            setupRecyclerView(arrayMessages);
//
//                        }
//
//                        @Override
//                        public void onChatRetrieve(List<Map<String, Object>> listenerChat) {
//
//                        }
//
//                        @Override
//                        public void onReceiverRetrieve(String receiver) {
//
//                        }
//
//                        @Override
//                        public void onTaskError(Exception taskException) {
//
//                        }
//                    });
//
//                }
//
//                @Override
//                public void onChatRetrieve(List<Map<String, Object>> listenerChat) {
//
//                }
//
//                @Override
//                public void onReceiverRetrieve(String receiver) {
//
//                }
//
//                @Override
//                public void onTaskError(Exception taskException) {
//
//                }
//            });
//
//        }



        if (!ChatFragment.isMyGift){

            Chat.getTimestamps2(MainActivity.userName, receiverName, giftName, new FirestoreListener() {
                @Override
                public void onMessageRetrieve(List<String> listenerMessages) {

                }

                @Override
                public void onDateRetrieve(final List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
                    final List<Timestamp> timestamps = new ArrayList<>();
                    final List<String> messages = new ArrayList<>();

                    timestamps.addAll(listenerTimestamps);
                    messages.addAll(listenerMessages);

                    Log.i(TAG, "Timestamps: " + timestamps.size());

                    Chat.getMyTimestamps2(receiverName, MainActivity.userName, giftName, new FirestoreListener() {
                        @Override
                        public void onMessageRetrieve(List<String> listenerMessages) {

                        }

                        @Override
                        public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
                            arrayMessages.clear();

                            int index = 0;

                            for (int i = 0; i < timestamps.size(); i++){

                                if (listenerTimestamps.size() == 0) { //se hanno solo contattato un mio regalo ma ancora non rispondo

                                    ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(i));
                                    arrayMessages.add(modelUserMessage);

                                }else {


                                    for (int j = index; j < listenerTimestamps.size(); j++){


                                        if (timestamps.get(i).compareTo(listenerTimestamps.get(j)) < 0){ //se timestamps < listener

                                            if (i == timestamps.size()-1){ //se sono all'ultimo elemento di timestamps

                                                ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(i));
                                                arrayMessages.add(modelUserMessage);

                                                if (j == listenerTimestamps.size()-1){ //se sono all'ultimo elemento di listener

                                                    ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(j));
                                                    arrayMessages.add(modelUserMessageJ);

                                                }else { //altrimenti mostro tutti i rimanenti listener

                                                    for (int k = j; k < listenerTimestamps.size(); k++){

                                                        ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(k));
                                                        arrayMessages.add(modelUserMessageJ);
                                                    }
                                                    break;

                                                }

                                            }else{ //se i non è l'ultimo elemento

                                                ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(i));
                                                arrayMessages.add(modelUserMessage);
                                                index = j;
                                                break;

                                            }

                                        }else{ //se timestamps >= listener

                                            if (j == listenerTimestamps.size()-1){

                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(j));
                                                arrayMessages.add(modelUserMessageJ);

                                                for (int k = i; k < timestamps.size(); k++){

                                                    ModelUserMessage modelUserMessage = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(k));
                                                    arrayMessages.add(modelUserMessage);
                                                    i = k;

                                                }

                                            }else {

                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(j));
                                                arrayMessages.add(modelUserMessageJ);

                                            }

                                        }

                                    }

                                }

                            }


                            Log.i(TAG, "ARRAYMESSAGE: " + arrayMessages.size());
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

                    timestamps.addAll(listenerTimestamps);
                    messages.addAll(listenerMessages);


                    Chat.getTimestamps2(receiverName, MainActivity.userName, giftName, new FirestoreListener() {
                        @Override
                        public void onMessageRetrieve(List<String> listenerMessages) {

                        }

                        @Override
                        public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {

                            arrayMessages.clear();

                            int index = 0;

                            for (int i = 0; i < listenerTimestamps.size(); i++){

                                if (timestamps.size() == 0) { //se hanno solo contattato un mio regalo ma ancora non rispondo

                                    ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(i));
                                    arrayMessages.add(modelUserMessage);

                                }else {


                                    for (int j = index; j < timestamps.size(); j++){


                                        if (listenerTimestamps.get(i).compareTo(timestamps.get(j)) < 0){ //se timestamps < listener

                                            if (i == listenerTimestamps.size()-1){ //se sono all'ultimo elemento di timestamps

                                                ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(i));
                                                arrayMessages.add(modelUserMessage);

                                                if (j == timestamps.size()-1){ //se sono all'ultimo elemento di listener

                                                    ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(j));
                                                    arrayMessages.add(modelUserMessageJ);

                                                }else { //altrimenti mostro tutti i rimanenti listener

                                                    for (int k = j; k < timestamps.size(); k++){

                                                        ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(k));
                                                        arrayMessages.add(modelUserMessageJ);

                                                    }
                                                    break;

                                                }

                                            }else{ //se i non è l'ultimo elemento

                                                ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(i));
                                                arrayMessages.add(modelUserMessage);
                                                index = j;
                                                break;

                                            }

                                        }else{ //se timestamps >= listener

                                            if (j == timestamps.size()-1){

                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(j));
                                                arrayMessages.add(modelUserMessageJ);

                                                for (int k = i; k < listenerTimestamps.size(); k++){

                                                    ModelUserMessage modelUserMessage = new ModelUserMessage(receiverName, MainActivity.userName, listenerMessages.get(k));
                                                    arrayMessages.add(modelUserMessage);
                                                    i = k;

                                                }

                                            }else {

                                                ModelUserMessage modelUserMessageJ = new ModelUserMessage(MainActivity.userName, receiverName, messages.get(j));
                                                arrayMessages.add(modelUserMessageJ);

                                            }


                                        }

                                    }

                                }

                            }


                            Log.i(TAG, "ARRAYMESSAGE2: " + arrayMessages);
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

        ImageView goBackButton = v.findViewById(R.id.go_back_chat);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatFragmentTag = getResources().getString(R.string.chat_fragment_tag);
                ChatFragment chatFragment = new ChatFragment();
                FragmentTransaction fragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.bottomtotop, R.anim.toptobottom);
                fragmentTransaction.replace(R.id.fragment_container, chatFragment, chatFragmentTag).commit();
                fragmentTransaction.addToBackStack(chatFragmentTag);
                MainActivity.activeFragment = chatFragment;
            }
        });

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

    //ritorna immagine del profilo
    private void getTwitterProfileImage(String username, final ImageView profileImage){
        TwitterFunctions.userInfo(mContext, username, new VolleyListener() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    //replace serve per prendere l'immagine con le dimensioni originali
                    Uri profileImgUri = Uri.parse((jsonObject.getString(mContext.getResources().getString(R.string.json_profile_image_url_https))).replace(mContext.getResources().getString(R.string.json_profile_image_normal),""));
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

    //mi serve per prendere nome del mittente e nome del regalo da ChatListAdapter (sostituisce il costruttore)
    public static ReplyChatFragment newInstance(String receiverName, String giftName){
        Bundle args = new Bundle();
        args.putString("receiverName", receiverName);
        args.putString("giftName", giftName);
        ReplyChatFragment replyChatFragment = new ReplyChatFragment();
        replyChatFragment.setArguments(args);
        return replyChatFragment;
    }

}
