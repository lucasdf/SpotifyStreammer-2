package com.example.lucas.spotifystreammerv2.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.lucas.spotifystreammerv2.R;

/**
 * Created by Lucas on 08/07/2015.
 */
public class Utility {
    // Get the country ISO 3166 code
    public static String getLocationPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_country_code_key),
                context.getString(R.string.pref_country_code_default));
    }
    // Get notification preference (true=enabled, false=disabled)
    public static boolean getNotificationPreference(Context context) {
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
        return displayNotifications;
    }
    // Get 'explicit' song preference (true=enabled, false=disabled)
    public static boolean getExplicitSongsPreference(Context context) {
        String displayNotificationsKey = context.getString(R.string.pref_enable_explicit_songs_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_explicit_songs_default)));
        return displayNotifications;
    }
}
