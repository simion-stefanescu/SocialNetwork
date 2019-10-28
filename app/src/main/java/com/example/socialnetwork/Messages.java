package com.example.socialnetwork;

public class Messages {

    public String data, time, type, messages, from;

    public Messages(){

    }

    public Messages(String data, String time, String type, String messages, String from) {
        this.data = data;
        this.time = time;
        this.type = type;
        this.messages = messages;
        this.from = from;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
