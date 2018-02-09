package es.elhaso.gradha.churfthewave.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import es.elhaso.gradha.churfthewave.R;
import es.elhaso.gradha.churfthewave.logic.Logic;
import es.elhaso.gradha.churfthewave.logic.UserModel;

import static junit.framework.Assert.assertNotNull;

public class UserDetailActivity
    extends AppCompatActivity
{
    private static final String TAG = "UserDetailActivity";
    private static final String PARAM_ID = "userId";

    private Logic mLogic;

    private TextView mFirstNameText;
    private TextView mLastNameText;
    private TextView mGenderText;

    /**
     * Opens a user detail activity for the specified user.
     */
    static public void start(@NonNull Context parentActivity,
        long userId)
    {
        Intent intent = new Intent(parentActivity, UserDetailActivity.class);
        intent.putExtra(PARAM_ID, userId);
        parentActivity.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mLogic = Logic.get(this);
        setContentView(R.layout.activity_user_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assertNotNull(actionBar);
        actionBar.setDisplayShowTitleEnabled(true);

        mFirstNameText = findViewById(R.id.first_name_text);
        mLastNameText = findViewById(R.id.last_name_text);
        mGenderText = findViewById(R.id.gender_text);

        final Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            updateUi(mLogic.getUser(bundle.getLong(PARAM_ID, -1)));
        } else {
            updateUi(null);
        }
    }

    private void updateUi(@Nullable UserModel user)
    {
        mFirstNameText.setText(null != user ? user.firstName : "");
        mLastNameText.setText(null != user ? user.lastName : "");
        mGenderText.setText(null != user ? user.gender : "");
    }
}
