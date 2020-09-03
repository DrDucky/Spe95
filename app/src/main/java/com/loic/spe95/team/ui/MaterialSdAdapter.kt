package com.loic.spe95.team.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.loic.spe95.databinding.ListItemMaterialSdBinding
import com.loic.spe95.speoperations.data.MaterialSd


class MaterialSdAdapter :
    ListAdapter<MaterialSd, MaterialSdAdapter.MaterialViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        return MaterialViewHolder(
            ListItemMaterialSdBinding.inflate(
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
        val binding: ListItemMaterialSdBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MaterialSd) {
            binding.apply {
                material = item
                executePendingBindings()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<MaterialSd>() {
        override fun areItemsTheSame(oldItem: MaterialSd, newItem: MaterialSd): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: MaterialSd, newItem: MaterialSd): Boolean {
            return oldItem == newItem
        }
    }
}