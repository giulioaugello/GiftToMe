package com.LAM.GiftToMe.FCMFirebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.LAM.GiftToMe.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG = "FCMTAG";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.i(TAG, "Refreshed token: " + token);

        if(MainActivity.isLogged) {
            DBFirestore.saveTokenDB(MainActivity.userName, token);
        }

        SharedPreferences prefs = getSharedPreferences("FCMTokenPref", Context.MODE_PRIVATE);
        prefs.edit().putString("FCMToken",token).apply();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }
}
