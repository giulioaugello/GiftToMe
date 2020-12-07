package com.LAM.GiftToMe.FCMFirebase;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface FirestoreListener {

    void onMessageRetrieve(List<String> listenerMessages);

    void onDateRetrieve(List<Date> listenerTimestamps);

    void onChatRetrieve(List<Map<String, Object>> listenerChat);

    void onReceiverRetrieve(String receiver);

    void onTaskError(Exception taskException);
}
