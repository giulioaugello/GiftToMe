package com.LAM.GiftToMe.UsefulClass;

public class MyReply {

    private String tweetId,tweetReplyId,replyId,sender,targetId,receiverName,message;

    public MyReply() {
    }

    public MyReply(String tweetId, String tweetReplyId,String replyId, String sender, String targetId, String receiverName, String message) {
        this.tweetId = tweetId;
        this.tweetReplyId = tweetReplyId;
        this.replyId = replyId;
        this.sender = sender;
        this.targetId = targetId;
        this.receiverName = receiverName;
        this.message = message;
    }

    public String getTweetId() {
        return tweetId;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public String getTweetReplyId() {
        return tweetReplyId;
    }

    public void setTweetReplyId(String tweetReplyId) {
        this.tweetReplyId = tweetReplyId;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
