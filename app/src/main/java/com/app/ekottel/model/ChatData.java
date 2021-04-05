package com.app.ekottel.model;
public class ChatData {

    private String destinationNumber = "", chatId, message = "", timestamp = "", contentType = "", fileName = "", uploadFilePath = "", fileSize = "", caption = "", thumbnail = "", downloadFilePath = "";
    private boolean isSender, isGroupMessage, isMediaDownloadingOrUploading = false, isOriginalImageLoaded = false, isChatSelected = false, isUnreadMessage = false, isChancelClicked = false;
    private int messageStatus, messageType, mediaUploadingOrDownloadingPercentage, audioProgress, isMultiDeviceMessage, adapterPosition;

    public boolean isChancelClicked() {
        return isChancelClicked;
    }

    public void setChancelClicked(boolean chancelClicked) {
        isChancelClicked = chancelClicked;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    public String getDownloadFilePath() {
        return downloadFilePath;
    }

    public void setDownloadFilePath(String downloadFilePath) {
        this.downloadFilePath = downloadFilePath;
    }

    public boolean isGroupMessage() {
        return isGroupMessage;
    }

    public void setGroupMessage(boolean groupMessage) {
        isGroupMessage = groupMessage;
    }

    public int getIsMultiDeviceMessage() {
        return isMultiDeviceMessage;
    }

    public void setIsMultiDeviceMessage(int isMultiDeviceMessage) {
        this.isMultiDeviceMessage = isMultiDeviceMessage;
    }

    public String getDestinationNumber() {
        return destinationNumber;
    }

    public void setDestinationNumber(String destinationNumber) {
        this.destinationNumber = destinationNumber;
    }

    public boolean isUnreadMessage() {
        return isUnreadMessage;
    }

    public void setUnreadMessage(boolean unreadMessage) {
        isUnreadMessage = unreadMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    public boolean isMediaDownloadingOrUploading() {
        return isMediaDownloadingOrUploading;
    }

    public void setMediaDownloadingOrUploading(boolean mediaDownloadingOrUploading) {
        isMediaDownloadingOrUploading = mediaDownloadingOrUploading;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getAudioProgress() {
        return audioProgress;
    }

    public void setAudioProgress(int audioProgress) {
        this.audioProgress = audioProgress;
    }

    public int getMediaUploadingOrDownloadingPercentage() {
        return mediaUploadingOrDownloadingPercentage;
    }

    public void setMediaUploadingOrDownloadingPercentage(int mediaUploadingOrDownloadingPercentage) {
        this.mediaUploadingOrDownloadingPercentage = mediaUploadingOrDownloadingPercentage;
    }

    public boolean isOriginalImageLoaded() {
        return isOriginalImageLoaded;
    }

    public void setOriginalImageLoaded(boolean originalImageLoaded) {
        isOriginalImageLoaded = originalImageLoaded;
    }

    public boolean isChatSelected() {
        return isChatSelected;
    }

    public void setChatSelected(boolean chatSelected) {
        isChatSelected = chatSelected;
    }
}
