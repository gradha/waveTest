package es.elhaso.gradha.churfthewave.misc;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Miscellaneous json parsing utilities.
 *
 * Please use this optString version instead of the OS provided
 * JSONObject.optString(), since that deals *incorrectly* with null entries.
 */
public class ParseUtils
{
    final static String TAG = "ParseUtils";

    /**
     * Return the value mapped by the given key, or the empty string if not
     * present or null.
     */
    public static @NonNull String optString(@NonNull final JSONObject json,
        @NonNull final String key)
    {
        // http://code.google.com/p/android/issues/detail?id=13830
        if (json.isNull(key)) {

            return "";
        } else {

            return json.optString(key, "");
        }
    }

    /**
     * Return the value mapped by the given key, or the default value if not
     * present or null.
     */
    @Contract("_, _, !null -> !null") @Nullable public static String
    optString(@NonNull final JSONObject json,
        @NonNull final String key,
        @Nullable final String defaultValue)
    {
        // http://code.google.com/p/android/issues/detail?id=13830
        if (json.isNull(key)) {

            return defaultValue;
        } else {

            return json.optString(key, defaultValue);
        }
    }

    /**
     * Return the value mapped by the given key, or the default value if not
     * present or null.
     */
    public static boolean optBoolean(@NonNull final JSONObject json,
        @NonNull final String key,
        final boolean defaultValue)
    {
        if (json.isNull(key)) {

            return defaultValue;
        }

        try {

            return json.getBoolean(key);
        } catch (JSONException e) {

            return defaultValue;
        }
    }

    /**
     * Return the value mapped by the given key, or the default value if not
     * present or null.
     *
     * Note how this method is able to return null objects unlike the built in
     * json long getter.
     */
    @Contract("_, _, !null -> !null") @Nullable public static Long optLong
    (@NonNull final JSONObject json,
        @NonNull final String key,
        @Nullable final Long defaultValue)
    {
        if (json.isNull(key)) {

            return defaultValue;
        }

        try {

            return json.getLong(key);
        } catch (JSONException e) {

            return defaultValue;
        }
    }

    /**
     * Return the value mapped by the given key, or the default value if not
     * present or null.
     *
     * Note how this method is able to return null objects unlike the built in
     * json long getter.
     */
    @Contract("_, _, !null -> !null") @Nullable public static Double
    optDouble(@NonNull final JSONObject json,
        @NonNull final String key,
        @Nullable final Double defaultValue)
    {
        if (json.isNull(key)) {

            return defaultValue;
        }

        try {

            return json.getDouble(key);
        } catch (JSONException e) {

            return defaultValue;
        }
    }

    /**
     * Like optString but returns a valid Uri object on success.
     *
     * @return null if the Uri was not valid or there was some other problem.
     */
    @Nullable public static Uri optUri(@NonNull final JSONObject json,
        @NonNull final String key)
    {
        final String temp = optString(json, key);
        if (temp.length() < 1) {

            return null;
        }

        try {

            return Uri.parse(temp);
        } catch (Exception e) {
            Log.d(TAG, "Could not parse uri " + temp);

            return null;
        }
    }
}
