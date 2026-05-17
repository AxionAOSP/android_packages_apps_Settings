/*
 * Copyright (C) 2026 AxionOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.notification;

import static com.android.settings.notification.SettingPref.TYPE_SECURE;

import android.content.Context;
import android.provider.Settings;

import com.android.axion.deviceinfo.DeviceInfoProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class FingerprintAuthVibrationPreferenceController extends SettingPrefController {

    private static final String KEY_FINGERPRINT_AUTH_VIBRATE = "fingerprint_auth_vibrate";

    public FingerprintAuthVibrationPreferenceController(Context context,
            SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        mPreference = new SettingPref(
                TYPE_SECURE, KEY_FINGERPRINT_AUTH_VIBRATE,
                Settings.Secure.FINGERPRINT_AUTH_VIBRATE, DEFAULT_ON);
    }

    @Override
    public boolean isAvailable() {
        return super.isAvailable() && DeviceInfoProvider.isFingerprintBiometricSupported(mContext);
    }
}
