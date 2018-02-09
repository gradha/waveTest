package es.elhaso.gradha.churfthewave.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import es.elhaso.gradha.churfthewave.R;

import static junit.framework.Assert.assertNotNull;

public class UsersListActivity
    extends AppCompatActivity
{
    static public void start(@NonNull Activity parentActivity)
    {
        Intent intent = new Intent(parentActivity, UsersListActivity.class);
        parentActivity.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assertNotNull(actionBar);
        actionBar.setDisplayShowTitleEnabled(true);
    }
}
