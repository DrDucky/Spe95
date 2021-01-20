package com.pomplarg.spe95.agent.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pomplarg.spe95.databinding.ListItemMaterialRaBinding
import com.pomplarg.spe95.speoperations.data.MaterialRa


class MaterialRaAdapter :
    ListAdapter<MaterialRa, MaterialRaAdapter.MaterialViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        return MaterialViewHolder(
            ListItemMaterialRaBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

        )
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val material = getItem(position)

        holder.apply {
            bind(material)
            itemView.tag = material
        }

    }

    class MaterialViewHolder(
        val binding: ListItemMaterialRaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MaterialRa) {
            binding.apply {
                material = item
                executePendingBindings()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<MaterialRa>() {
        override fun areItemsTheSame(oldItem: MaterialRa, newItem: MaterialRa): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: MaterialRa, newItem: MaterialRa): Boolean {
            return oldItem == newItem
        }
    }
}