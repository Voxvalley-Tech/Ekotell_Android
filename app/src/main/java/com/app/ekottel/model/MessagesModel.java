package com.app.ekottel.model;

/**
 * Created by ramesh.u on 8/18/2017.
 */

public class MessagesModel {

    private String name;
    private String number;
    private String lastmessage;
    private String msg_type;
    private String dateStr;
    private String timeStr;
    private String contactID;
    private String fileName;
    private boolean isProfilePicAvailable=false;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isProfilePicAvailable() {
        return isProfilePicAvailable;
    }

    public void setProfilePicAvailable(boolean profilePicAvailable) {
        isProfilePicAvailable = profilePicAvailable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLastmessage() {
        return lastmessage;
    }

    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public String getContactID() {
        return contactID;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
    }
}
