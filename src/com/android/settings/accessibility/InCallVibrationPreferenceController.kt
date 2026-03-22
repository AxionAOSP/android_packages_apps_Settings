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
import android.provider.Settings

import com.android.settings.core.TogglePreferenceController

abstract class InCallVibrationPreferenceController(
    context: Context,
    preferenceKey: String,
    private val settingKey: String
) : TogglePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus() = AVAILABLE

    override fun isChecked(): Boolean =
        Settings.Secure.getInt(mContext.contentResolver, settingKey, 0) == 1

    override fun setChecked(isChecked: Boolean): Boolean =
        Settings.Secure.putInt(mContext.contentResolver, settingKey, if (isChecked) 1 else 0)

    override fun getSliceHighlightMenuRes() = 0

    class Connect(context: Context, preferenceKey: String) :
        InCallVibrationPreferenceController(context, preferenceKey, "incall_vibrate_on_connect")

    class Disconnect(context: Context, preferenceKey: String) :
        InCallVibrationPreferenceController(context, preferenceKey, "incall_vibrate_on_disconnect")

    class CallWaiting(context: Context, preferenceKey: String) :
        InCallVibrationPreferenceController(context, preferenceKey, "incall_vibrate_on_call_waiting")
}
