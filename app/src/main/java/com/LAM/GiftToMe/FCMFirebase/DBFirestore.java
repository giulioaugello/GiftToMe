package com.LAM.GiftToMe.FCMFirebase;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class DBFirestore {

    private static String TAG = "FCMTAG";

    public static void saveUserDB(String username, String token){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestoreDB.collection("users");
        ModelUser modelUser = new ModelUser(username, token);
        collectionReference.add(modelUser);

    }

    public static void getToken(String username, final ArrayList<String> message, final Context context){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    String token = "";
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.i(TAG, document.getId() + " => " + document.getData());
                        //document.get("token").toString();
                        token = document.getString("token");
                    }
                    Log.i("FCMTAG", "eccomi " + token);
                    FCMNotification.sendFCMNotification(message, token, context);
                } else {
                    Log.i(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    public static void checkIfExist(final String username, final String token){

        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestoreDB.collection("users");
        collectionReference.whereEqualTo("username", username).whereEqualTo("token", token).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().size() == 0 ){
                        Log.i(TAG, "User not Exists " + username + " " + token);
                        saveUserDB(username, token);
                    }else{
                        Log.i(TAG, "User already Exists");
                    }
                }
            }
        });
    }

}
