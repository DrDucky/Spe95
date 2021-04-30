package com.pomplarg.spe95.agent.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pomplarg.spe95.databinding.ListItemEnginsSdBinding
import com.pomplarg.spe95.speoperations.data.EnginSd


class EnginsSdAdapter :
    ListAdapter<EnginSd, EnginsSdAdapter.EnginViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnginViewHolder {
        return EnginViewHolder(
            ListItemEnginsSdBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

        )
    }

    override fun onBindViewHolder(holder: EnginViewHolder, position: Int) {
        val engin = getItem(position)

        holder.apply {
            bind(engin)
            itemView.tag = engin
        }

    }

    class EnginViewHolder(
        val binding: ListItemEnginsSdBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EnginSd) {
            binding.apply {
                engin = item
                executePendingBindings()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EnginSd>() {
        override fun areItemsTheSame(oldItem: EnginSd, newItem: EnginSd): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: EnginSd, newItem: EnginSd): Boolean {
            return oldItem == newItem
        }
    }
}