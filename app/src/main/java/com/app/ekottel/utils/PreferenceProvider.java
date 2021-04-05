package com.app.ekottel.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * This activity is used to maintain values in shared preference.
 *
 * @author Ramesh U
 * @version 2017
 */
public class PreferenceProvider {

    public static final String SAVED_AUDIO_MODE = "savedAudioMode";
    public static final String IS_PASSWORD_UPDATED = "isPassWordUpdated";
    public static final String IN_CALL_CALLER_ID = "inCallCallerId";
    public static final String RUNNING_CALL_TYPE = "callType";
    public static final String IN_CALL_NUMBER = "inCallNumber";
    public static final String IS_CALL_RUNNING = "isCalLRunning";
    public static final String LAST_DIAL_NUMBER = "lastDialNumber";
    public static final String USER_REGISTRED_COUNTRY_CODE = "userRegistredCountry";
    public static final String IS_RETURNING_FROM_CALL="isReturningFromCall";
    public static final String NEED_TO_SHOW_CALL_END_REASON="needToShowCallEndReason";
    public static final String DONT_SHOW_DEFAULT_APP_SETTINGS = "dont_show_defauilt_app_settings";
    public static final String DONT_SHOW_LOCK_SCREEN_NOTIFICATION_MIUI = "dont_show_lock_screen_notification_miui";
    public static final String IS_FILE_SHARE_AVAILABLE = "is_file_share_available";
    SharedPreferences Pref;
    Editor edit;

    public PreferenceProvider(Context applicationContext) {

        Pref = applicationContext.getSharedPreferences(GlobalVariables.MyPREFERENCES,
                Context.MODE_PRIVATE);

    }

    public void setPrefString(String key, String val) {

        edit = Pref.edit();
        edit.putString(key, val);
        edit.commit();
    }

    public void setPrefboolean(String key, boolean val) {

        edit = Pref.edit();
        edit.putBoolean(key, val);
        edit.commit();
    }

    public void setPrefint(String key, int val) {

        edit = Pref.edit();
        edit.putInt(key, val);
        edit.commit();
    }

    public void setPreffloat(String key, float val) {

        edit = Pref.edit();
        edit.putFloat(key, val);
        edit.commit();
    }

    public String getPrefString(String key) {

        return Pref.getString(key, "");
    }

    public boolean getPrefBoolean(String key) {

        return Pref.getBoolean(key, false);
    }

    public int getPrefInt(String key) {

        return Pref.getInt(key, 0);
    }

    public float getPrefFloat(String key) {

        return Pref.getFloat(key, 0);
    }

    public int getPrefInt(String key, int i) {
        return Pref.getInt(key, -1);
    }

}
