package com.michelin.qrttag.common.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.michelin.qrttag.common.dialog.model.ListDialogModelUI
import com.pomplarg.spe95.R
import com.pomplarg.spe95.common.dialog.AbsDialog
import com.pomplarg.spe95.databinding.ListDialogBinding

class ListDialog(
    private val title: String?,
    private val itemList: List<ListDialogModelUI>,
    private val itemSelectedPosition: Int?
) : AbsDialog() {

    var currentItemList: List<ListDialogModelUI> = itemList
    val listAdapter = ListDialogAdapter(currentItemList)
    var onItemSelectedListener: OnItemSelectedListener? = null

    override val layoutResId: Int
        get() = R.layout.list_dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = ListDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        context ?: return binding.root

        listAdapter.onItemSelectedListener = object : ListDialogAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                onItemSelectedListener?.onItemSelected(currentItemList[position], itemList.indexOf(currentItemList[position]))
                dismiss()
            }
        }
        itemSelectedPosition?.let { listAdapter.selectedItem = itemList[it] }
        if (title.isNullOrEmpty()) {
            binding.listDialogTitle.isVisible = false
        } else {
            binding.listDialogTitle.isVisible = true
            binding.listDialogTitle.text = title
        }
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    currentItemList = itemList.filter { it.label.contains(s, true) }
                    listAdapter.itemList = currentItemList
                    listAdapter.filter = binding.listDialogEt.text.toString()
                    listAdapter.notifyDataSetChanged()
                }
            }
        }
        binding.listDialogEt.addTextChangedListener(textWatcher)

        binding.listDialogRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = listAdapter
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        // request a window without the title
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)

        }
        return dialog
    }

    interface OnItemSelectedListener {
        fun onItemSelected(item: ListDialogModelUI, position: Int)
    }

}

