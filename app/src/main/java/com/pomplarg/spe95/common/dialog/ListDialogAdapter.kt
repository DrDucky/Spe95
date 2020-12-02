package com.michelin.qrttag.common.dialog

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.michelin.qrttag.common.dialog.model.ListDialogModelUI
import com.pomplarg.spe95.R
import kotlinx.android.synthetic.main.list_dialog_cell.view.*
import java.util.*


class ListDialogAdapter(var itemList: List<ListDialogModelUI>) :
    RecyclerView.Adapter<ListDialogAdapter.ListDialogItemViewHolder>() {

    var onItemSelectedListener: OnItemSelectedListener? = null
    var filter: String? = null
    var selectedItem: ListDialogModelUI? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListDialogItemViewHolder =
        ListDialogItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_dialog_cell, parent, false)
        )

    override fun onBindViewHolder(holder: ListDialogItemViewHolder, position: Int) {
        val isSelected = itemList[position] == selectedItem
        holder.bind(itemList[position], position, isSelected)
    }

    override fun getItemCount(): Int =
        itemList.size

    inner class ListDialogItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: ListDialogModelUI, position: Int, isSelected: Boolean) {
            itemView.listDialogCellLabel.apply {
                filter?.let {

                    val wordToSpan: Spannable = SpannableString(item.label.toUpperCase(Locale.getDefault()))
                    if (wordToSpan.contains(filter.toString(), true)) {
                        val initialPosition = wordToSpan.indexOf(filter!!, 0, true)
                        wordToSpan.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorSecondary)),
                            initialPosition,
                            initialPosition + filter!!.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    text = wordToSpan
                } ?: run {
                    text = item.label.toUpperCase(Locale.getDefault())
                }
                isChecked = isSelected
                isClickable = false
            }
            itemView.listDialogCellSubLabel.apply {
                item.subLabel.let {
                    isVisible = !it.isNullOrEmpty()
                    text = it
                }
            }
            itemView.setOnClickListener {
                onItemSelectedListener?.onItemSelected(position)
            }
        }
    }

    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)
    }

}