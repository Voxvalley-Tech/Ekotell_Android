package com.app.ekottel.model;

/**
 * Created by ramesh.u on 4/2/2018.
 */

public class CallRatesModel {
    private String prefix;
    private String callRate;
    private String connectionFee;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getCallRate() {
        return callRate;
    }

    public void setCallRate(String callRate) {
        this.callRate = callRate;
    }

    public String getConnectionFee() {
        return connectionFee;
    }

    public void setConnectionFee(String connectionFee) {
        this.connectionFee = connectionFee;
    }
}
