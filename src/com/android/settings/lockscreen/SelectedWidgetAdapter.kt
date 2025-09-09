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
package com.android.settings.lockscreen

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.android.settings.R

class SelectedWidgetAdapter(
    private val onRemove: (String) -> Unit,
    private val onReorder: (List<String>) -> Unit
) : RecyclerView.Adapter<SelectedWidgetAdapter.WidgetViewHolder>() {

    var selectedWidgets: List<String> = emptyList()
        set(value) {
            field = value.filter { it.isNotBlank() }.take(4)
            notifyDataSetChanged()
        }

    private val widgetIcons = mapOf(
        "torch" to R.drawable.ic_flashlight,
        "wifi" to R.drawable.ic_wifi,
        "data" to R.drawable.ic_data,
        "ringer" to R.drawable.ic_vibrate,
        "bt" to R.drawable.ic_bt,
        "hotspot" to R.drawable.ic_hotspot
    )

    fun moveItem(from: Int, to: Int) {
        if (from == to || from !in selectedWidgets.indices || to !in selectedWidgets.indices) return
        val mutable = selectedWidgets.toMutableList()
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        selectedWidgets = mutable
        notifyItemMoved(from, to)
        onReorder(selectedWidgets)
    }

    fun removeItem(position: Int) {
        if (position !in selectedWidgets.indices) return
        val mutable = selectedWidgets.toMutableList()
        val item = mutable.removeAt(position)
        selectedWidgets = mutable
        notifyItemRemoved(position)
        onRemove(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val container = FrameLayout(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return WidgetViewHolder(container)
    }

    override fun getItemCount(): Int = selectedWidgets.size

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val chip = LayoutInflater.from(holder.itemView.context)
            .inflate(R.layout.item_widget_chip, holder.container, false) as Chip

        val widget = selectedWidgets[position]

        chip.text = widget
        chip.isChecked = true

        widgetIcons[widget]?.let {
            chip.chipIcon = ContextCompat.getDrawable(holder.itemView.context, it)
        }

        val onRemoveClick = { removeItem(holder.bindingAdapterPosition) }
        chip.setOnClickListener { onRemoveClick() }
        chip.setOnCloseIconClickListener { onRemoveClick() }

        Log.d("LockscreenWidgets", "binded widget: $widget position: $position selectedWidgets: $selectedWidgets")

        holder.container.removeAllViews()
        holder.container.addView(chip)
    }

    inner class WidgetViewHolder(val container: FrameLayout) : RecyclerView.ViewHolder(container)
}
