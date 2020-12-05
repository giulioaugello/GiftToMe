package com.LAM.GiftToMe.FCMFirebase;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class Chat {

    private static final String TAG = "TAGCHAT";
    private static int indexReceiver = 0;

    public static void sendMessage(String username, final String receiver, final String reply){ //voglio inviare un messaggio da username a receiver con testo reply

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
                        //Log.i("chatchat", "List " + chat); //List [{timestamps=[Timestamp(seconds=1607158800, nanoseconds=0)], messages=[Ciao Lam], issuer=Giulio2803}]
                        //Log.i("chatchat", "List " + queryDocumentSnapshot.getData()); //restituisce tutto il blocco di lam2803
                        if (chat != null){

                            Map<String, Object> model = new HashMap<>();
                            model.put("username", queryDocumentSnapshot.getData().get("username"));
                            model.put("token", queryDocumentSnapshot.getData().get("token"));

                            if (!checkIssuerExist(chat, receiver)){ //se il receiver non c'è: creo una nuova hashmap e la aggiungo a chat

                                Log.i("chatchat", "Nuova Chat");

                                ArrayList<String> messagesArrayList = new ArrayList<>();
                                messagesArrayList.add(reply);
                                ArrayList<Date> datesArrayList = new ArrayList<>();
                                Date date = new Date();
                                datesArrayList.add(date);
                                Map<String, Object> chatNewIssuer = new HashMap<>();
                                chatNewIssuer.put("issuer", receiver);
                                chatNewIssuer.put("messages", messagesArrayList);
                                chatNewIssuer.put("timestamps", datesArrayList);
                                chat.add(chatNewIssuer);

                                model.put("chat", chat);

                                collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                            }else { //se il receiver esiste aggiungo solo messaggio e timestamp

                                Log.i("chatchat", "Chat già esistente " + reply);

                                ArrayList<String> messagesArrayList = (ArrayList<String>) chat.get(indexReceiver).get("messages");
                                messagesArrayList.add(reply);
                                Log.i("chatchat", "Messages " + messagesArrayList);

                                ArrayList<Date> datesArrayList = (ArrayList<Date>) chat.get(indexReceiver).get("timestamps");
                                Date date = new Date();
                                datesArrayList.add(date);
                                Log.i("chatchat", "Dates " + datesArrayList + " " + chat.get(indexReceiver).get("issuer"));

                                chat.get(indexReceiver).put("messages", messagesArrayList);
                                chat.get(indexReceiver).put("timestamps", datesArrayList);

                                model.put("chat", chat);

                                collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                            }
                            //Log.i("chatchat", "List " + chat);

//                            for (int i = 0; i < chat.size(); i++){
//                                Log.i("chatchat", "receiver: " + chat.get(i).get("issuer") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
//                            }
                        }

                    }
                }
            }
        });

//        DocumentReference documentReference = collectionReference.document();
//        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()){
//                    DocumentSnapshot documentSnapshot = task.getResult();
//                    if (documentSnapshot.exists()){
//                        List<Map<String, Object>> chat = (List<Map<String, Object>>) documentSnapshot.get("chat");
//                        Log.i("chatchat", "List " + chat);
//                    }
//                }
//            }
//        });
    }

    private static boolean checkIssuerExist(List<Map<String, Object>> chat, String receiver){
        for (int i = 0; i < chat.size(); i++){
            String issuers = (String) chat.get(i).get("issuer");
            if (issuers.equals(receiver)){
                indexReceiver = i;
                Log.i("chatchat", "Issuer boolean: " + issuers + " " + indexReceiver);
                return true;
            }
        }
        return false;
    }

    public static void updateChat(String id, Map<String, Object> data, Map<String, Object> dataInside, String message, String issuer){

        if (data.containsKey("chat")){
            List<Map<String, Object>> listChat = (ArrayList) data.get("chat");



//            String issuers = (String) mapChat.get("issuer");
//            if (mapChat.containsKey("issuer")){
//                if (issuers.equals(issuer)){
//                    Log.i(TAG, "Hai già una chat con questa persona");
//                }else{
//                    ArrayList<String> messagesArrayList = (ArrayList<String>) mapChat.get("messages");
//                    messagesArrayList.add(message);
//                    Map<String, Object> chatModel = new HashMap<>();
//                    chatModel.put("issuer", issuer);
//                    chatModel.put("messages", messagesArrayList);
//
//                    FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
//                    CollectionReference collectionReference = firestoreDB.collection("users");
//                    Map<String, Object> model = new HashMap<>();
//                    model.put("username", data.get("username"));
//                    model.put("token", data.get("token"));
//                    model.put("chat", chatModel);
//                    collectionReference.document(id).set(model);
//                }
//            }


        }
    }

}
