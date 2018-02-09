package es.elhaso.gradha.churfthewave.disk;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;

/**
 * Poor man's serialization.
 *
 * No encryption either, that is TODO for a stackoverflow copy&paste.
 */
public class ChurfPreferences
{
    private static final String PREF_AUTH_TOKEN = "authToken";

    final private SharedPreferences mPrefs;

    public ChurfPreferences(@NonNull Context appContext)
    {
        mPrefs = appContext.getSharedPreferences("ChurfPreferences",
            MODE_PRIVATE);
    }

    public @NonNull String getAuthToken()
    {
        return mPrefs.getString(PREF_AUTH_TOKEN, "");
    }

    public void setAuthToken(@NonNull String token)
    {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_AUTH_TOKEN, token);
        editor.apply();
    }
}
