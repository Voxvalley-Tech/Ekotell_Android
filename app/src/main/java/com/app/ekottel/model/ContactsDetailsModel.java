package com.app.ekottel.model;

/**
 * Created by ramesh.u on 6/21/2017.
 */
public class ContactsDetailsModel {

	private String contactID="";
	private String contactName="";
	private String contactNumber="";
	private String contactEmail="";
	private String contactType="";
	private String isAppContact="";
	private boolean isProfilePicAvailable=false;

	public boolean isProfilePicAvailable() {
		return isProfilePicAvailable;
	}

	public void setProfilePicAvailable(boolean profilePicAvailable) {
		isProfilePicAvailable = profilePicAvailable;
	}

	public String getContactID() {
		return contactID;
	}
	public void setContactID(String contactID) {
		this.contactID = contactID;
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
	public String getContactType() {
		return contactType;
	}
	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public String getIsAppContact() {
		return isAppContact;
	}

	public void setIsAppContact(String isAppContact) {
		this.isAppContact = isAppContact;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
}
