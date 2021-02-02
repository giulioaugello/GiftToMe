package com.LAM.GiftToMe.FCMFirebase;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Chat {

    private static final String TAG = "TAGCHAT";
    private static int indexReceiver = 0;
    private static int indexGift = 0;
    private static int indexSender = 0;
    private static int indexMyGift = 0;
    private static int indexName = 0;
    public static List<String> listString;
    public static List<String> listNameMyGift;
    private static List<String> senderDeleteGift;

    //uso questo quando sono io ad inviare il messaggio
    public static void sendMessageMyGift(final String username, final String sender, final String reply, final String giftName){ //voglio inviare un messaggio da username a receiver con testo reply

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chatMyGift = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");
                        Map<String, Object> model = new HashMap<>();
                        model.put("username", queryDocumentSnapshot.getData().get("username"));
                        model.put("token", queryDocumentSnapshot.getData().get("token"));
                        model.put("chat", queryDocumentSnapshot.getData().get("chat"));

                        if (chatMyGift != null){

                            int indexG = 0;
                            for (int i = 0; i < chatMyGift.size(); i++){
                                Log.i("chatchat", "myGiftName: " + chatMyGift.get(i).get("myGiftName"));
                                if (chatMyGift.get(i).get("myGiftName").equals(giftName)){ //cerco l'indice del regalo che mi serve per myGiftList
                                    indexG = i;
                                    break;
                                }
                            }

                            List<Map<String, Object>> myGiftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(indexG).get("arrayMyGift");

                            if (checkSenderExist(myGiftList, sender)){
                                ArrayList<String> messagesArrayList = (ArrayList<String>) myGiftList.get(indexSender).get("messages");
                                messagesArrayList.add(reply);

                                ArrayList<Date> datesArrayList = (ArrayList<Date>) myGiftList.get(indexSender).get("timestamps");
                                Date date = new Date();
                                datesArrayList.add(date);

                                myGiftList.get(indexSender).put("messages", messagesArrayList);
                                myGiftList.get(indexSender).put("timestamps", datesArrayList);

                                chatMyGift.get(indexG).put("myGiftName", giftName);
                                chatMyGift.get(indexG).put("arrayMyGift", myGiftList);

                                model.put("chatMyGift", chatMyGift);

                                collectionReference.document(queryDocumentSnapshot.getId()).set(model);
                            }else {
                                return;
                            }
                        }
                    }
                }
            }
        });

    }

    //quando qualcuno mi invia un messaggio
    public static void sendMessage(final String username, final String receiver, final String reply, final String giftName){ //voglio inviare un messaggio da username a receiver con testo reply

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

                    }
                }
            }
        });

    }


    public static void sendMessageFromGift(final String username, final String receiver, final String reply, final String giftName){ //voglio inviare un messaggio da username a receiver con testo reply

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

                        //dopo di questo creo in chatMyGift del receiver uno slot con username come sender (se già non esiste)
                        //mentre messages e timestamps per il receiver restano vuoti
                        createSender(receiver, username, giftName);

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

    //creazione slot per arrayGift
    public static void updateChat(List<Map<String, Object>> chat, Map<String, Object> model, final String receiver, String reply, String giftName, QueryDocumentSnapshot queryDocumentSnapshot){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");

        if (chat != null) {

            if (!checkReceiverExist(chat, receiver)) { //se il receiver non c'è: creo una nuova hashmap e la aggiungo a chat (creo uno slot per arrayGift)

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
                model.put("chatMyGift", queryDocumentSnapshot.getData().get("chatMyGift"));

                collectionReference.document(queryDocumentSnapshot.getId()).set(model);

            } else { //se il receiver esiste: controllo se il regalo è presente già in arrayGift

                Log.i("chatchat", "Chat già esistente " + reply);

                List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexReceiver).get("arrayGift");


                if (checkGiftExist(giftList, giftName)){ //se ho già una chat per questo regalo

                    Log.i("chatchat", "Chat con regalo che già ho");

                    ArrayList<String> messagesArrayList = (ArrayList<String>) giftList.get(indexGift).get("messages");
                    messagesArrayList.add(reply);
                    //Log.i("chatchat", "Messages " + messagesArrayList);

                    ArrayList<Date> datesArrayList = (ArrayList<Date>) giftList.get(indexGift).get("timestamps");
                    Date date = new Date();
                    datesArrayList.add(date);
                    //Log.i("chatchat", "Dates " + datesArrayList + " " + chat.get(indexReceiver).get("receiver"));

                    giftList.get(indexGift).put("messages", messagesArrayList);
                    giftList.get(indexGift).put("timestamps", datesArrayList);

                    chat.get(indexReceiver).put("receiver", receiver);
                    chat.get(indexReceiver).put("arrayGift", giftList);

                    model.put("chat", chat);
                    model.put("chatMyGift", queryDocumentSnapshot.getData().get("chatMyGift"));

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
                    model.put("chatMyGift", queryDocumentSnapshot.getData().get("chatMyGift"));

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

    //quando inserisco un nuovo regalo lo aggiungo anche all'array chatMyGift
    public static void newGiftUpload(String username, final String giftName){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        List<Map<String, Object>> chatMyGift = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");
//                        Map<String, Object> modelMyGift = new HashMap<>();
//                        modelMyGift.put("username", queryDocumentSnapshot.getData().get("username"));
//                        modelMyGift.put("token", queryDocumentSnapshot.getData().get("token"));

                        if (chatMyGift != null) {

                            //controllo prima se esiste myGiftName
                            //se esiste dico di inserire un altro nome
                            //se il regalo non esiste: creo il suo posto in chatMyGift
                            //quando un utente risponde ad uno dei miei regali: a lui creo la parte in chat con i suoi messaggi
                            //a me creo il sender, se non esiste già, per quel regalo

                            Map<String, Object> model = new HashMap<>();
                            model.put("username", queryDocumentSnapshot.getData().get("username"));
                            model.put("token", queryDocumentSnapshot.getData().get("token"));
                            model.put("chat", queryDocumentSnapshot.getData().get("chat"));

                            if (checkMyGiftExist(chatMyGift, giftName)){ //se esiste dico di inserire un altro nome
                                //Toast.makeText(mContext, "Hai già un regalo con questo nome, cambia nome", Toast.LENGTH_LONG).show();

                                Log.i("chatchat", "esiste già");
                                return;
                            }else{ //se il regalo non esiste: creo il suo posto in chatMyGift

                                List<Map<String, Object>> arrayMyGift = new ArrayList<>();

                                Map<String, Object> chatNewGift = new HashMap<>();
                                chatNewGift.put("myGiftName", giftName);
                                chatNewGift.put("arrayMyGift", arrayMyGift);

                                chatMyGift.add(chatNewGift);

                                model.put("chatMyGift", chatMyGift);

                                collectionReference.document(queryDocumentSnapshot.getId()).set(model);
                            }

//                            Log.i("chatchat", "Nuovo Gift");
//
//                            ArrayList<String> messagesArrayList = new ArrayList<>();
//
//                            ArrayList<Date> datesArrayList = new ArrayList<>();
//
//                            //questo lo devo creare quando qualcuno mi risponde (quindi in sendMessage)
//                            List<Map<String, Object>> gift = new ArrayList<>();
//                            Map<String, Object> myGiftInfo = new HashMap<>();
//                            myGiftInfo.put("sender", "");
//                            myGiftInfo.put("messages", messagesArrayList);
//                            myGiftInfo.put("timestamps", datesArrayList);
//
//                            gift.add(myGiftInfo);
//
//                            Map<String, Object> chatNewGift = new HashMap<>();
//                            chatNewGift.put("myGiftName", giftName);
//                            chatNewGift.put("arrayMyGift", gift);
//
//                            chatMyGift.add(chatNewGift);
//
//                            model.put("chatMyGift", chatMyGift);
//
//                            collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                        }

                    }
                }
            }
        });
    }

    public static void createSender(String username, final String sender, final String giftName){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Map<String, Object> model = new HashMap<>();
                        model.put("username", queryDocumentSnapshot.getData().get("username"));
                        model.put("token", queryDocumentSnapshot.getData().get("token"));
                        model.put("chat", queryDocumentSnapshot.getData().get("chat"));

                        List<Map<String, Object>> chatMyGift = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");

                        int indexG = 0;
                        for (int i = 0; i < chatMyGift.size(); i++){
                            Log.i("chatchat", "myGiftName: " + chatMyGift.get(i).get("myGiftName"));
                            if (chatMyGift.get(i).get("myGiftName").equals(giftName)){ //cerco l'indice del regalo che mi serve per myGiftList
                                indexG = i;
                                break;
                            }
                        }

                        List<Map<String, Object>> myGiftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(indexG).get("arrayMyGift");

                        if (!checkSenderExist(myGiftList, sender)){//se il sender non esiste: creo la sua map

                            List<String> messagesArrayList = new ArrayList<>();

                            List<Date> datesArrayList = new ArrayList<>();

                            //List<Map<String, Object>> gift = new ArrayList<>();
                            Map<String, Object> myGiftInfo = new HashMap<>();
                            myGiftInfo.put("sender", sender);
                            myGiftInfo.put("messages", messagesArrayList);
                            myGiftInfo.put("timestamps", datesArrayList);

                            myGiftList.add(myGiftInfo);
                            Log.i("chatchat", "myGiftList: " + myGiftList);

                            chatMyGift.get(indexG).put("myGiftName", giftName);
                            chatMyGift.get(indexG).put("arrayMyGift", myGiftList);

                            model.put("chatMyGift", chatMyGift);


                            collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                        }else{
                            return;
                        }

                    }
                }
            }
        });
    }

    public static void checkNameExist(String username, final String giftName, final Context mContext, final FirestoreCheckName listener){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){

                        List<Map<String, Object>> chatMyGift = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");

                        boolean exist = false;
                        for (int i = 0; i < chatMyGift.size(); i++){
                            if (chatMyGift.get(i).get("myGiftName").equals(giftName)){

                                Toast.makeText(mContext, "Hai già un regalo con questo nome, cambialo", Toast.LENGTH_SHORT).show();
                                exist = true;

                            }
                        }
                        listener.onReceiverRetrieve(exist);
                    }
                }
            }
        });
    }

    public static void deleteGiftFromDB(final String username, final String giftName){

        senderDeleteGift = new ArrayList<>();

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                        List<Map<String, Object>> chatMyGift = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");

                        for (int i = 0; i < chatMyGift.size(); i++) {
                            if (chatMyGift.get(i).get("myGiftName").equals(giftName)) {

                                List<Map<String, Object>> elementArraySenders = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(i).get("arrayMyGift");

                                for (Map<String, Object> list: elementArraySenders){
                                    senderDeleteGift.add((String) list.get("sender"));
                                }
                                Log.i("sendersender", senderDeleteGift + "");

                                //chatMyGift.remove(i);
                                chatMyGift.get(i).remove("myGiftName");
                                chatMyGift.get(i).remove("arrayMyGift");
                            }
                        }
                    }
                }
            }
        });

        for (String string: senderDeleteGift){
            collectionReference.whereEqualTo("username", string).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                            for (int i = 0; i < chat.size(); i++) {
                                if (chat.get(i).get("receiver").equals(username)) {

                                    List<Map<String, Object>> arrayElement = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("arrayGift");
                                    for (int j = 0; j < arrayElement.size(); j++){
                                        if (arrayElement.get(j).get("giftName").equals(giftName)){
                                            arrayElement.remove(j);
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            });
        }

    }


    //controllo se il mio regalo già esiste
    private static boolean checkMyGiftExist(List<Map<String, Object>> chatmyGift, String myGiftName){
        for (int i = 0; i < chatmyGift.size(); i++){
            String myGifts = (String) chatmyGift.get(i).get("myGiftName");
            if (myGifts.equals(myGiftName)){
                indexMyGift = i;
                //Log.i("chatchat", "GeofenceBroadcastReceiver boolean: " + myGifts + " " + indexMyGift);
                return true;
            }
        }
        return false;
    }

    //controllo se il sender già esiste
    private static boolean checkSenderExist(List<Map<String, Object>> arrayMyGift, String sender){
        for (int i = 0; i < arrayMyGift.size(); i++){
            String senders = (String) arrayMyGift.get(i).get("sender");
            if (senders.equals(sender)){
                indexSender = i;
                //Log.i("chatchat", "GeofenceBroadcastReceiver boolean: " + senders + " " + indexReceiver);
                return true;
            }
        }
        return false;
    }

    //controllo se il receiver già esiste
    private static boolean checkReceiverExist(List<Map<String, Object>> chat, String receiver){
        for (int i = 0; i < chat.size(); i++){
            String receivers = (String) chat.get(i).get("receiver");
            if (receivers.equals(receiver)){
                indexReceiver = i;
                //Log.i("chatchat", "GeofenceBroadcastReceiver boolean: " + receivers + " " + indexReceiver);
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
                //Log.i("chatchat", "GeofenceBroadcastReceiver boolean: " + giftInside + " " + indexGift);
                return true;
            }
        }
        return false;
    }

    public static void getArrayGift(final String username, final Context mContext, final FirestoreListener firestoreListener){

        listString = new ArrayList<>();
        final List<Map<String, Object>> myList = new ArrayList<>();

        final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){

                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                        if (chat != null){
                            for (int i = 0; i < chat.size(); i++){

                                List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("arrayGift");

                                for (int j = 0; j < giftList.size(); j++){

                                    List<String> messages = (List<String>) giftList.get(j).get("messages");
                                    List<Timestamp> timestamps = (List<Timestamp>) giftList.get(j).get("timestamps");
                                    //Log.i("chatchat",  "myListFor: " + giftList.get(j));

                                    getReceiverUsernameFromGift(queryDocumentSnapshot, (String) giftList.get(j).get("giftName"), messages.get(0), timestamps.get(0), giftList, messages, timestamps, i, j);

                                    myList.add(giftList.get(j));

                                }

                            }

                            firestoreListener.onChatRetrieve(myList);
                        }else{
                            Toast.makeText(mContext, "Non hai nessuna chat", Toast.LENGTH_LONG).show();
                        }


                    }
                }else {
                    firestoreListener.onTaskError(task.getException());
                }
            }
        });
    }


    public static void getReceiverUsernameFromGift(QueryDocumentSnapshot queryDocumentSnapshot, final String giftName, final String firstMessage, final Timestamp firstTimestamp, final List<Map<String, Object>> giftList, final List<String> messages, final List<Timestamp> timestamps, final int i, final int j){

        String receiverUsername = "";

        if (giftList.get(j).get("giftName").equals(giftName) && messages.get(0).equals(firstMessage) && timestamps.get(0).equals(firstTimestamp)){
            receiverUsername = (String) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("receiver");
            listString.add(receiverUsername);
            indexName = i;
            Log.i("chatchat", "ListName: " + listString);
        }
    }


    public static void getMyTimestamps2(final String username, final String sender, final String giftName, final FirestoreListener listener){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                    List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");

                    int indexR = 0; //indice del regalo
                    for (int i = 0; i < chat.size(); i++){
                        //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
                        if (chat.get(i).get("myGiftName").equals(giftName)){
                            indexR = i;
                            break;
                        }
                    }

                    //lista dei regali del destinatario
                    List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(indexR).get("arrayMyGift");

                    int indexG = 0; //indice del sender
                    for (int j = 0; j < giftList.size(); j++){
                        //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
                        if (giftList.get(j).get("sender").equals(sender)){
                            indexG = j;
                            break;
                        }
                    }

                    final List<Timestamp> myTimestamps = (List<Timestamp>) giftList.get(indexG).get("timestamps");
                    List<String> myMessages = (List<String>) giftList.get(indexG).get("messages");
                    listener.onDateRetrieve(myTimestamps, myMessages);
                }
            }
        });
    }



    //ritorna List con tutti i messaggi da username a receiver
