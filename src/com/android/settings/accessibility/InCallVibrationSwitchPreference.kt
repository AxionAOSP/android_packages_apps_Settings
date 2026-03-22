/*
 * Copyright (C) 2025-2026 AxionOS
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
package com.android.settings.accessibility

import android.content.Context
import android.provider.Settings.System.VIBRATE_ON
import androidx.annotation.StringRes
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.SettingsSecureStore
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.SwitchPreference

open class InCallVibrationSwitchPreference(
    context: Context,
    key: String,
    private val settingsKey: String,
    private val mainSwitchPreferenceKey: String,
    @StringRes title: Int,
    @StringRes summary: Int = 0,
) :
    SwitchPreference(key, title, summary),
    PreferenceAvailabilityProvider {

    private val store by lazy {
        VibrationToggleSettingsStore(
            context,
            preferenceKey = key,
            settingsProviderKey = settingsKey,
            defaultValue = false,
            keyValueStoreDelegate = SettingsSecureStore.get(context),
        )
    }

    override fun storage(context: Context): KeyValueStore = store

    override fun dependencies(context: Context) = arrayOf(mainSwitchPreferenceKey)

    override fun isAvailable(context: Context) = context.hasVibrator

    override fun isEnabled(context: Context) = store.isPreferenceEnabled()
}
