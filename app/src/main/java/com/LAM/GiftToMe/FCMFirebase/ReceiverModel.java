package com.LAM.GiftToMe.FCMFirebase;

import java.util.Date;
import java.util.List;

public class ReceiverModel {

    private String username, giftName;
    private List<String> messages;
    private List<Date> timestamps;

    public ReceiverModel(String username, String giftName, List<String> messages, List<Date> timestamps){
        this.username = username;
        this.giftName = giftName;
        this.messages = messages;
        this.timestamps = timestamps;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<Date> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(List<Date> timestamps) {
        this.timestamps = timestamps;
    }
}
