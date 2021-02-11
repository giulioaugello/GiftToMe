package com.LAM.GiftToMe.FCMFirebase;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.LAM.GiftToMe.Fragment.ChatFragment;
import com.LAM.GiftToMe.MainActivity;
import com.LAM.GiftToMe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.LAM.GiftToMe.MainActivity.replyChatFragmentTag;
import static com.LAM.GiftToMe.MainActivity.settingsFragmentTag;

public class Chat {

    private static final String TAG = "ChatTAG";
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
    //voglio inviare un messaggio da username a receiver con testo reply
    public static void sendMessage(final String username, final String receiver, final String reply, final String giftName){

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

                        //controlla se il receiver esiste e se esiste controlla che il regalo non c'è già
                        updateChat(chat, model, receiver, reply, giftName, queryDocumentSnapshot);

                    }
                }
            }
        });

    }

    //invio messaggio tramite regali
    //voglio inviare un messaggio da username a receiver con testo reply
    public static void sendMessageFromGift(final String username, final String receiver, final String reply, final String giftName){

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

                Log.i(TAG, "Nuova Chat");

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

                Log.i(TAG, "Chat già esistente");

                List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexReceiver).get("arrayGift");


                if (checkGiftExist(giftList, giftName)){ //se ho già una chat per questo regalo

                    Log.i(TAG, "Chat con regalo che già ho");

                    ArrayList<String> messagesArrayList = (ArrayList<String>) giftList.get(indexGift).get("messages");
                    messagesArrayList.add(reply);

                    ArrayList<Date> datesArrayList = (ArrayList<Date>) giftList.get(indexGift).get("timestamps");
                    Date date = new Date();
                    datesArrayList.add(date);

                    giftList.get(indexGift).put("messages", messagesArrayList);
                    giftList.get(indexGift).put("timestamps", datesArrayList);

                    chat.get(indexReceiver).put("receiver", receiver);
                    chat.get(indexReceiver).put("arrayGift", giftList);

                    model.put("chat", chat);
                    model.put("chatMyGift", queryDocumentSnapshot.getData().get("chatMyGift"));

                    collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                }else{ //se non ho una chat per questo regalo

                    Log.i(TAG, "Chat con Nuovo Regalo");

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

            }

        }

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

                                Log.i(TAG, "esiste già");
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

                        }

                    }
                }
            }
        });
    }

    //crea il sender se non esiste
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

                            if (chatMyGift.get(i).get("myGiftName").equals(giftName)){ //cerco l'indice del regalo che mi serve per myGiftList
                                indexG = i;
                                break;
                            }
                        }

                        List<Map<String, Object>> myGiftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(indexG).get("arrayMyGift");

                        if (!checkSenderExist(myGiftList, sender)){//se il sender non esiste: creo la sua map

                            List<String> messagesArrayList = new ArrayList<>();

                            List<Date> datesArrayList = new ArrayList<>();

                            Map<String, Object> myGiftInfo = new HashMap<>();
                            myGiftInfo.put("sender", sender);
                            myGiftInfo.put("messages", messagesArrayList);
                            myGiftInfo.put("timestamps", datesArrayList);

                            myGiftList.add(myGiftInfo);

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

    //controlla se esiste un regalo con questo nome
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

                                Toast.makeText(mContext, mContext.getResources().getString(R.string.same_name), Toast.LENGTH_SHORT).show();
                                exist = true;

                            }
                        }
                        listener.onReceiverRetrieve(exist);
                    }
                }
            }
        });
    }

    //cancella il regalo che elimino dal fragment anche dal db
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

                                chatMyGift.remove(i);
                                Map<String, Object> model = new HashMap<>();
                                model.put("username", queryDocumentSnapshot.getData().get("username"));
                                model.put("token", queryDocumentSnapshot.getData().get("token"));
                                model.put("chat", queryDocumentSnapshot.getData().get("chat"));
                                model.put("chatMyGift", chatMyGift);
                                collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                                //toglie il regalo dall' array dei sender e elimina la chat
                                deleteFromArrayChat(senderDeleteGift, username, giftName);

                                break;
                            }
                        }
                    }
                }
            }
        });

    }

    //toglie il regalo dall' array dei sender e elimina la chat
    public static void deleteFromArrayChat(List<String> list, final String username, final String giftName){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        for (String string: list){
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

                                            chat.get(i).remove("arrayGift");
                                            chat.get(i).put("arrayGift", arrayElement);

                                            Map<String, Object> model = new HashMap<>();
                                            model.put("username", queryDocumentSnapshot.getData().get("username"));
                                            model.put("token", queryDocumentSnapshot.getData().get("token"));
                                            model.put("chat", chat);
                                            model.put("chatMyGift", queryDocumentSnapshot.getData().get("chatMyGift"));
                                            collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                                            break;
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

    //modifica il nome del regalo nel db quando faccio edit in mygiftadapter
    public static void modifyGiftName(final String username, final String giftName, final String newGiftName){

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

                                chatMyGift.get(i).remove("myGiftName");
                                chatMyGift.get(i).put("myGiftName", newGiftName);

                                Map<String, Object> model = new HashMap<>();
                                model.put("username", queryDocumentSnapshot.getData().get("username"));
                                model.put("token", queryDocumentSnapshot.getData().get("token"));
                                model.put("chat", queryDocumentSnapshot.getData().get("chat"));
                                model.put("chatMyGift", chatMyGift);
                                collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                                //toglie il regalo dall' array dei sender e elimina la chat
                                modifyGiftNameUpdate(senderDeleteGift, username, giftName, newGiftName);

                                break;
                            }
                        }
                    }
                }
            }
        });

    }

    //modifica il nome del regalo in tutte le chat aperte se ce ne sono
    public static void modifyGiftNameUpdate(List<String> list, final String username, final String giftName, final String newGiftName){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        for (String string: list){
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

                                            arrayElement.get(j).remove("giftName");
                                            arrayElement.get(j).put("giftName", newGiftName);

                                            chat.get(i).remove("arrayGift");
                                            chat.get(i).put("arrayGift", arrayElement);

                                            Map<String, Object> model = new HashMap<>();
                                            model.put("username", queryDocumentSnapshot.getData().get("username"));
                                            model.put("token", queryDocumentSnapshot.getData().get("token"));
                                            model.put("chat", chat);
                                            model.put("chatMyGift", queryDocumentSnapshot.getData().get("chatMyGift"));
                                            collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                                            break;
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



    //controlla se la chat già esiste
    public static void checkIfChatExist(final String username, final String receiver, final String giftName, final FirestoreCheckName firestoreCheckName){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                        boolean exist = false;

                        for (int i = 0; i < chat.size(); i++) {
                            if (chat.get(i).get("receiver").equals(receiver)) {

                                List<Map<String, Object>> arrayElement = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("arrayGift");

                                for (int j = 0; j < arrayElement.size(); j++){
                                    if (arrayElement.get(j).get("giftName").equals(giftName)){
                                        exist = true;
                                        break;
                                    }
                                }

                            }
                        }
                        firestoreCheckName.onReceiverRetrieve(exist);
                    }
                }
            }
        });
    }

    //controllo se il mio regalo già esiste
    private static boolean checkMyGiftExist(List<Map<String, Object>> chatmyGift, String myGiftName){
        for (int i = 0; i < chatmyGift.size(); i++){
            String myGifts = (String) chatmyGift.get(i).get("myGiftName");
            if (myGifts.equals(myGiftName)){
                indexMyGift = i;
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
                return true;
            }
        }
        return false;
    }

    //ritorna le chat delle risposte usato in ChatFragment