//    public static void getMyTimestamps(final String username, final String sender, final String giftName, final FirestoreListener listener){
//
//        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
//        final CollectionReference collectionReference = firestoreDB.collection("users");
//        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()){
//                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
//                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");
//
//                        int indexR = 0; //indice del regalo
//                        for (int i = 0; i < chat.size(); i++){
//                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
//                            if (chat.get(i).get("myGiftName").equals(giftName)){
//                                indexR = i;
//                                break;
//                            }
//                        }
//
//                        //lista dei regali del destinatario
//                        List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(indexR).get("arrayMyGift");
//
//                        int indexG = 0; //indice del sender
//                        for (int j = 0; j < giftList.size(); j++){
//                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
//                            if (giftList.get(j).get("sender").equals(sender)){
//                                indexG = j;
//                                break;
//                            }
//                        }
//
//                        final List<Timestamp> myTimestamps = (List<Timestamp>) giftList.get(indexG).get("timestamps");
//                        List<String> myMessages = (List<String>) giftList.get(indexG).get("messages");
////                        provaSnapshotMy(username, indexR, indexG, new FirestoreListener() {
////                            @Override
////                            public void onMessageRetrieve(List<String> listenerMessages) {
////                                listener.onDateRetrieve(myTimestamps, listenerMessages);
////                            }
////
////                            @Override
////                            public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
////
////                            }
////
////                            @Override
////                            public void onChatRetrieve(List<Map<String, Object>> listenerChat) {
////
////                            }
////
////                            @Override
////                            public void onReceiverRetrieve(String receiver) {
////
////                            }
////
////                            @Override
////                            public void onTaskError(Exception taskException) {
////
////                            }
////                        });
//
//                        Log.i("chatchat", "sender: " + giftList.get(indexG).get("sender"));
//
//                        //listener.onDateRetrieve(myTimestamps, proveMy);
//                        listener.onDateRetrieve(myTimestamps, myMessages);
//                    }
//                }else {
//                    listener.onTaskError(task.getException());
//                }
//            }
//        });
//
//    }

    public static void getTimestamps2(final String username, final String receiver, final String giftName, final FirestoreListener listener){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {

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

                    final List<Timestamp> myTimestamps = (List<Timestamp>) giftList.get(indexG).get("timestamps");
                    List<String> myMessages = (List<String>) giftList.get(indexG).get("messages");

                    listener.onDateRetrieve(myTimestamps, myMessages);
                }
            }
        });
    }



    //ritorna List con i timestamps e i messaggi da username a receiver
