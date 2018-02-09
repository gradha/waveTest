package es.elhaso.gradha.churfthewave.logic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import es.elhaso.gradha.churfthewave.misc.PubSub;
import es.elhaso.gradha.churfthewave.network.Net;

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

    @WorkerThread public boolean login(@NonNull String user,
        @NonNull String password)
    {
        assertFalse(isLoggedIn());
        DONT_BLOCK_UI();

        Net.login(user, password);

        // Presume success.
        mPubSub.sendBroadcast(PubSub.LOGIN_EVENT);

        return true;
    }
}
