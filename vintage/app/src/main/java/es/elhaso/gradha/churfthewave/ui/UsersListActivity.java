package es.elhaso.gradha.churfthewave.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import es.elhaso.gradha.churfthewave.R;
import es.elhaso.gradha.churfthewave.logic.Logic;
import es.elhaso.gradha.churfthewave.logic.UserModel;
import es.elhaso.gradha.churfthewave.logic.UsersRepository;

import static es.elhaso.gradha.churfthewave.misc.PubSub.LOGOUT_EVENT;
import static es.elhaso.gradha.churfthewave.misc.PubSub.USERS_LIST_UPDATED;
import static junit.framework.Assert.assertNotNull;

public class UsersListActivity
    extends AppCompatActivity
    implements UsersListRecyclerViewAdapter.OnUserClickedListener
{
    private static final String TAG = "UsersListActivity";

    private Logic mLogic;
    private RecyclerView mRecyclerView;
    private UsersListRecyclerViewAdapter mAdapter;

    static public void start(@NonNull Context parentActivity)
    {
        Intent intent = new Intent(parentActivity, UsersListActivity.class);
        parentActivity.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mLogic = Logic.get(this);

        setContentView(R.layout.activity_users_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assertNotNull(actionBar);
        actionBar.setDisplayShowTitleEnabled(true);

        mLogic.getPubSub().registerReceiver(mLogoutEvent, LOGOUT_EVENT);
        mLogic.getPubSub().registerReceiver(mUsersUpdatedEvent,
            USERS_LIST_UPDATED);

        mAdapter = new UsersListRecyclerViewAdapter(mLogic);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        UsersRepository.UsersListState usersListState = mLogic
            .getUsersListState();
        mAdapter.setListener(this);
        mAdapter.load(usersListState.users);
    }

    @Override protected void onDestroy()
    {
        mRecyclerView.setAdapter(null);
        mLogic.getPubSub().unregisterReceiver(mLogoutEvent);
        mLogic.getPubSub().unregisterReceiver(mUsersUpdatedEvent);
        super.onDestroy();
    }

    public void onLogoutClick(View view)
    {
        mLogic.logout();
    }

    //region Events

    private BroadcastReceiver mLogoutEvent = new BroadcastReceiver()
    {
        @Override public void onReceive(Context context,
            Intent intent)
        {
            Log.d(TAG, "Received logout notification, dying!");
            LoginActivity.start(UsersListActivity.this);
            finish();
        }
    };

    private BroadcastReceiver mUsersUpdatedEvent = new BroadcastReceiver()
    {
        @Override public void onReceive(Context context,
            Intent intent)
        {
            Log.d(TAG, "Received user update event");
            UsersRepository.UsersListState usersListState = mLogic
                .getUsersListState();
            mAdapter.load(usersListState.users);
        }
    };

    @Override public void onUserClicked(@NonNull UserModel user)
    {
        Log.d(TAG, "User clicked on " + user.firstName);
        UserDetailActivity.start(this, user.id);
    }

    //endregion Events

}
