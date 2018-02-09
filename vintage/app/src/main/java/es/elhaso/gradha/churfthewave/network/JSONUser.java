package es.elhaso.gradha.churfthewave.network;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import es.elhaso.gradha.churfthewave.misc.ParseUtils;

import static es.elhaso.gradha.churfthewave.misc.ParseUtils.optString;

public class JSONUser
{
    private static final String TAG = "JSONUser";

    public final long id;
    public final @NonNull String firstName;
    public final @NonNull String lastName;
    public final @NonNull String email;
    public final @NonNull String gender;
    public final @Nullable Uri smallAvatar;
    public final @Nullable Uri bigAvatar;

    public JSONUser(@NonNull JSONObject json) throws JSONException
    {
        id = ParseUtils.optLong(json, "id", -1L);
        if (id <= 0) {
            throw new JSONException("Didn't find id or bad value");
        }
        firstName = optString(json, "firstName", "");
        lastName = optString(json, "lastName", "");
        email = optString(json, "email", "");
        gender = optString(json, "gender", "");
        smallAvatar = ParseUtils.optUri(json, "smallAvatar");
        bigAvatar = ParseUtils.optUri(json, "bigAvatar");
    }

    static public @Nullable JSONUser from(@NonNull JSONObject json)
    {
        try {
            return new JSONUser(json);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Couldn't extract user from " + json);

            return null;
        }
    }
}
