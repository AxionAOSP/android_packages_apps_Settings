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

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import com.android.settings.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WidgetPickerBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var selectedRV: RecyclerView
    private lateinit var pickerRV: RecyclerView
    private lateinit var confirm: MaterialButton
    private lateinit var cancel: MaterialButton
    private lateinit var reset: MaterialButton
    private lateinit var guideTextView: TextView
    
    private val maxWidgets = 4

    private var active: List<String> = emptyList()
        set(value) {
            field = value.filter { it.isNotBlank() }.distinct().take(maxWidgets)
            selectedAdapter.selectedWidgets = field
            pickerAdapter.selection = field
            updateGuideText()
        }
    
    private val selectedAdapter = SelectedWidgetAdapter(
        onRemove = { active = active - it },
        onReorder = { newList ->
            reset()
            active = newList
        }
    )

    private val pickerAdapter = WidgetPickerAdapter(maxWidgets) { widget ->
        if (widget in active) {
            active = active - widget
        } else {
            active = active + widget
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
            BottomSheetBehavior.from(bottomSheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogStyle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.widget_picker_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedRV = view.findViewById(R.id.selected_recycler)
        pickerRV = view.findViewById(R.id.picker_recycler)
        confirm = view.findViewById(R.id.confirm_button)
        cancel = view.findViewById(R.id.cancel_button)
        reset = view.findViewById(R.id.reset_button)
        guideTextView = view.findViewById(R.id.selected_guide_text)

        selectedRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        selectedRV.adapter = selectedAdapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                selectedAdapter.moveItem(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            override fun isLongPressDragEnabled(): Boolean = true
        })
        touchHelper.attachToRecyclerView(selectedRV)

        pickerRV.layoutManager = LinearLayoutManager(context)
        pickerRV.adapter = pickerAdapter

        val currentWidgets = arguments?.getStringArrayList("widgets") ?: arrayListOf()
        active = currentWidgets.take(maxWidgets).toMutableList()
        selectedAdapter.selectedWidgets = active.toList()
        pickerAdapter.selection = active.toList()

        confirm.setOnClickListener {
            Settings.System.putString(
                requireContext().contentResolver,
                "lockscreen_widgets_extras",
                active.joinToString(",")
            )
            dismiss()
        }

        cancel.setOnClickListener { dismiss() }

        reset.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.reset_to_default)
                .setMessage(R.string.reset_confirmation)
                .setPositiveButton(R.string.yes) { _, _ -> reset() }
                .setNegativeButton(R.string.no, null)
                .show()
        }

        updateGuideText()
        Log.d("LockscreenWidgets", "oncreate: active: $active")
    }

    private fun reset() {
        active = emptyList()
        Log.d("LockscreenWidgets", "reset: active: $active")
    }

    private fun updateGuideText() {
        val targetText = if (active.isNotEmpty()) {
            getString(R.string.selected_widgets_guide)
        } else {
            getString(R.string.selected_widgets_empty)
        }

        val textView = guideTextView
        if (textView.text == targetText) return

        textView.animate()
            .alpha(0f)
            .setDuration(100)
            .withEndAction {
                textView.text = targetText
                textView.animate().alpha(1f).setDuration(150).start()
            }
            .start()
    }

    companion object {
        fun newInstance(current: List<String>): WidgetPickerBottomSheetDialog {
            return WidgetPickerBottomSheetDialog().apply {
                arguments = bundleOf("widgets" to ArrayList(current))
            }
        }
    }
}
