package es.elhaso.gradha.churfthewave.logic;

import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import es.elhaso.gradha.churfthewave.misc.PubSub;
import es.elhaso.gradha.churfthewave.misc.ThreadUtils;
import es.elhaso.gradha.churfthewave.network.JSONUser;
import es.elhaso.gradha.churfthewave.network.Net;
import es.elhaso.gradha.churfthewave.network.NetSyncResult;

import static es.elhaso.gradha.churfthewave.logic.UsersRepository.State.Loaded;
import static es.elhaso.gradha.churfthewave.logic.UsersRepository.State.Loading;
import static es.elhaso.gradha.churfthewave.logic.UsersRepository.State.None;

/**
 * Holds in memory instances of users.
 *
 * These could come from network or disk.
 */
public class UsersRepository
{
    private static final String TAG = "UsersRepository";

    private final Object mLock = new Object[0];

    private List<UserModel> mItems = new ArrayList();
    private final PubSub mPubSub;

    UsersRepository(PubSub pubSub)
    {
        mPubSub = pubSub;
    }

    // TODO: Implement properly cancellation of users fetch, in case of logout
    enum State
    {
        None,
        Loading,
        Loaded
    }

    private State mState = State.None;

    //region Users List

    public static class UsersListState
    {
        final public boolean isFetching;
        final public List<UserModel> users;

        public UsersListState(boolean isFetching,
            List<UserModel> users)
        {
            this.isFetching = isFetching;
            this.users = users;
        }
    }

    //endregion Users List

    @AnyThread void clear()
    {
        synchronized (mLock) {
            mItems.clear();
            mState = None;
        }
    }

    /**
     * Retrieves all the currently loaded items.
     *
     * This doesn't trigger any fetches, you need to use
     * {@link #fetchItems(String)} for that.
     */
    @AnyThread public @NonNull UsersListState getCurrent()
    {
        synchronized (mLock) {
            return new UsersListState(Loading == mState, new ArrayList<>
                (mItems));
        }
    }

    /**
     * Clients use this method to request more data to be loaded.
     *
     * @return True if there is any data potentially pending to be loaded.
     * False if all the data has been loaded and no work will be done. If this
     * method returns true, you should be waiting for a future notification
     * update.
     */
    @AnyThread boolean fetchItems(final @NonNull String token)
    {
        synchronized (mLock) {
            switch (mState) {
                case None:
                    mState = Loading;
                    ThreadUtils.runInSeparateThread(new Runnable()
                    {
                        @Override public void run()
                        {
                            loadFromServer(token);
                        }
                    });
                    return true;

                case Loading:
                    Log.v(TAG, "Waiting for loading state to clear");
                    return true;

                default:
                case Loaded:
                    return false;
            }
        }
    }

    /**
     * Finds the specified user model in the repository.
     *
     * This won't fetch any real data, you need to call
     * {@link #fetchItems(String)} for that.
     */
    @AnyThread @Nullable UserModel getUser(long id)
    {
        synchronized (mLock) {
            for (UserModel user : mItems) {
                if (user.id == id) {
                    return user;
                }
            }
        }

        return null;
    }

    @WorkerThread private void loadFromServer(@NonNull String token)
    {
        ThreadUtils.DONT_BLOCK_UI();

        try {
            Log.v(TAG, "Sleeping to simulate server response");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        NetSyncResult<List<JSONUser>> serverResult = Net.getUsers(token);

        try {
            synchronized (mLock) {
                if (Loading != mState) {
                    Log.e(TAG, "Reentrant cancellation? Too much for " +
                        "prototype");
                    return;
                }

                if (null != serverResult.exception || null == serverResult
                    .value) {

                    mState = None;
                    return;
                }

                mItems.clear();
                List<JSONUser> jsonUsers = serverResult.value;
                for (JSONUser jsonUser : jsonUsers) {
                    mItems.add(new UserModel(jsonUser));
                }

                mState = Loaded;
            }
        } finally {
            mPubSub.sendBroadcast(PubSub.USERS_LIST_UPDATED);
        }
    }
}
