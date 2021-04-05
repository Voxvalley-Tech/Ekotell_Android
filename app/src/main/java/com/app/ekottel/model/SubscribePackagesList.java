package com.app.ekottel.model;

/**
 * Created by ramesh.u on 5/3/2018.
 */

public class SubscribePackagesList {

    private String id;
    private String packageName;
    private String cost;
    private String validity;
    private String maxMinutes;
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

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public String getMaxMinutes() {
        return maxMinutes;
    }

    public void setMaxMinutes(String maxMinutes) {
        this.maxMinutes = maxMinutes;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
