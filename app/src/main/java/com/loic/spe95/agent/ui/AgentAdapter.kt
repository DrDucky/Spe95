package com.loic.spe95.agent.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.loic.spe95.agent.data.Agent
import com.loic.spe95.databinding.ListItemAgentBinding
import com.loic.spe95.speoperations.ui.SpeOperationHandlerClick
import com.loic.spe95.utils.AvatarGenerator


class AgentAdapter : ListAdapter<Agent, AgentAdapter.AgentViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgentViewHolder {
        return AgentViewHolder(
            ListItemAgentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

        )
    }

    override fun onBindViewHolder(holder: AgentViewHolder, position: Int) {
        val imageView: ImageView = holder.binding.ivAgent
        val speOperationHandler = SpeOperationHandlerClick()
        holder.binding.handler = speOperationHandler
        val agent = getItem(position)
        var storageReference: StorageReference? = null

        holder.apply {
            bind(agent)

            if (agent.avatar.isNotEmpty()) {
                storageReference =
                    FirebaseStorage.getInstance().getReferenceFromUrl(agent.avatar)
            }

            Glide.with(imageView.context)
                .load(storageReference)
                .placeholder(AvatarGenerator.avatarImage(imageView.context, agent.lastname))
                .apply(RequestOptions.circleCropTransform())
                .override(100, 100)
                .into(imageView)

            itemView.tag = agent
        }

    }

    class AgentViewHolder(
        val binding: ListItemAgentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Agent) {
            binding.apply {
                agent = item
                executePendingBindings()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Agent>() {
        override fun areItemsTheSame(oldItem: Agent, newItem: Agent): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Agent, newItem: Agent): Boolean {
            return oldItem == newItem
        }
    }
}