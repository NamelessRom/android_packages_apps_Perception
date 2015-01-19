package com.android.launcher3.nameless;

import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;

import com.android.launcher3.R;
import com.android.launcher3.settings.SettingsProvider;

@SuppressWarnings("deprecation")
public class LauncherPreferenceActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private static final String ANDROID_SETTINGS = "com.android.settings";
    private static final String ANDROID_PROTECTED_APPS =
            "com.android.settings.applications.ProtectedAppsActivity";

    private static final String PREFIX_HOME_SCREEN = "homescreen_";

    private static final String KEY_PROTECTED_APPS = "protected_apps";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.settings_main);

        // Global
        final ListPreference screenOrientation =
                (ListPreference) findPreference(SettingsProvider.SETTINGS_UI_GLOBAL_ORIENTATION);
        screenOrientation.setSummary(screenOrientation.getEntry());
        screenOrientation.setOnPreferenceChangeListener(this);

        // Drawer
        final ListPreference sortMode =
                (ListPreference) findPreference(SettingsProvider.SETTINGS_UI_DRAWER_SORT_MODE);
        sortMode.setSummary(sortMode.getEntry());
        sortMode.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();
        if (KEY_PROTECTED_APPS.equals(key)) {
            final Intent intent = new Intent();
            intent.setClassName(ANDROID_SETTINGS, ANDROID_PROTECTED_APPS);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ane) {
                Log.e("LauncherPreferenceActivity", "Could not start activity!", ane);
            }
            return true;
        } else if (key.contains(PREFIX_HOME_SCREEN)) {
            SettingsProvider.putBoolean(this, SettingsProvider.SETTINGS_CHANGED, true);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            final String value = String.valueOf(newValue);
            final int index = ((ListPreference) preference).findIndexOfValue(value);
            preference.setSummary(((ListPreference) preference).getEntries()[index]);
            return true;
        }
        return false;
    }
}
