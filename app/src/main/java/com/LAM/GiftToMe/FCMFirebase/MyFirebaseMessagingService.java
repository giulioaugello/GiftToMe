package com.LAM.GiftToMe.FCMFirebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.LAM.GiftToMe.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "FCMTAG";
    public MyFirebaseMessagingService() {
    }

    @Override
    public void onNewToken(String token) {
        Log.i(TAG, "Refreshed token: " + token);

        if(MainActivity.isLogged) {
            DBFirestore.saveUserDB(MainActivity.userName, token);
        }

        SharedPreferences prefs = getSharedPreferences("FCMTokenPref", Context.MODE_PRIVATE);
        prefs.edit().putString("FCMToken", token).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.i(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.i(TAG, "Message data payload: " + remoteMessage.getData());
            FCMNotification fcmNotification = new FCMNotification(this);
            fcmNotification.displayFCMNotification(remoteMessage);

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.i(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
