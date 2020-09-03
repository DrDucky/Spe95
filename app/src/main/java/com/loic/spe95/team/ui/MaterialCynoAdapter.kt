package com.loic.spe95.team.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.loic.spe95.databinding.ListItemMaterialCynoBinding
import com.loic.spe95.speoperations.data.MaterialCyno


class MaterialCynoAdapter :
    ListAdapter<MaterialCyno, MaterialCynoAdapter.MaterialViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        return MaterialViewHolder(
            ListItemMaterialCynoBinding.inflate(
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
        val binding: ListItemMaterialCynoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MaterialCyno) {
            binding.apply {
                material = item
                executePendingBindings()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<MaterialCyno>() {
        override fun areItemsTheSame(oldItem: MaterialCyno, newItem: MaterialCyno): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: MaterialCyno, newItem: MaterialCyno): Boolean {
            return oldItem == newItem
        }
    }
}