package es.elhaso.gradha.churfthewave.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import es.elhaso.gradha.churfthewave.BuildConfig;
import es.elhaso.gradha.churfthewave.R;
import es.elhaso.gradha.churfthewave.logic.Logic;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity
    extends AppCompatActivity
{
    private @Nullable UserLoginTask mAuthTask;

    // UI references.
    private AutoCompleteTextView mUserEdit;
    private EditText mPasswordEdit;
    private View mProgressView;
    private View mLoginFormView;

    static public void start(@NonNull Activity parentActivity)
    {
        Intent intent = new Intent(parentActivity, LoginActivity.class);
        parentActivity.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        final Logic logic = Logic.get(this);

        if (logic.isLoggedIn()) {
            UsersListActivity.start(LoginActivity.this);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserEdit = findViewById(R.id.email);
        mPasswordEdit = findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if (BuildConfig.DEBUG) {
            mUserEdit.setText("mock_user");
            mPasswordEdit.setText("mock_password");
        }

        mPasswordEdit.setOnEditorActionListener(new TextView
            .OnEditorActionListener()

        {
            @Override public boolean onEditorAction(TextView textView,
                int id,
                KeyEvent keyEvent)
            {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo
                    .IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener()
        {
            @Override public void onClick(View view)
            {
                attemptLogin();
            }
        });

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin()
    {
        /*
        if (mAuthTask != null) {
            return;
        }
        */

        // Reset errors.
        mUserEdit.setError(null);
        mPasswordEdit.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordEdit.setError(getString(R.string
                .login_error_invalid_password));
            focusView = mPasswordEdit;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(user)) {
            mUserEdit.setError(getString(R.string.login_error_field_required));
            focusView = mUserEdit;
            cancel = true;
        } else if (!isUserValid(user)) {
            mUserEdit.setError(getString(R.string
                .login_error_invalid_username));
            focusView = mUserEdit;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(user, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUserValid(String user)
    {
        return user.length() > 0;
    }

    private boolean isPasswordValid(String password)
    {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2) private void showProgress
    (final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer
                .config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ?
                0 : 1).setListener(new AnimatorListenerAdapter()
            {
                @Override public void onAnimationEnd(Animator animation)
                {
                    mLoginFormView.setVisibility(show ? View.GONE : View
                        .VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1
                : 0).setListener(new AnimatorListenerAdapter()
            {
                @Override public void onAnimationEnd(Animator animation)
                {
                    mProgressView.setVisibility(show ? View.VISIBLE : View
                        .GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask
        extends AsyncTask<Void, Void, Exception>
    {

        private final String mUser;
        private final String mPassword;

        UserLoginTask(String user,
            String password)
        {
            mUser = user;
            mPassword = password;
        }

        @Override protected Exception doInBackground(Void... params)
        {
            final Logic logic = Logic.get();
            assertNotNull(logic);
            assertFalse(logic.isLoggedIn());

            return logic.login(mUser, mPassword).exception;
        }

        @Override protected void onPostExecute(final Exception failure)
        {
            mAuthTask = null;
            showProgress(false);

            // If there is no exception, it was a success.
            if (null != failure) {
                mPasswordEdit.setError(getString(R.string
                    .login_error_incorrect_password));
                mPasswordEdit.requestFocus();
            } else {
                UsersListActivity.start(LoginActivity.this);
                finish();
            }
        }

        @Override protected void onCancelled()
        {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

