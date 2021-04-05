package com.app.ekottel.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class GlobalVariables {
	public static ArrayList<Integer> notification_id_list = new ArrayList<>();
	//public static String server = "192.96.206.176";//banatelecom live
	//public static String server = "Android.banatelecom.com";//banatelecom 54.227.136.155
	public static String server = "162.0.220.110";//banatelecom 54.227.136.155
	//public static String server = "";//Konverz
	//public static String server = "182.72.244.87";//banatelecom local
	//public static String server = "208.96.164.20";//ericall
	//public static int port = 5061;
	//public static int port = 7061;  //banatelecom
	public static int port = 8050; // banatelecom
	public static String CONFIG_CLIENT_ID = "testid";

	public static final String MyPREFERENCES = "MyPrefs" ;
	public static final Logger LOG = LoggerFactory.getLogger("ekottel");



	//internal settings
	public static int loginretries = 0;

	public static String phoneNumber = "";
	public static int randomNumber = 0;
	public static String password = "password";



	public static String destinationnumbettocall = "";

	public static boolean ispeerconnectionclosed = true;

	public static int answeredcallcount = 0;
	public static String lastcallid = "";
	public static int incallcount = 0;
	public static boolean isgolive_live = false;

	public static String defaultRegion = "US";
	public static String defaultCountryCode = "+1";
	public static String SDK_APP_NAME= "Ekottel";
	public static Boolean INCALL= false;
}
