package com.app.ekottel.model;

import java.util.ArrayList;

public class ChatFtNotifiationModel {


    String title,description;
    boolean isempty = true;
    ArrayList<String> messageList = new ArrayList<>();

    public boolean getisEmpty() {
        return isempty;
    }

    public void setIsempty(boolean isempty) {
        this.isempty = isempty;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getMessageList() {
        return messageList;
    }

    public void setMessageList(ArrayList<String> messageList) {

for(String message: messageList) {
    this.messageList.add(message);
}
    }


}
