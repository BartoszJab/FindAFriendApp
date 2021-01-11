package com.uwb.findafriendapp.classes;

public class Chat {

    private String senderID;
    private String receiverID;
    private String message;

    public Chat() { }

    public Chat(String senderID, String receiverID, String message) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.message = message;
    }

    public Chat(String senderID, String message) {
        this.senderID = senderID;
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String sender) {
        this.senderID = sender;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiver) {
        this.receiverID = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
