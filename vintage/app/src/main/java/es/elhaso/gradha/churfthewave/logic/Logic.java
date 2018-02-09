package es.elhaso.gradha.churfthewave.logic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import es.elhaso.gradha.churfthewave.misc.PubSub;
import es.elhaso.gradha.churfthewave.network.Net;
import es.elhaso.gradha.churfthewave.network.NetSyncResult;

import static es.elhaso.gradha.churfthewave.misc.ThreadUtils.DONT_BLOCK_UI;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * Holds the business logic and controls everything else.
 */
public class Logic
{
    private static final String TAG = "Logic";

    private static @Nullable Logic gSharedInstance = null;
    private static final Object gLock = new Object[0];

    private @Nullable String mAuthToken;
    private @NonNull PubSub mPubSub;

    //region Setters and getters

    public @Nullable boolean isLoggedIn()
    {
        return null != mAuthToken;
    }

    public @NonNull PubSub getPubSub()
    {
        return mPubSub;
    }

    /**
     * Call this during activity initialization to init the global app context.
     *
     * @param context Context of the activity which wants to access the logic.
     */
    public static @NonNull Logic get(@NonNull Context context)
    {
        final Logic result;
        synchronized (gLock) {
            if (null == gSharedInstance) {
                gSharedInstance = new Logic(context.getApplicationContext());
            }
            assertNotNull(gSharedInstance);
            result = gSharedInstance;
        }

        return gSharedInstance;
    }

    /**
     * Convenience method to avoid passing annoying contexts everywhere,
     * since it is very unlikely this will return null wherever we want to
     * use it.
     */
    public static @Nullable Logic get()
    {
        synchronized (gLock) {
            return gSharedInstance;
        }
    }

    //endregion Setters and getters

    public Logic(@NonNull Context appContext)
    {
        Log.d(TAG, "Building Logic singleton");
        mPubSub = new PubSub(appContext);
    }

    /**
     * Requests login against the server.
     *
     * @return True if the login was a success, an exception otherwise.
     */
    @WorkerThread public @NonNull NetSyncResult<String> login(@NonNull String
        user,
        @NonNull String password)
    {
        assertFalse(isLoggedIn());
        DONT_BLOCK_UI();

        try {
            Log.d(TAG, "Sleeping");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final NetSyncResult<String> result = Net.login(user, password);
        Log.d(TAG, "Server login " + result);

        if (null != result.exception) {
            return result;
        }

        mAuthToken = result.value;
        mPubSub.sendBroadcast(PubSub.LOGIN_EVENT);

        return result;
    }
}
