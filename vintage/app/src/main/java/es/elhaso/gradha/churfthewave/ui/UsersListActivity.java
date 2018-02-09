package es.elhaso.gradha.churfthewave.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import es.elhaso.gradha.churfthewave.R;
import es.elhaso.gradha.churfthewave.logic.Logic;
import es.elhaso.gradha.churfthewave.misc.PubSub;

import static junit.framework.Assert.assertNotNull;

public class UsersListActivity
    extends AppCompatActivity
{
    private static final String TAG = "UsersListActivity";

    private Logic mLogic;

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

        mLogic.getPubSub().registerReceiver(mMessageReceiver, PubSub
            .LOGOUT_EVENT);
    }

    @Override protected void onDestroy()
    {
        mLogic.getPubSub().unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
    {
        @Override public void onReceive(Context context,
            Intent intent)
        {
            Log.d(TAG, "Received logout notification, dying!");
            LoginActivity.start(UsersListActivity.this);
            finish();
        }
    };

    public void onLogoutClick(View view)
    {
        mLogic.logout();
    }
}
