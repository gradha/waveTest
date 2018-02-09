package es.elhaso.gradha.churfthewave.logic;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import es.elhaso.gradha.churfthewave.network.JSONUser;

/**
 * Business logic model.
 */
public class UserModel
{
    static private final String TAG = "UserModel";

    public final long id;
    public final @NonNull String firstName;
    public final @NonNull String lastName;
    public final @NonNull String gender;

    public final @Nullable URL smallAvatarUrl;
    public final @Nullable URL bigAvatarUrl;

    public UserModel(JSONUser jsonUser)
    {
        id = jsonUser.id;
        firstName = jsonUser.firstName;
        lastName = jsonUser.lastName;
        gender = jsonUser.gender;
        smallAvatarUrl = convertUri(jsonUser.smallAvatar);
        bigAvatarUrl = convertUri(jsonUser.bigAvatar);
    }

    private @Nullable URL convertUri(@Nullable Uri uri)
    {
        if (null == uri) {
            return null;
        }

        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "Ignoring conversion error");

            return null;
        }
    }
}
