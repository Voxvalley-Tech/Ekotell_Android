package com.app.ekottel.utils;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.ca.wrapper.CSDataProvider;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by nagaraju on 7/14/2017.
 */

public class HTTPUtils {

    private static String TAG1 = "ServerResponse";

    /**
     * This is used for retrieve information from server
     *
     * @param urlString
     * @return which returns response
     */

    public static String getResponseFromURLGET(String urlString) {
        //LOG.info(TAG1, "Response from server is request " + urlString);
        String stringResult = "";

        try {
            String username = CSDataProvider.getLoginID();
            if (username != null && username.charAt(0) != '+') {
                LOG.info(TAG1,"url request username "+username);
                urlString= urlString.replace("%2B", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Create URL string
        //String urlString = "https://185.62.85.143:443/api/getpackages?username=%2B919030708054&password=123456";
        //LOG.info("httpget", URL);
        try {
            // Create Request to server and get response
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            stringResult = buffer.toString();
            LOG.info(TAG1, "Response from server is " + stringResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringResult;
    }

    public static int statusCode;
    public static String getStripeResponseFromURLPOST(String link, String val) {

        StringBuffer balance_value = new StringBuffer();
        try {
            URL urls = new URL(link);
            HttpURLConnection connection;
            connection = (HttpURLConnection) urls.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setUseCaches(false);
            connection.setReadTimeout(30000);// set the time out as 30 secs

            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(val);
            wr.flush();
            wr.close();

            InputStream inputStream;

            statusCode = connection.getResponseCode();

            if (statusCode != HttpURLConnection.HTTP_OK)
                inputStream = connection.getErrorStream();
            else
                inputStream = connection.getInputStream();

            DataInputStream is = new DataInputStream(inputStream);

            int ch;
            while ((ch = is.read()) != -1) {
                balance_value.append((char) ch);
            }

            is.close();
            connection.disconnect();
            /**/

        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance_value.toString();
    }
    /**
     * This is used for retrieve information from server
     *
     * @param urlString
     * @return
     */
    public static String getResponseFromURLPOST(String urlString, JSONObject params) {
        //LOG.info(TAG1, "Response from server is request " + urlString + " Params are " + params.toString());
        StringBuffer balance_value = new StringBuffer();
        try {
            URL urls = new URL(urlString);
            HttpURLConnection connection;
            connection = (HttpsURLConnection) urls.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(params.toString().getBytes().length));
            connection.setUseCaches(false);
            connection.setReadTimeout(30000);// set the time out as 30 secs

            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(params.toString());
            wr.flush();
            wr.close();

            InputStream inputStream;

            int status = connection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK)
                inputStream = connection.getErrorStream();
            else
                inputStream = connection.getInputStream();

            DataInputStream is = new DataInputStream(inputStream);

            int ch;
            while ((ch = is.read()) != -1) {
                balance_value.append((char) ch);
            }

            is.close();
            connection.disconnect();
            /**/

        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance_value.toString();
    }
}