//    public static void getArrayGift(final String username, final Context mContext, final FirestoreListener firestoreListener){
//
//        listString = new ArrayList<>();
//        final List<Map<String, Object>> myList = new ArrayList<>();
//
//        final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
//        final CollectionReference collectionReference = firestoreDB.collection("users");
//        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()){
//                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
//
//                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
//
//                        if (chat != null){
//                            for (int i = 0; i < chat.size(); i++){
//
//                                List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("arrayGift");
//
//                                for (int j = 0; j < giftList.size(); j++){
//
//                                    List<String> messages = (List<String>) giftList.get(j).get("messages");
//                                    List<Timestamp> timestamps = (List<Timestamp>) giftList.get(j).get("timestamps");
//
//                                    getReceiverUsernameFromGift(queryDocumentSnapshot, (String) giftList.get(j).get("giftName"), messages.get(0), timestamps.get(0), giftList, messages, timestamps, i, j);
//
//                                    myList.add(giftList.get(j));
//
//                                }
//
//                            }
//
//                            firestoreListener.onChatRetrieve(myList);
//                        }else{
//                            Toast.makeText(mContext, mContext.getResources().getString(R.string.no_chat), Toast.LENGTH_LONG).show();
//                        }
//
//
//                    }
//                }else {
//                    firestoreListener.onTaskError(task.getException());
//                }
//            }
//        });
//    }

    //ritorna timestamps da miei regali
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

                        if (chat.get(i).get("myGiftName").equals(giftName)){
                            indexR = i;
                            break;
                        }
                    }

                    //lista dei regali del destinatario
                    List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(indexR).get("arrayMyGift");

                    int indexG = 0; //indice del sender
                    for (int j = 0; j < giftList.size(); j++){

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
//    public static void getMyTimestamps2(final String username, final String sender, final String giftName, Activity activity, final FirestoreListener listener){
//        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
//        Query collectionReference = firestoreDB.collection("users").whereEqualTo("username", username).limit(1);
//        ListenerRegistration listenerRegistration = collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Log.w(TAG, "Listen failed.", error);
//                    return;
//                }
//
//                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
//                    List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift");
//
//                    int indexR = 0; //indice del regalo
//                    for (int i = 0; i < chat.size(); i++){
//
//                        if (chat.get(i).get("myGiftName").equals(giftName)){
//                            indexR = i;
//                            break;
//                        }
//                    }
//
//                    //lista dei regali del destinatario
//                    List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chatMyGift")).get(indexR).get("arrayMyGift");
//
//                    int indexG = 0; //indice del sender
//                    for (int j = 0; j < giftList.size(); j++){
//
//                        if (giftList.get(j).get("sender").equals(sender)){
//                            indexG = j;
//                            break;
//                        }
//                    }
//
//                    final List<Timestamp> myTimestamps = (List<Timestamp>) giftList.get(indexG).get("timestamps");
//                    List<String> myMessages = (List<String>) giftList.get(indexG).get("messages");
//                    listener.onDateRetrieve(myTimestamps, myMessages);
//                }
//            }
//        });
//
//        if (!MainActivity.activeFragment.equals(((AppCompatActivity)activity).getSupportFragmentManager().findFragmentByTag(replyChatFragmentTag))){
//            listenerRegistration.remove();
//        }
//    }

    //ritorna timestamps da risposte
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

                        if (chat.get(i).get("receiver").equals(receiver)){
                            indexR = i;
                            break;
                        }
                    }

                    //lista dei regali del destinatario
                    List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexR).get("arrayGift");

                    int indexG = 0; //indice del regalo
                    for (int j = 0; j < giftList.size(); j++){

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

//    public static void getTimestamps2(final String username, final String receiver, final String giftName, Activity activity, final FirestoreListener listener){
//        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
//        Query collectionReference = firestoreDB.collection("users").whereEqualTo("username", username).limit(1);
//        ListenerRegistration listenerRegistration = collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null) {
//                    Log.w(TAG, "Listen failed.", error);
//                    return;
//                }
//
//                for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
//
//                    List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
//
//                    int indexR = 0; //indice del destinatario
//                    for (int i = 0; i < chat.size(); i++){
//
//                        if (chat.get(i).get("receiver").equals(receiver)){
//                            indexR = i;
//                            break;
//                        }
//                    }
//
//                    //lista dei regali del destinatario
//                    List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(indexR).get("arrayGift");
//
//                    int indexG = 0; //indice del regalo
//                    for (int j = 0; j < giftList.size(); j++){
//
//                        if (giftList.get(j).get("giftName").equals(giftName)){
//                            indexG = j;
//                            break;
//                        }
//                    }
//
//                    final List<Timestamp> myTimestamps = (List<Timestamp>) giftList.get(indexG).get("timestamps");
//                    List<String> myMessages = (List<String>) giftList.get(indexG).get("messages");
//
//                    listener.onDateRetrieve(myTimestamps, myMessages);
//                }
//            }
//        });
//
//        if (!MainActivity.activeFragment.equals(((AppCompatActivity)activity).getSupportFragmentManager().findFragmentByTag(replyChatFragmentTag))){
//            listenerRegistration.remove();
//        }
//    }

    //ritorna le chat delle risposte usato in ChatFragment
    public static void getArrayGift2(final String username, final Context mContext, final FirestoreListener firestoreListener){
        listString = new ArrayList<>();

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
                    List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                    if (chat != null){
                        for (int i = 0; i < chat.size(); i++){

                            List<Map<String, Object>> giftList = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("arrayGift");

                            for (int j = 0; j < giftList.size(); j++){

                                List<String> messages = (List<String>) giftList.get(j).get("messages");
                                List<Timestamp> timestamps = (List<Timestamp>) giftList.get(j).get("timestamps");

                                getReceiverUsernameFromGift(queryDocumentSnapshot, (String) giftList.get(j).get("giftName"), messages.get(0), timestamps.get(0), giftList, messages, timestamps, i, j);

                                myList.add(giftList.get(j));

                            }

                        }

                        firestoreListener.onChatRetrieve(myList);
                    }else{
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.no_chat), Toast.LENGTH_LONG).show();
                    }
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
        }
    }

    //ritorna chat dei miei regali
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
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.no_chat), Toast.LENGTH_LONG).show();
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
        }
    }


    public static void changeFirstMessage(final String username, final String receiver, final String giftName, final String oldMessage, final String newMessage){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                        for (int i = 0; i < chat.size(); i++) {
                            if (chat.get(i).get("receiver").equals(receiver)) {

                                List<Map<String, Object>> arrayElement = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("arrayGift");
                                for (int j = 0; j < arrayElement.size(); j++){
                                    if (arrayElement.get(j).get("giftName").equals(giftName)){

                                        List<String> myMessages = (List<String>) arrayElement.get(j).get("messages");
                                        String first = myMessages.get(0);
                                        Log.i("receiverreceiver", myMessages.get(0) + ", " + first + ", " + oldMessage + ", " + newMessage);

                                        first = newMessage;
                                        ArrayList<String> messagesArrayList = new ArrayList<>();
                                        messagesArrayList.add(newMessage);
                                        Log.i("receiverreceiver", myMessages.get(0) + ", " + oldMessage + ", " + newMessage + " ... " + first);

                                        arrayElement.get(j).remove("messages");

                                        arrayElement.get(j).put("messages", messagesArrayList);
                                        Log.i("receiverreceiver", messagesArrayList + "");

                                        chat.get(i).remove("arrayGift");
                                        chat.get(i).put("arrayGift", arrayElement);

                                        Map<String, Object> model = new HashMap<>();
                                        model.put("username", queryDocumentSnapshot.getData().get("username"));
                                        model.put("token", queryDocumentSnapshot.getData().get("token"));
                                        model.put("chat", chat);
                                        model.put("chatMyGift", queryDocumentSnapshot.getData().get("chatMyGift"));
                                        collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                                        break;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        });
    }

    public static void deleteFirstMessage(final String username, final String receiver, final String giftName){
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");

                        for (int i = 0; i < chat.size(); i++) {
                            if (chat.get(i).get("receiver").equals(receiver)) {

                                List<Map<String, Object>> arrayElement = (List<Map<String, Object>>) ((List<Map<String, Object>>) queryDocumentSnapshot.get("chat")).get(i).get("arrayGift");
                                for (int j = 0; j < arrayElement.size(); j++){
                                    if (arrayElement.get(j).get("giftName").equals(giftName)){

                                        arrayElement.remove(j);

                                        chat.get(i).remove("arrayGift");
                                        chat.get(i).put("arrayGift", arrayElement);

                                        Map<String, Object> model = new HashMap<>();
                                        model.put("username", queryDocumentSnapshot.getData().get("username"));
                                        model.put("token", queryDocumentSnapshot.getData().get("token"));
                                        model.put("chat", chat);
                                        model.put("chatMyGift", queryDocumentSnapshot.getData().get("chatMyGift"));
                                        collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                                        deleteSender(receiver, username, giftName);

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public static void deleteSender(String username, final String sender, final String giftName){
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

                                for (int j = 0; j < elementArraySenders.size(); j++){
                                    if (elementArraySenders.get(j).get("sender").equals(sender)){

                                        elementArraySenders.remove(j);

                                        chatMyGift.get(i).remove("arrayMyGift");
                                        chatMyGift.get(i).put("arrayMyGift", elementArraySenders);

                                        Map<String, Object> model = new HashMap<>();
                                        model.put("username", queryDocumentSnapshot.getData().get("username"));
                                        model.put("token", queryDocumentSnapshot.getData().get("token"));
                                        model.put("chat", queryDocumentSnapshot.getData().get("chat"));
                                        model.put("chatMyGift", chatMyGift);
                                        collectionReference.document(queryDocumentSnapshot.getId()).set(model);

                                        break;
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
