package es.elhaso.gradha.churfthewave.logic;

import android.content.Context;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import es.elhaso.gradha.churfthewave.disk.ChurfPreferences;
import es.elhaso.gradha.churfthewave.logic.UsersRepository.UsersListState;
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

    final private @NonNull PubSub mPubSub;
    final private @NonNull ChurfPreferences mPrefs;
    final private @NonNull UsersRepository mUsersRepository;

    private @NonNull String mAuthToken;

    //region Setters and getters

    public boolean isLoggedIn()
    {
        return mAuthToken.length() > 0;
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
        synchronized (gLock) {
            if (null == gSharedInstance) {
                gSharedInstance = new Logic(context.getApplicationContext());
            }
            assertNotNull(gSharedInstance);

            return gSharedInstance;
        }
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

    private Logic(@NonNull Context appContext)
    {
        Log.d(TAG, "Building Logic singleton");
        mPubSub = new PubSub(appContext);
        mPrefs = new ChurfPreferences(appContext);
        mAuthToken = mPrefs.getAuthToken();
        mUsersRepository = new UsersRepository(mPubSub);
    }

    //region Login

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
            Log.d(TAG, "Sleeping… to add dramatism");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final NetSyncResult<String> result = Net.login(user, password);
        Log.d(TAG, "Server login " + result);

        if (null != result.exception) {
            return result;
        }

        assertNotNull(result.value);
        mAuthToken = result.value;
        mPrefs.setAuthToken(mAuthToken);
        mPubSub.sendBroadcast(PubSub.LOGIN_EVENT);

        return result;
    }

    /**
     * Logs out the current user and starts the login activity.
     */
    @AnyThread public void logout()
    {
        mAuthToken = "";
        mPrefs.setAuthToken("");
        mUsersRepository.clear();
        mPubSub.sendBroadcast(PubSub.LOGOUT_EVENT);
    }

    //endregion Login

    //region Users

    /**
     * @return The current list of users along with the fetch state.
     * If the current state requires fetching more items, this will happen
     * automatically.
     */
    @AnyThread public @NonNull UsersListState getUsersListState()
    {
        if (mAuthToken.length() > 0) {
            mUsersRepository.fetchItems(mAuthToken);
        }
        return mUsersRepository.getCurrent();
    }

    //endregion Users
}
