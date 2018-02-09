package es.elhaso.gradha.churfthewave.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import es.elhaso.gradha.churfthewave.misc.ParseUtils;

public class JSONAuthResult
{
    private static final String TAG = "JSONAuthResult";
    public final @NonNull String token;

    public JSONAuthResult(@NonNull JSONObject json) throws JSONException
    {
        token = ParseUtils.optString(json, "token", "");
        if (token.length() < 1) {
            throw new JSONException("Didn't find token or bad value");
        }
    }

    static public @Nullable JSONAuthResult from(@NonNull JSONObject json)
    {
        try {
            return new JSONAuthResult(json);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Couldn't extract auth token from " + json);

            return null;
        }

    }
}
