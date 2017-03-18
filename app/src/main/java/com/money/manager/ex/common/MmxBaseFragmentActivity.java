/*
 * Copyright (C) 2012-2017 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.DatabaseMetadataFactory;
import com.money.manager.ex.log.ErrorRaisedEvent;
import com.money.manager.ex.settings.AppSettings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import icepick.Icepick;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

//import net.sqlcipher.database.SQLiteDatabase;

public abstract class MmxBaseFragmentActivity
    extends AppCompatActivity {

    public CompositeSubscription compositeSubscription;
    public static boolean isHomePressed = true;
    protected static boolean isAuthenticated = true;
    private Toolbar mToolbar;
    private boolean mDisplayHomeAsUpEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();

        AppSettings settings = new AppSettings(this);
        String locale = settings.getGeneralSettings().getApplicationLanguage();
        new Core(this).setAppLocale(locale);

        // Add layout inflater for icon fonts in xml.
//        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));

        // Initialize database encryption.
//        SQLiteDatabase.loadLibs(this);

        this.compositeSubscription = new CompositeSubscription();

        Icepick.restoreInstanceState(this, savedInstanceState);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // home click can be handled in the manifest by setting up the parent activity.
            case android.R.id.home:
                // This is used to handle the <- Home arrow button in the toolbar.
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
                if (mDisplayHomeAsUpEnabled) {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                    return true;
                }

//            case R.id.menu_cancel:
//                if (isDialogMode()) {
//                    onActionCancelClick();
//                    return true;
//                }
//            case R.id.menu_done:
//                if (isDialogMode()) {
//                    onActionDoneClick();
//                    return true;
//                }
//
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
            // set elevation actionbar 0
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setElevation(0);
//            }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onDestroy() {
        if (!compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }

        super.onDestroy();
    }

    @Subscribe
    public void onEvent(ErrorRaisedEvent event) {
        // display the error to the user
        new UIHelper(this).showToast(event.message);
    }

    /**
     * Add handlers for the OK/Cancel buttons at the bottom of the screen.
     */
    public void addDefaultButtonHandlers() {
        View container = findViewById(R.id.defaultButtons);

        showStandardToolbarActions(container);
    }

    /**
     * Uses the default toolbar and action buttons.
     */
    public void showStandardToolbarActions() {
        View toolbar = getToolbar();
        if (toolbar != null) {
            showStandardToolbarActions(toolbar);
        } else {
            // use the button container at the bottom
            addDefaultButtonHandlers();
        }
    }

    /**
     * Sets OK & Cancel as the toolbar buttons with handlers (onActionDoneClick & onActionCancelClick).
     * @param toolbar Toolbar element.
     */
    public void showStandardToolbarActions(View toolbar) {
        showStandardToolbarActions(toolbar, R.id.action_cancel, R.id.action_done);
    }

    /**
     * Allows customization of the toolbar buttons
     * @param toolbar       Toolbar element to attach to.
     * @param actionCancel  R.id of the negative (cancel) button
     * @param actionDone    R.id of the positive (action) button
     */
    public void showStandardToolbarActions(View toolbar, int actionCancel, int actionDone) {
        if (toolbar != null) {
            View cancelActionView = toolbar.findViewById(actionCancel);
            if (cancelActionView != null)
                cancelActionView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onActionCancelClick();
                    }
                });
            View doneActionView = toolbar.findViewById(actionDone);
            if (doneActionView != null)
                doneActionView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onActionDoneClick();
                    }
                });
        }
    }

    /**
     * Override to e clicking the Cancel button in the toolbar
     */
    public boolean onActionCancelClick() {
        return true;
    }

    /**
     * Override to e clicking the Action button in the toolbar
     */
    public boolean onActionDoneClick() {
        return true;
    }

    public void setDisplayHomeAsUpEnabled(boolean mDisplayHomeAsUpEnabled) {
        this.mDisplayHomeAsUpEnabled = mDisplayHomeAsUpEnabled;
        getSupportActionBar().setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
    }

    // protected

    protected Toolbar getToolbar() {
        return mToolbar;
    }

    protected void setTheme() {
        try {
            UIHelper uiHelper = new UIHelper(this);
            this.setTheme(uiHelper.getThemeId());
        } catch (Exception e) {
            Timber.e(e, "setting theme");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (isHomePressed) {
            Passcode passcode = new Passcode(getApplicationContext());
            if (passcode.hasPasscode()) {
                Intent intent = new Intent(this, PasscodeActivity.class);
                // set action and data
                intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
                intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, getString(R.string.enter_your_passcode));
                // start activity
                startActivityForResult(intent, RequestCodes.PASSCODE);
                // set in authentication
            }
            isHomePressed = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RequestCodes.PASSCODE:
                isAuthenticated = false;
                if (resultCode == RESULT_OK && data != null) {
                    Passcode passcode = new Passcode(getApplicationContext());
                    String passIntent = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                    String passDb = passcode.getPasscode();
                    if (passIntent != null && passDb != null) {
                        isAuthenticated = passIntent.equals(passDb);
                        if (!isAuthenticated) {
                            Toast.makeText(getApplicationContext(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                if (!isAuthenticated) {
                    Passcode passcode = new Passcode(getApplicationContext());
                    if (passcode.hasPasscode()) {
                        Intent intent = new Intent(this, PasscodeActivity.class);
                        // set action and data
                        intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
                        intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, getString(R.string.enter_your_passcode));
                        // start activity
                        startActivityForResult(intent, RequestCodes.PASSCODE);
                        // set in authentication
                    }
                }
                break;
        }
    }

    private boolean startingActivity = false;

    @Override
    protected void onUserLeaveHint()
    {
        if(startingActivity)
        {
            // Reset boolean for next time
            startingActivity = false;
        }
        else
        {
           isHomePressed = true;
        }
    }

    @Override
    public void startActivity(Intent intent)
    {
        startingActivity = true;
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        startingActivity = true;
        super.startActivityForResult(intent, requestCode);
    }
}
