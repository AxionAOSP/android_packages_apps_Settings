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

package com.android.settings

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings

class SimRingtonePreference(context: Context) : RingtonePreference(context, null) {

    var subscriptionId: Int = -1

    init {
        ringtoneType = RingtoneManager.TYPE_RINGTONE
    }

    override fun onPrepareRingtonePickerIntent(ringtonePickerIntent: Intent) {
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent)
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false)
    }

    override fun onSaveRingtone(ringtoneUri: Uri?) {
        Settings.System.putString(
            mUserContext.contentResolver,
            settingsKey,
            ringtoneUri?.toString() ?: ""
        )
    }

    override fun onRestoreRingtone(): Uri? {
        val uriString = Settings.System.getString(
            mUserContext.contentResolver,
            settingsKey
        )
        if (!uriString.isNullOrEmpty()) {
            return Uri.parse(uriString)
        }
        return RingtoneManager.getActualDefaultRingtoneUri(
            mUserContext, RingtoneManager.TYPE_RINGTONE
        )
    }

    private val settingsKey: String
        get() = "ringtone_sub_$subscriptionId"
}
