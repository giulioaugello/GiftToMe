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
import com.twitter.sdk.android.core.identity.OAuthActivity;

import com.google.firebase.Timestamp;
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
    private static int indexGift = 0;
    private static int indexName = 0;

    public static void sendMessage(String username, final String receiver, final String reply, final String giftName){ //voglio inviare un messaggio da username a receiver con testo reply

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

                        updateChat(chat, model, receiver, reply, giftName, queryDocumentSnapshot);

                        //Log.i("chatchat", "List " + chat); //List [{timestamps=[Timestamp(seconds=1607158800, nanoseconds=0)], messages=[Ciao Lam], receiver=Giulio2803}]
                        //Log.i("chatchat", "List " + queryDocumentSnapshot.getData()); //restituisce tutto il blocco di lam2803
//                        if (chat != null) {
//
//                            Map<String, Object> model = new HashMap<>();
//                            model.put("username", queryDocumentSnapshot.getData().get("username"));
//                            model.put("token", queryDocumentSnapshot.getData().get("token"));
//
//                            if (!checkReceiverExist(chat, receiver)) { //se il receiver non c'è: creo una nuova hashmap e la aggiungo a chat
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
//                                List<Map<String, Object>> gift = new ArrayList<>();
//                                Map<String, Object> giftInfo = new HashMap<>();
//                                giftInfo.put("giftName", giftName);
//                                giftInfo.put("messages", messagesArrayList);
//                                giftInfo.put("timestamps", datesArrayList);
//
//                                gift.add(giftInfo);
//
//                                Map<String, Object> chatNewReceiver = new HashMap<>();
//                                chatNewReceiver.put("receiver", receiver);
//                                chatNewReceiver.put("arrayGift", gift);
//
//                                chat.add(chatNewReceiver);
//
//                                model.put("chat", chat);
//
//                                collectionReference.document(queryDocumentSnapshot.getId()).set(model);
//
//                            } else { //se il receiver esiste: controllo se il regalo è presente già in arrayGift
//
//                                Log.i("chatchat", "Chat già esistente " + reply);
//
//                                List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexReceiver).get("arrayGift");
//
//
//                                if (checkGiftExist(giftList, giftName)){ //se ho già una chat per questo regalo
//
//                                    Log.i("chatchat", "Chat con regalo che già ho");
//
//                                    ArrayList<String> messagesArrayList = (ArrayList<String>) giftList.get(indexGift).get("messages");
//                                    messagesArrayList.add(reply);
//                                    Log.i("chatchat", "Messages " + messagesArrayList);
//
//                                    ArrayList<Date> datesArrayList = (ArrayList<Date>) giftList.get(indexGift).get("timestamps");
//                                    Date date = new Date();
//                                    datesArrayList.add(date);
//                                    Log.i("chatchat", "Dates " + datesArrayList + " " + chat.get(indexReceiver).get("receiver"));
//
//                                    giftList.get(indexGift).put("messages", messagesArrayList);
//                                    giftList.get(indexGift).put("timestamps", datesArrayList);
//
//                                    chat.get(indexReceiver).put("receiver", receiver);
//                                    chat.get(indexReceiver).put("arrayGift", giftList);
//
//                                    model.put("chat", chat);
//
//                                    collectionReference.document(queryDocumentSnapshot.getId()).set(model);
//
//                                }else{ //se non ho una chat per questo regalo
//
//                                    Log.i("chatchat", "Chat con Nuovo Regalo");
//
//                                    ArrayList<String> messagesArrayList = new ArrayList<>();
//                                    messagesArrayList.add(reply);
//
//                                    ArrayList<Date> datesArrayList = new ArrayList<>();
//                                    Date date = new Date();
//                                    datesArrayList.add(date);
//
//                                    Map<String, Object> mapToAdd = new HashMap<>(); //creo una nuova map da aggiungere a giftList
//                                    mapToAdd.put("giftName", giftName);
//                                    mapToAdd.put("messages", messagesArrayList);
//                                    mapToAdd.put("timestamps", datesArrayList);
//
//                                    giftList.add(mapToAdd);
//
//                                    chat.get(indexReceiver).put("receiver", receiver);
//                                    chat.get(indexReceiver).put("arrayGift", giftList);
//
//                                    model.put("chat", chat);
//
//                                    collectionReference.document(queryDocumentSnapshot.getId()).set(model);
//                                }
//
//                                //Log.i("chatchat", "List " + giftList);
//                            }
//
//
////                            for (int i = 0; i < chat.size(); i++){
////                                Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", arrayGift: " + giftI.get(i).get("giftName") + " " + giftI.get(i).get("messages") + " " + giftI.get(i).get("timestamps"));
////                            }
//
////                        }
//                        }

                    }
                }
            }
        });

    }

    public static void updateChat(List<Map<String, Object>> chat, Map<String, Object> model, final String receiver, String reply, String giftName, QueryDocumentSnapshot queryDocumentSnapshot){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");

        if (chat != null) {

            if (!checkReceiverExist(chat, receiver)) { //se il receiver non c'è: creo una nuova hashmap e la aggiungo a chat

                Log.i("chatchat", "Nuova Chat");

                ArrayList<String> messagesArrayList = new ArrayList<>();
                messagesArrayList.add(reply);

                ArrayList<Date> datesArrayList = new ArrayList<>();
                Date date = new Date();
                datesArrayList.add(date);

                List<Map<String, Object>> gift = new ArrayList<>();
                Map<String, Object> giftInfo = new HashMap<>();
                giftInfo.put("giftName", giftName);
                giftInfo.put("messages", messagesArrayList);
                giftInfo.put("timestamps", datesArrayList);

                gift.add(giftInfo);

                Map<String, Object> chatNewReceiver = new HashMap<>();
                chatNewReceiver.put("receiver", receiver);
                chatNewReceiver.put("arrayGift", gift);

                chat.add(chatNewReceiver);

                model.put("chat", chat);

                collectionReference.document(queryDocumentSnapshot.getId()).set(model);

            } else { //se il receiver esiste: controllo se il regalo è presente già in arrayGift

                Log.i("chatchat", "Chat già esistente " + reply);

                List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexReceiver).get("arrayGift");


                if (checkGiftExist(giftList, giftName)){ //se ho già una chat per questo regalo

                    Log.i("chatchat", "Chat con regalo che già ho");

                    ArrayList<String> messagesArrayList = (ArrayList<String>) giftList.get(indexGift).get("messages");
                    messagesArrayList.add(reply);
                    Log.i("chatchat", "Messages " + messagesArrayList);

                    ArrayList<Date> datesArrayList = (ArrayList<Date>) giftList.get(indexGift).get("timestamps");
                    Date date = new Date();
                    datesArrayList.add(date);
                    Log.i("chatchat", "Dates " + datesArrayList + " " + chat.get(indexReceiver).get("receiver"));

                    giftList.get(indexGift).put("messages", messagesArrayList);
                    giftList.get(indexGift).put("timestamps", datesArrayList);

                    chat.get(indexReceiver).put("receiver", receiver);
                    chat.get(indexReceiver).put("arrayGift", giftList);

                    model.put("chat", chat);

                    collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                }else{ //se non ho una chat per questo regalo

                    Log.i("chatchat", "Chat con Nuovo Regalo");

                    ArrayList<String> messagesArrayList = new ArrayList<>();
                    messagesArrayList.add(reply);

                    ArrayList<Date> datesArrayList = new ArrayList<>();
                    Date date = new Date();
                    datesArrayList.add(date);

                    Map<String, Object> mapToAdd = new HashMap<>(); //creo una nuova map da aggiungere a giftList
                    mapToAdd.put("giftName", giftName);
                    mapToAdd.put("messages", messagesArrayList);
                    mapToAdd.put("timestamps", datesArrayList);

                    giftList.add(mapToAdd);

                    chat.get(indexReceiver).put("receiver", receiver);
                    chat.get(indexReceiver).put("arrayGift", giftList);

                    model.put("chat", chat);

                    collectionReference.document(queryDocumentSnapshot.getId()).set(model);
                }

                //Log.i("chatchat", "List " + giftList);
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

    private static boolean checkGiftExist(List<Map<String, Object>> gift, String giftName){
        for (int i = 0; i < gift.size(); i++){
            String giftInside = (String) gift.get(i).get("giftName");
            if (giftInside.equals(giftName)){
                indexGift = i;
                Log.i("chatchat", "Receiver boolean: " + giftInside + " " + indexGift);
                return true;
            }
        }
        return false;
    }

    //ritorna List con tutti i messaggi da username a receiver
    public static void getMessages(String username, final String receiver, final String giftName, final FirestoreListener listener){

        final List<String> myList = new ArrayList<>();

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                        int indexR = 0; //indice del destinatario
                        for (int i = 0; i < chat.size(); i++){
                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
                            if (chat.get(i).get("receiver").equals(receiver)){
                                indexR = i;
                                break;
                            }
                        }

                        //lista dei regali del destinatario
                        List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexR).get("arrayGift");

                        int indexG = 0; //indice del regalo
                        for (int j = 0; j < giftList.size(); j++){
                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
                            if (giftList.get(j).get("giftName").equals(giftName)){
                                indexG = j;
                                break;
                            }
                        }

                        List<String> myMessages = (List<String>) giftList.get(indexG).get("messages");
                        myList.addAll(myMessages);
                        Log.i("chatchat", "receiver: " + chat.get(indexR).get("receiver") + ", message: " + myMessages + ", return: " + myList +  " " + myList.get(0));

                        listener.onMessageRetrieve(myList);

                    }
                }else{
                    listener.onTaskError(task.getException());
                }
            }
        });

    }

    //ritorna List con tutti i timestamp da username a receiver
    public static void getTimestamps(String username, final String receiver, final String giftName, final FirestoreListener listener){

        final List<Date> myList = new ArrayList<>();

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                        int indexR = 0; //indice del destinatario
                        for (int i = 0; i < chat.size(); i++){
                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
                            if (chat.get(i).get("receiver").equals(receiver)){
                                indexR = i;
                                break;
                            }
                        }

                        //lista dei regali del destinatario
                        List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexR).get("arrayGift");

                        int indexG = 0; //indice del regalo
                        for (int j = 0; j < giftList.size(); j++){
                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
                            if (giftList.get(j).get("giftName").equals(giftName)){
                                indexG = j;
                                break;
                            }
                        }

                        List<Date> myTimestamps = (List<Date>) giftList.get(indexG).get("timestamps");
                        myList.addAll(myTimestamps);
                        Log.i("chatchat", "receiver: " + chat.get(indexR).get("receiver") + ", timestamp: " + myTimestamps + ", return " + myList + " " + myList.get(0));

                        Log.i("chatchat",  "myList: " + myList);

                        listener.onDateRetrieve(myList);
                    }
                }else{
                    listener.onTaskError(task.getException());
                }
            }
        });


    }

    public static void getReceiverUsernameFromGift(String username, final String giftName, final String firstMessage, final Timestamp firstTimestamp, final FirestoreListener listener){

        final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){

                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
                        String receiverUsername = "";

                        for (int i = 0; i < chat.size(); i++){ //per ogni chat

                            List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("arrayGift");

                            for (int j = 0; j < giftList.size(); j++){ //per ogni regalo

                                List<String> messages = (List<String>) giftList.get(j).get("messages");
                                List<Timestamp> timestamps = (List<Timestamp>) giftList.get(j).get("timestamps");

//                                Log.i("chatchat", "messages1: " + messages + messages.get(0));
//                                Log.i("chatchat", "timestamps1: " + timestamps);

                                if (giftList.get(j).get("giftName").equals(giftName) && messages.get(0).equals(firstMessage) && timestamps.get(0).equals(firstTimestamp)){
                                    receiverUsername = (String) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("receiver");
                                    indexName = i;
                                    Log.i("chatchat", "IndexName: " + indexName);
                                }

                            }
                        }

                        listener.onReceiverRetrieve(receiverUsername);

                    }
                }else {
                    listener.onTaskError(task.getException());
                }
            }
        });
    }


    public static void getArrayGift(final String username, final FirestoreListener firestoreListener){

        final List<Map<String, Object>> myList = new ArrayList<>();

        final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){

                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
                        String receiverUsername = "";

                        for (int i = 0; i < chat.size(); i++){

                            List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("arrayGift");

                            for (int j = 0; j < giftList.size(); j++){

                                List<String> messages = (List<String>) giftList.get(j).get("messages");
                                List<Timestamp> timestamps = (List<Timestamp>) giftList.get(j).get("timestamps");
                                //Log.i("chatchat",  "myListFor: " + giftList.get(j));


                                myList.add(giftList.get(j));

                            }

                        }
                        firestoreListener.onChatRetrieve(myList);


                    }
                }else {
                    firestoreListener.onTaskError(task.getException());
                }
            }
        });
    }

    public static int indexOfName(List<Map<String, Object>> chat, List<Map<String, Object>> array, String giftName, String firstMessage, Timestamp firstTimestamp){
        int intToReturn = 0;

        for (int i = 0; i < chat.size(); i++){
            for (int j = 0; j < array.size(); j++){
                List<String> messages = (List<String>) array.get(j).get("messages");
                List<Timestamp> timestamps = (List<Timestamp>) array.get(j).get("timestamps");
                if (array.get(j).get("giftName").equals(giftName) && messages.get(0).equals(firstMessage) && timestamps.get(0).equals(firstTimestamp)){
                    intToReturn = i;
                }
            }
        }
        return intToReturn;
    }

    public static void getReceiverArray(String username, final FirestoreListener firestoreListener){
        final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
//                        List<String> listName = new ArrayList<>();
//                        for (int i = 0; i < chat.size(); i++){
//                            String chatReceiver = (String) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("receiver");
//                            listName.add(chatReceiver);
//                        }
                        String chatReceiver = (String) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexName).get("receiver");
                        firestoreListener.onReceiverRetrieve(chatReceiver);

                    }
                }else {
                    firestoreListener.onTaskError(task.getException());
                }
            }
        });
    }

    //prendo ogni elemento dell'array chat
    public static void getArrayChat(String username, final FirestoreListener listener){

        final List<Map<String, Object>> myList = new ArrayList<>();

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
                        myList.addAll(chat);
//                        for (int i = 0; i < chat.size(); i++){
//                            Log.i("chatchat",  "receiver: " + myList.get(i).get("receiver") + " " + myList.get(i).get("arrayGift"));
//                        }

                        Log.i("chatchat",  "myList: " + myList);

                        //listener.onChatRetrieve(myList, chat);

                    }
                }else{
                    listener.onTaskError(task.getException());
                }
            }
        });

    }

}
