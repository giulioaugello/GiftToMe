package com.LAM.GiftToMe.FCMFirebase;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class Chat {

    private static final String TAG = "TAGCHAT";

    public static void sendMessage(String username){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        //updateChat(queryDocumentSnapshot.getId(), queryDocumentSnapshot.getData(), message, issuer);
                        List<Map<String, Object>> chat = (List<Map<String, Object>>) queryDocumentSnapshot.get("chat");
                        Log.i("chatchat", "List " + chat);
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
