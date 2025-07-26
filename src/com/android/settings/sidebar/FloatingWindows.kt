/*
 * Copyright (C) 2025 AxionOS Project
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
package com.android.settings.sidebar

import android.app.ActivityManager
import android.content.Intent
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.preferences.BaseAppListFragment

class FloatingWindows : BaseAppListFragment() {

    companion object {
        private const val TAG = "FloatingWindows"
        private const val PACKAGE_SIDEBAR = "com.android.edge.bar"
        private const val ACTION_START = "com.android.edge.bar.ACTION_START_SIDEBAR"
        private const val ACTION_STOP = "com.android.edge.bar.ACTION_STOP_SIDEBAR"
        private const val KEY_FEATURE_ENABLED = "sidebar_feature_enabled"
    }

    override fun onPreferenceReady() {
        findPreference<Preference>(KEY_FEATURE_ENABLED)?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            sendBroadcastIfNeeded(if (enabled) ACTION_START else ACTION_STOP)
            true
        }
    }

    override fun onCollectChanges() {
        super.onCollectChanges()
        val shouldEnable = isPinnedAppsAvailable() && isFeatureEnabled()
        sendBroadcastIfNeeded(if (shouldEnable) ACTION_START else ACTION_STOP)
    }

    private fun sendBroadcastIfNeeded(action: String) {
        val currentlyEnabled = isFeatureEnabled()
        val actionIsEnable = action == ACTION_START
        if ((actionIsEnable && currentlyEnabled) || (!actionIsEnable && !currentlyEnabled)) return
        sendBroadcast(action)
    }

    private fun sendBroadcast(action: String) {
        try {
            requireContext().getSystemService(ActivityManager::class.java)
                ?.forceStopPackage(PACKAGE_SIDEBAR)

            val intent = Intent(action).apply {
                setPackage(PACKAGE_SIDEBAR)
            }
            requireContext().sendBroadcastAsUser(intent, UserHandle.CURRENT)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send broadcast: $action", e)
        }
    }

    private fun isFeatureEnabled(): Boolean =
        Settings.Secure.getInt(requireContext().contentResolver, KEY_FEATURE_ENABLED, 0) == 1

    private fun isPinnedAppsAvailable(): Boolean =
        !Settings.Secure.getString(requireContext().contentResolver, getSettingsKey()).isNullOrEmpty()

    override fun getSettingsKey(): String = "edge_launcher_pinned_apps"
    override fun getCategoryTitle(): String = ""
    override fun getInitialPreferencesXmlResId(): Int = R.xml.floating_windows
    override fun getFragmentTitle(): String = getString(R.string.sidebar_title)
    override fun getSelectedCategoryTitle(): String = getString(R.string.sidebar_pinned)
    override fun getUnselectedCategoryTitle(): String = getString(R.string.sidebar_unpinned)
}
