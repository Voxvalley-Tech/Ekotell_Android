package com.app.ekottel.model;

/**
 * Created by ramesh.u on 7/26/2017.
 */

public class CallLogsModel {
    private String name;
    private String number;
    private String direction;
    private String time;
    private String duration;
    private boolean isProfilePicAvailable=false;

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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
