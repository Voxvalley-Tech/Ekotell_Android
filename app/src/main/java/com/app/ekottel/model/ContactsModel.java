package com.app.ekottel.model;

public class ContactsModel {
    String contactName,contactNumber,conatctID,contactPicID;
    boolean isContactSelected=false;

    public boolean isContactSelected() {
        return isContactSelected;
    }

    public void setContactSelected(boolean contactSelected) {
        isContactSelected = contactSelected;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getConatctID() {
        return conatctID;
    }

    public void setConatctID(String conatctID) {
        this.conatctID = conatctID;
    }

    public String getContactPicID() {
        return contactPicID;
    }

    public void setContactPicID(String contactPicID) {
        this.contactPicID = contactPicID;
    }
}
