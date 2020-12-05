package com.LAM.GiftToMe.FCMFirebase;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class Chat {

    private static final String TAG = "TAGCHAT";
    private static int indexReceiver = 0;

    public static void sendMessage(String username, final String receiver, final String reply, final Context context){ //voglio inviare un messaggio da username a receiver con testo reply

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                        Map<String, Object> model = new HashMap<>();
                        model.put("username", queryDocumentSnapshot.getData().get("username"));
                        model.put("token", queryDocumentSnapshot.getData().get("token"));

                        updateChat(chat, model, receiver, reply, queryDocumentSnapshot.getId(), context);


                        //Log.i("chatchat", "List " + chat); //List [{timestamps=[Timestamp(seconds=1607158800, nanoseconds=0)], messages=[Ciao Lam], receiver=Giulio2803}]
                        //Log.i("chatchat", "List " + queryDocumentSnapshot.getData()); //restituisce tutto il blocco di lam2803
//                        if (chat != null){
//
//                            Map<String, Object> model = new HashMap<>();
//                            model.put("username", queryDocumentSnapshot.getData().get("username"));
//                            model.put("token", queryDocumentSnapshot.getData().get("token"));
//
//                            if (!checkReceiverExist(chat, receiver)){ //se il receiver non c'è: creo una nuova hashmap e la aggiungo a chat
//
//                                Log.i("chatchat", "Nuova Chat");
//
//                                ArrayList<String> messagesArrayList = new ArrayList<>();
//                                messagesArrayList.add(reply);
//
//                                ArrayList<Date> datesArrayList = new ArrayList<>();
//                                Date date = new Date();
//                                datesArrayList.add(date);
//
//                                Map<String, Object> chatNewReceiver = new HashMap<>();
//                                chatNewReceiver.put("receiver", receiver);
//                                chatNewReceiver.put("messages", messagesArrayList);
//                                chatNewReceiver.put("timestamps", datesArrayList);
//                                chat.add(chatNewReceiver);
//
//                                model.put("chat", chat);
//
//                                collectionReference.document(queryDocumentSnapshot.getId()).set(model);
//
//                            }else { //se il receiver esiste aggiungo solo messaggio e timestamp
//
//                                Log.i("chatchat", "Chat già esistente " + reply);
//
//                                ArrayList<String> messagesArrayList = (ArrayList<String>) chat.get(indexReceiver).get("messages");
//                                messagesArrayList.add(reply);
//                                Log.i("chatchat", "Messages " + messagesArrayList);
//
//                                ArrayList<Date> datesArrayList = (ArrayList<Date>) chat.get(indexReceiver).get("timestamps");
//                                Date date = new Date();
//                                datesArrayList.add(date);
//                                Log.i("chatchat", "Dates " + datesArrayList + " " + chat.get(indexReceiver).get("receiver"));
//
//                                chat.get(indexReceiver).put("messages", messagesArrayList);
//                                chat.get(indexReceiver).put("timestamps", datesArrayList);
//
//                                model.put("chat", chat);
//
//                                collectionReference.document(queryDocumentSnapshot.getId()).set(model);
//
//                            }
//                            //Log.i("chatchat", "List " + chat);
//
////                            for (int i = 0; i < chat.size(); i++){
////                                Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
////                            }
//                        }

                    }
                }
            }
        });

    }

    public static void updateChat(List<Map<String, Object>> chat, Map<String, Object> model, final String receiver, String reply, String id, Context context){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");

        if (chat != null){

            if(!checkReceiverExist(chat, receiver)){ //se il receiver non c'è: creo una nuova hashmap e la aggiungo a chat

                Log.i("chatchat", "Nuova Chat");

                ArrayList<String> messagesArrayList = new ArrayList<>();
                messagesArrayList.add(reply);

                ArrayList<Date> datesArrayList = new ArrayList<>();
                Date date = new Date();
                datesArrayList.add(date);

                Map<String, Object> chatNewReceiver = new HashMap<>();
                chatNewReceiver.put("receiver", receiver);
                chatNewReceiver.put("messages", messagesArrayList);
                chatNewReceiver.put("timestamps", datesArrayList);
                chat.add(chatNewReceiver);

                model.put("chat", chat);

                collectionReference.document(id).set(model);

            }else{ //se il receiver esiste aggiungo solo messaggio e timestamp

                Log.i("chatchat", "Chat già esistente " + reply);
                Toast.makeText(context, "Hai già una chat con questo utente, vai nella sezione chat", Toast.LENGTH_SHORT).show();

                ArrayList<String> messagesArrayList = (ArrayList<String>) chat.get(indexReceiver).get("messages");
                messagesArrayList.add(reply);
                Log.i("chatchat", "Messages " + messagesArrayList);

                ArrayList<Date> datesArrayList = (ArrayList<Date>) chat.get(indexReceiver).get("timestamps");
                Date date = new Date();
                datesArrayList.add(date);
                Log.i("chatchat", "Dates " + datesArrayList + " " + chat.get(indexReceiver).get("receiver"));

                chat.get(indexReceiver).put("messages", messagesArrayList);
                chat.get(indexReceiver).put("timestamps", datesArrayList);

                model.put("chat", chat);

                collectionReference.document(id).set(model);

            }

        }

//        Log.i("chatchat", "List " + chat);
//
//        for (int i = 0; i < chat.size(); i++){
//            Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
//        }

    }

    //controllo se il receiver già esiste
    private static boolean checkReceiverExist(List<Map<String, Object>> chat, String receiver){
        for (int i = 0; i < chat.size(); i++){
            String receivers = (String) chat.get(i).get("receiver");
            if (receivers.equals(receiver)){
                indexReceiver = i;
                Log.i("chatchat", "Receiver boolean: " + receivers + " " + indexReceiver);
                return true;
            }
        }
        return false;
    }

    //ritorna List con tutti i messaggi da username a receiver
    public static List<String> getMessages(String username, final String receiver){

        final List<String> myList = new ArrayList<>();

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                        int index = 0;
                        for (int i = 0; i < chat.size(); i++){
                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
                            if (chat.get(i).get("receiver").equals(receiver)){
                                index = i;
                                break;
                            }
                        }

                        List<String> myMessages = (List<String>) chat.get(index).get("messages");
                        myList.addAll(myMessages);
                        Log.i("chatchat", "receiver: " + chat.get(index).get("receiver") + ", message: " + myMessages + " " + myList + myList.get(0));

//                        List<Date> myTimestamps = (List<Date>) chat.get(index).get("timestamps");
//                        Log.i("chatchat", "receiver: " + chat.get(index).get("receiver") + ", timestamp: " + myTimestamps + " " + myTimestamps.get(0));

                    }
                }
            }
        });

        return myList;

    }

    //ritorna List con tutti i timestamp da username a receiver
    public static List<Date> getTimestamps(String username, final String receiver){

        final List<Date> myList = new ArrayList<>();

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                        int index = 0;
                        for (int i = 0; i < chat.size(); i++){
                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
                            if (chat.get(i).get("receiver").equals(receiver)){
                                index = i;
                                break;
                            }
                        }

                        List<Date> myTimestamps = (List<Date>) chat.get(index).get("timestamps");
                        myList.addAll(myTimestamps);
                        Log.i("chatchat", "receiver: " + chat.get(index).get("receiver") + ", timestamp: " + myTimestamps + " " + myTimestamps.get(0));

                    }
                }
            }
        });

        return myList;

    }

}
