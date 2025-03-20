package com.example.letschat.Models;

public class MessageModel {
    private String message,uid, time,date,image,receiverId,messageId,senderName,type,pdf,docx,isseen;
    public MessageModel() {

    }
    public MessageModel(String message, String uid, String time, String date, String receiverId, String messageId,String type,String isseen) {
        this.message = message;
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.receiverId = receiverId;
        this.messageId = messageId;
        this.type=type;
        this.isseen=isseen;
    }
    public MessageModel(String message, String uid, String time) {
        this.message = message;
        this.uid = uid;
        this.time = time;
    }

    public String getIsseen() {
        return isseen;
    }

    public void setIsseen(String isseen) {
        this.isseen = isseen;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getDocx() {
        return docx;
    }

    public void setDocx(String docx) {
        this.docx = docx;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}