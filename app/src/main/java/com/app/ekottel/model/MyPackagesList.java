package com.app.ekottel.model;

/**
 * Created by ramesh.u on 5/3/2018.
 */

public class MyPackagesList {
    private String id;
    private String packageName;
    private String cost;
    private String expiry;
    private String remainingMinutes;
    private String creationDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(String remainingMinutes) {
        this.remainingMinutes = remainingMinutes;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
