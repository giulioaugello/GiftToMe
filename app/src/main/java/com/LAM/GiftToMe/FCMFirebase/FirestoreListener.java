package com.LAM.GiftToMe.FCMFirebase;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface FirestoreListener {

    void onMessageRetrieve(List<String> listenerMessages);

    //per recuperare messaggi e timestamps
    void onDateRetrieve(List<Timestamp> listenerTimestamps, List<String> listenerMessages);

    //per recuperare le chat
    void onChatRetrieve(List<Map<String, Object>> listenerChat);

    void onReceiverRetrieve(String receiver);

    void onTaskError(Exception taskException);
}
