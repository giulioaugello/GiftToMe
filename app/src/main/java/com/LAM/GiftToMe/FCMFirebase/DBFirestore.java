package com.LAM.GiftToMe.FCMFirebase;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class DBFirestore {

    private static String TAG = "FCMTAG";

    public static void saveUserDB(String username, String token){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestoreDB.collection("users");

        /////////////////////////////////////////////////////////////////////////
        //CHAT MY GIFT

//        String[] messagesMyGift = {};
//        Date[] timestampsMyGift = {};
//
//        List<Map<String, Object>> myGift = new ArrayList<>();
//
//        Map<String, Object> myGiftInfo = new HashMap<>();
//        myGiftInfo.put("sender", "");
//        myGiftInfo.put("messages", Arrays.asList(messagesMyGift));
//        myGiftInfo.put("timestamps", Arrays.asList(timestampsMyGift));
//        myGift.add(myGiftInfo);

        List<Map<String, Object>> chatListMyGift = new ArrayList<>();

//        Map<String, Object> chatMapMyGift = new HashMap<>();
//        chatMapMyGift.put("myGiftName", "");
//        chatMapMyGift.put("arrayMyGift", myGift);
//        chatListMyGift.add(chatMapMyGift);

        /////////////////////////////////////////////////////////////
        //CHAT OTHER GIFT

//        String[] messages = {};
//        Date[] timestamps = {};
//
//        List<Map<String, Object>> gift = new ArrayList<>();
//
//        Map<String, Object> giftInfo = new HashMap<>();
//        giftInfo.put("giftName", "");
//        giftInfo.put("messages", Arrays.asList(messages));
//        giftInfo.put("timestamps", Arrays.asList(timestamps));
//        gift.add(giftInfo);

        List<Map<String, Object>> chatList = new ArrayList<>();

//        Map<String, Object> chatMap = new HashMap<>();
//        chatMap.put("receiver", "");
//        chatMap.put("arrayGift", gift);
//        chatList.add(chatMap);

        /////////////////////////////////////////////////////////////
        //MODEL

        Map<String, Object> model = new HashMap<>();
        String[] tokens = {token};
        model.put("username", username);
        model.put("token", Arrays.asList(tokens));
        model.put("chat", chatList);
        model.put("chatMyGift", chatListMyGift);

        collectionReference.add(model);

    }

    public static void getToken(String username, final ArrayList<String> message, final Context context){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.i(TAG, document.getId() + " => " + document.getData());
                        //document.get("token").toString();
                        ArrayList<String> tokens = (ArrayList<String>) document.getData().get("token");
                        for(String token: tokens){
                            FCMNotification.sendFCMNotification(message, token, context);
                        }
                    }
                    //Log.i("FCMTAG", "eccomi " + token);
                } else {
                    Log.i(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    public static void checkIfExist(final String username, final String token){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().size() == 0){
                        Log.i(TAG, "User not Exists " + username + " " + token);
                        saveUserDB(username, token);
                    }else{
                        Log.i(TAG, "User already Exists");
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            updateTokens(queryDocumentSnapshot.getId(), queryDocumentSnapshot.getData(), token);
                        }
                    }
                }
            }
        });
    }

    public static void updateTokens(String id, Map<String, Object> data, String token){

        if (data.containsKey("token")){
            ArrayList<String> tokens = (ArrayList<String>) data.get("token");
            if (!tokens.contains(token)){
                tokens.add(token);
                FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
                CollectionReference collectionReference = firestoreDB.collection("users");
                Map<String, Object> model = new HashMap<>();
                model.put("username", data.get("username"));
                model.put("token", tokens);
                collectionReference.document(id).set(model);
            }
        }
    }

    public static void removeToken(final String username, final String token){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        updateTokensLogout(queryDocumentSnapshot.getId(), queryDocumentSnapshot.getData(), token);
                    }
                }
            }
        });
    }

    public static void updateTokensLogout(String id, Map<String, Object> data, String token){
        if (data.containsKey("token")){
            ArrayList<String> tokens = (ArrayList<String>) data.get("token");
            if (tokens.contains(token)){
                tokens.remove(token);
                FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
                CollectionReference collectionReference = firestoreDB.collection("users");
                Map<String, Object> model = new HashMap<>();
                model.put("username", data.get("username"));
                model.put("token", tokens);
                collectionReference.document(id).set(model);
            }
        }
    }

}