//    public static void getTimestamps(final String username, final String receiver, final String giftName, final FirestoreListener listener){
//
//        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
//        final CollectionReference collectionReference = firestoreDB.collection("users");
//        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()){
//                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
//                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
//
//                        int indexR = 0; //indice del destinatario
//                        for (int i = 0; i < chat.size(); i++){
//                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
//                            if (chat.get(i).get("receiver").equals(receiver)){
//                                indexR = i;
//                                break;
//                            }
//                        }
//
//                        //lista dei regali del destinatario
//                        List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexR).get("arrayGift");
//
//                        int indexG = 0; //indice del regalo
//                        for (int j = 0; j < giftList.size(); j++){
//                            //Log.i("chatchat", "receiver: " + chat.get(i).get("receiver") + ", message: " + chat.get(i).get("messages") + ", timestamp: " + chat.get(i).get("timestamps"));
//                            if (giftList.get(j).get("giftName").equals(giftName)){
//                                indexG = j;
//                                break;
//                            }
//                        }
//
//                        final List<Timestamp> myTimestamps = (List<Timestamp>) giftList.get(indexG).get("timestamps");
//                        List<String> myMessages = (List<String>) giftList.get(indexG).get("messages");
//
////                        provaSnapshot(username, indexR, indexG, new FirestoreListener() {
////                            @Override
////                            public void onMessageRetrieve(List<String> listenerMessages) {
////
////                                listener.onDateRetrieve(myTimestamps, listenerMessages);
////                            }
////
////                            @Override
////                            public void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages) {
////
////                            }
////
////                            @Override
////                            public void onChatRetrieve(List<Map<String, Object>> listenerChat) {
////
////                            }
////
////                            @Override
////                            public void onReceiverRetrieve(String receiver) {
////
////                            }
////
////                            @Override
////                            public void onTaskError(Exception taskException) {
////
////                            }
////                        });
//
//                        listener.onDateRetrieve(myTimestamps, myMessages);
//                    }
//                }else{
//                    listener.onTaskError(task.getException());
//                }
//            }
//        });
//
//
//    }

    public static void getArrayMyGift(final String username, final Context mContext, final FirestoreListener firestoreListener){
        listNameMyGift = new ArrayList<>();
        final List<Map<String, Object>> myList = new ArrayList<>();

        final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){

                        List<Map<String, Object>> chatMyGift = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");

                        if (chatMyGift != null){
                            for (int i = 0; i < chatMyGift.size(); i++){

                                List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(i).get("arrayMyGift");

                                for (int j = 0; j < giftList.size(); j++){

                                    getGiftNameFromSender(queryDocumentSnapshot, (String) giftList.get(j).get("sender"), giftList,  i, j);

                                    myList.add(giftList.get(j));

                                }

                            }

                            firestoreListener.onChatRetrieve(myList);
                        }else{
                            Toast.makeText(mContext, "Non hai nessuna chat", Toast.LENGTH_LONG).show();
                        }


                    }
                }else {
                    firestoreListener.onTaskError(task.getException());
                }
            }
        });
    }

    public static void getArrayMyGift2(final String username, final Context mContext, final FirestoreListener firestoreListener){
        listNameMyGift = new ArrayList<>();
        final List<Map<String, Object>> myList = new ArrayList<>();

        final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                    List<Map<String, Object>> chatMyGift = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");

                    if (chatMyGift != null){
                        for (int i = 0; i < chatMyGift.size(); i++){

                            List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(i).get("arrayMyGift");

                            for (int j = 0; j < giftList.size(); j++){

                                getGiftNameFromSender(queryDocumentSnapshot, (String) giftList.get(j).get("sender"), giftList,  i, j);

                                myList.add(giftList.get(j));

                            }

                        }

                        firestoreListener.onChatRetrieve(myList);
                    }else{
                        Toast.makeText(mContext, "Non hai nessuna chat", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }

    public static void getGiftNameFromSender(QueryDocumentSnapshot queryDocumentSnapshot, final String sender, final List<Map<String, Object>> giftList, final int i, final int j){

        String myGiftName = "";

        if (giftList.get(j).get("sender").equals(sender)){
            myGiftName = (String) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(i).get("myGiftName");
            listNameMyGift.add(myGiftName);
            Log.i("chatchat", "ChatMyGift: " + listNameMyGift);
        }
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


//    public static void provaSnapshot(String username, final int indexR, final int indexG, final FirestoreListener firestoreListener){
//        prove = new ArrayList<>();
//
//        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
//        final CollectionReference collectionReference = firestoreDB.collection("users");
//        collectionReference.whereEqualTo("username", username).limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Log.w(TAG, "Listen failed.", error);
//                    return;
//                }
//
//                List<String> messages = new ArrayList<>();
//
//                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
//
//                    List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
//
//                    if (chat != null) {
//                        List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexR).get("arrayGift");
//                        List<String> myMessages = (List<String>) giftList.get(indexG).get("messages");
//                        messages.addAll(myMessages);
//                        Log.i("chatchat", "messages: " + messages);
//                        firestoreListener.onMessageRetrieve(messages);
//                    }
//
//                }
//            }
//        });
//    }
//
//    public static void provaSnapshotMy(String username, final int indexR, final int indexG, final FirestoreListener firestoreListener){
//        proveMy = new ArrayList<>();
//
//        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
//        final CollectionReference collectionReference = firestoreDB.collection("users");
//        collectionReference.whereEqualTo("username", username).limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Log.w(TAG, "Listen failed.", error);
//                    return;
//                }
//
//                List<String> messages = new ArrayList<>();
//
//                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
//
//                    List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");
//
//
//                    if (chat != null) {
//                        List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(indexR).get("arrayMyGift");
//                        List<String> myMessages = (List<String>) giftList.get(indexG).get("messages");
//                        messages.addAll(myMessages);
//                        Log.i("chatchat", "messages1: " + messages);
//                        firestoreListener.onMessageRetrieve(messages);
//                    }
//
//                }
//            }
//        });
//    }

}
