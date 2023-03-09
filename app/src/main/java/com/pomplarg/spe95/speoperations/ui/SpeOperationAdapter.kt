package com.pomplarg.spe95.speoperations.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pomplarg.spe95.R
import com.pomplarg.spe95.databinding.ListItemSpeOperationBinding
import com.pomplarg.spe95.speoperations.data.SpeOperation
import com.pomplarg.spe95.utils.Constants


class SpeOperationAdapter :
    ListAdapter<SpeOperation, SpeOperationAdapter.SpeOperationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeOperationViewHolder {
        return SpeOperationViewHolder(
            ListItemSpeOperationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )

        )
    }

    override fun onBindViewHolder(holder: SpeOperationViewHolder, position: Int) {
        val imageView: ImageView = holder.binding.ivSpeType
        val speOperationHandler = SpeOperationHandlerClick()
        holder.binding.handler = speOperationHandler
        val speOperation = getItem(position)
        holder.apply {
            bind(speOperation)
            /*val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(speOperation.avatar)

            Glide.with(imageView.context)
                .load(storageReference)
                .apply(RequestOptions.circleCropTransform())
                .override(100, 100)
                .into(imageView);*/
            when (speOperation.type) {
                Constants.TYPE_OPERATION_REGULATION ->
                    displayIcon(imageView, R.drawable.ic_type_regulation)
                Constants.TYPE_OPERATION_INTERVENTION ->
                    displayIcon(imageView, R.drawable.ic_type_intervention)
                Constants.TYPE_OPERATION_TRAINING ->
                    displayIcon(imageView, R.drawable.ic_type_training)
                Constants.TYPE_OPERATION_FORMATION ->
                    displayIcon(imageView, R.drawable.ic_type_formation)
                Constants.TYPE_OPERATION_INFORMATION ->
                    displayIcon(imageView, R.drawable.ic_type_information)
            }
            itemView.tag = speOperation
        }

    }

    /**
     * Display icon into imageview at the left of the list
     */
    private fun displayIcon(imageView: ImageView, icon: Int) {
        imageView.setImageDrawable(
            getDrawable(
                imageView.context,
                icon
            )
        )
    }

    class SpeOperationViewHolder(
        val binding: ListItemSpeOperationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SpeOperation) {
            binding.apply {
                speOperation = item
                executePendingBindings()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SpeOperation>() {
        override fun areItemsTheSame(oldItem: SpeOperation, newItem: SpeOperation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SpeOperation, newItem: SpeOperation): Boolean {
            return oldItem == newItem
        }
    }
}