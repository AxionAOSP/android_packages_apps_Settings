package com.android.settings.gestures;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.SliderPreference;

public class GestureNavbarLengthPreferenceController
        extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY = "gesture_navbar_length_mode";

    public GestureNavbarLengthPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        Preference preference = screen.findPreference(getPreferenceKey());
        if (preference != null) {
            preference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void updateState(Preference preference) {
        int value = Settings.System.getIntForUser(
                mContext.getContentResolver(),
                KEY,
                2,
                UserHandle.USER_CURRENT);

        ((SliderPreference) preference).setValue(value);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        return Settings.System.putIntForUser(
                mContext.getContentResolver(),
                KEY,
                (Integer) value,
                UserHandle.USER_CURRENT);
    }
}
