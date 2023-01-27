package com.pomplarg.spe95.agent.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pomplarg.spe95.databinding.ListItemDecisionsCynoBinding
import com.pomplarg.spe95.databinding.ListItemMaterialSdBinding
import com.pomplarg.spe95.speoperations.data.DecisionCyno
import com.pomplarg.spe95.speoperations.data.MaterialSd


class DecisionsCynoAdapter :
    ListAdapter<DecisionCyno, DecisionsCynoAdapter.DecisionsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DecisionsViewHolder {
        return DecisionsViewHolder(
            ListItemDecisionsCynoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

        )
    }

    override fun onBindViewHolder(holder: DecisionsViewHolder, position: Int) {
        val decision = getItem(position)

        holder.apply {
            bind(decision)
            itemView.tag = decision
        }

    }

    class DecisionsViewHolder(
        val binding: ListItemDecisionsCynoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DecisionCyno) {
            binding.apply {
                decision = item
                executePendingBindings()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DecisionCyno>() {
        override fun areItemsTheSame(oldItem: DecisionCyno, newItem: DecisionCyno): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: DecisionCyno, newItem: DecisionCyno): Boolean {
            return oldItem == newItem
        }
    }
}