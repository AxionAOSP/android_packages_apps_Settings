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

package com.android.settings.notification

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.audio.Flags
import android.net.Uri
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager

import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen

import com.android.settings.R
import com.android.settings.SimRingtonePreference
import com.android.settings.Utils
import com.android.settings.core.PreferenceControllerMixin
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.utils.ThreadUtils

class PerSimRingtonePreferenceController(context: Context) :
    AbstractPreferenceController(context), PreferenceControllerMixin {

    private val simPreferences = mutableListOf<SimRingtonePreference>()

    override fun getPreferenceKey(): String = KEY

    override fun isAvailable(): Boolean {
        if (isRingtoneVibrationEnabled()) return false
        if (!Utils.isVoiceCapable(mContext)) return false
        val subs = getActiveSubscriptions()
        return subs != null && subs.size > 1
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        if (!isAvailable()) return

        val category = screen.findPreference<PreferenceCategory>(CATEGORY_KEY) ?: return
        simPreferences.clear()

        val subs = getActiveSubscriptions() ?: return
        subs.forEachIndexed { index, sub ->
            val subId = sub.subscriptionId
            val pref = SimRingtonePreference(mContext).apply {
                subscriptionId = subId
                key = "phone_ringtone_sub_$subId"
                title = "${mContext.getString(R.string.ringtone_title)} - ${sub.displayName}"
                order = PREFERENCE_ORDER + index
            }
            category.addPreference(pref)
            simPreferences.add(pref)
        }
    }

    override fun updateState(preference: Preference?) {
        simPreferences.forEach { updateSimRingtoneSummary(it) }
    }

    private fun updateSimRingtoneSummary(pref: SimRingtonePreference) {
        ThreadUtils.postOnBackgroundThread {
            val key = "$SETTINGS_KEY_PREFIX${pref.subscriptionId}"
            val uriString = Settings.System.getString(mContext.contentResolver, key)
            val ringtoneUri: Uri? = if (!uriString.isNullOrEmpty()) {
                Uri.parse(uriString)
            } else {
                RingtoneManager.getActualDefaultRingtoneUri(
                    mContext, RingtoneManager.TYPE_RINGTONE
                )
            }
            val summary = Ringtone.getTitle(mContext, ringtoneUri, false, true)
            if (summary != null) {
                ThreadUtils.postOnMainThread { pref.summary = summary }
            }
        }
    }

    private fun getActiveSubscriptions(): List<SubscriptionInfo>? {
        val sm = mContext.getSystemService(SubscriptionManager::class.java) ?: return null
        return sm.activeSubscriptionInfoList
    }

    private fun isRingtoneVibrationEnabled(): Boolean =
        Flags.enableRingtoneHapticsCustomization() && mContext.resources.getBoolean(
            com.android.internal.R.bool.config_ringtoneVibrationSettingsSupported
        )

    companion object {
        private const val KEY = "per_sim_ringtones"
        private const val CATEGORY_KEY = "sound_patterns_category"
        private const val SETTINGS_KEY_PREFIX = "ringtone_sub_"
        private const val PREFERENCE_ORDER = -119
    }
}
