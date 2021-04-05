package com.app.ekottel.model;
/**
 * Created by ramesh.u on 5/23/2017.
 */
public class RechargeHistoryPojo implements Comparable<RechargeHistoryPojo>{
	private String date;
	private String cost;
	private String type;
	private String fromuser;
	private String touser;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFromuser() {
		return fromuser;
	}

	public void setFromuser(String fromuser) {
		this.fromuser = fromuser;
	}

	public String getTouser() {
		return touser;
	}

	public void setTouser(String touser) {
		this.touser = touser;
	}

	@Override
	public int compareTo(RechargeHistoryPojo o) {

		return this.getDate().compareTo(o.getDate());
	}
}

