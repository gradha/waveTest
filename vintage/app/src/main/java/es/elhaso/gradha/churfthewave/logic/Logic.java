package es.elhaso.gradha.churfthewave.logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import es.elhaso.gradha.churfthewave.disk.ChurfPreferences;
import es.elhaso.gradha.churfthewave.logic.UsersRepository.UsersListState;
import es.elhaso.gradha.churfthewave.misc.PubSub;
import es.elhaso.gradha.churfthewave.misc.ThreadUtils;
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
    final private @NonNull ImageCache mImageCache;

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
        mImageCache = new ImageCache();
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
        mImageCache.clear();
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

    /**
     * @return The specified user or null if not found. This won't perform
     * any network fetches.
     */
    @AnyThread public @Nullable UserModel getUser(long id)
    {
        return mUsersRepository.getUser(id);
    }

    //endregion Users

    //region Image cache wrapper

    /**
     * Fetches an image at the specified URL.
     *
     * Unlike the raw method from the {@link Net} class, this will cache the
     * bitmaps and thus the answer should happen quicker.
     *
     * @param strongCallback The callback will be called with null or the
     * bitmap image always in a background thread.
     */
    @AnyThread public void getBitmap(final @NonNull URL url,
        @NonNull final Net.OnBitmapLoadedCallback strongCallback)
    {
        final WeakReference<Net.OnBitmapLoadedCallback> weakCallback = new
            WeakReference<>(strongCallback);
        final Bitmap cachedBitmap = mImageCache.get(url);
        if (null != cachedBitmap) {
            Log.d(TAG, "Found image for " + url);
            ThreadUtils.runInBackground(new Runnable()
            {
                @Override public void run()
                {
                    Net.OnBitmapLoadedCallback callback = weakCallback.get();
                    if (null != callback) {
                        callback.onBitmapLoaded(url, cachedBitmap);
                    }
                }
            });
            return;
        }

        Log.d(TAG, "Failed cache for " + url);
        // Wrap the callback to cache the results for future queries.
        Net.getBitmap(url, new Net.OnBitmapLoadedCallback()
        {
            @Override public void onBitmapLoaded(final @NonNull URL url,
                final @Nullable Bitmap bitmap)
            {
                if (null != bitmap) {
                    Log.d(TAG, "Saving cache for " + url);
                    mImageCache.put(url, bitmap);
                }

                Net.OnBitmapLoadedCallback callback = weakCallback.get();
                if (null != callback) {
                    callback.onBitmapLoaded(url, bitmap);
                }
            }
        });
    }

    //endregion Image cache wrapper
}
