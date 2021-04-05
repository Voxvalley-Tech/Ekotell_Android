package com.app.ekottel.model;

/**
 * This model class that stores chat information
 *
 * @author Ramesh U and Ramesh Reddy
 */

public class ChatConversationModel {

    private long timeStamp;
    private int isSender;
    private String message;
    private String filePath;
    private String thumbNail="";
    private int messageType;
    private String chatID;
    private int chatStatus,chatThumbStatus;
    private String destinationNumber;
    private boolean isFileTransferInProgress = false;
    private int fileTransferPercentage = 0;
    private boolean isUploadStoped=false;
    private String fileName;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isUploadStoped() {
        return isUploadStoped;
    }

    public void setUploadStoped(boolean uploadStoped) {
        isUploadStoped = uploadStoped;
    }

    public int getChatThumbStatus() {
        return chatThumbStatus;
    }

    public void setChatThumbStatus(int chatThumbStatus) {
        this.chatThumbStatus = chatThumbStatus;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getIsSender() {
        return isSender;
    }

    public void setIsSender(int isSender) {
        this.isSender = isSender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(String thumbNail) {
        this.thumbNail = thumbNail;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public int getChatStatus() {
        return chatStatus;
    }

    public void setChatStatus(int chatStatus) {
        this.chatStatus = chatStatus;
    }

    public String getDestinationNumber() {
        return destinationNumber;
    }

    public void setDestinationNumber(String destinationNumber) {
        this.destinationNumber = destinationNumber;
    }


    public boolean isFileTransferInProgress() {
        return isFileTransferInProgress;
    }

    public void setFileTransferInProgress(boolean fileTransferInProgress) {
        isFileTransferInProgress = fileTransferInProgress;
    }

    public int getFileTransferPercentage() {
        return fileTransferPercentage;
    }

    public void setFileTransferPercentage(int fileTransferPercentage) {
        this.fileTransferPercentage = fileTransferPercentage;
    }
}
