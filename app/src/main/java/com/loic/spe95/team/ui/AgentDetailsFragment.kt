package com.loic.spe95.team.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.loic.spe95.databinding.FragmentAgentDetailsBinding
import com.loic.spe95.team.data.Agent
import com.loic.spe95.utils.AvatarGenerator
import org.koin.androidx.viewmodel.ext.android.viewModel

class AgentDetailsFragment : Fragment() {

    private val args: AgentDetailsFragmentArgs by navArgs()
    private val agentViewModel: AgentDetailsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAgentDetailsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val agentId = args.agentId

        subscribeUi(binding, agentId)
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun subscribeUi(
        binding: FragmentAgentDetailsBinding,
        agentId: Int
    ) {
        agentViewModel.fetchAgentInformation(agentId)
        agentViewModel.agentLd.observe(viewLifecycleOwner, Observer<Agent> { it ->
            bindView(binding, it)
            binding.progressBar.visibility = View.GONE
        })
    }

    private fun bindView(binding: FragmentAgentDetailsBinding, agent: Agent) {

        var storageReference: StorageReference? = null

        agent.apply {

            binding.agent = agent

            if (agent.avatar.isNotEmpty()) {
                storageReference =
                    FirebaseStorage.getInstance().getReferenceFromUrl(agent.avatar)
            }

            Glide.with(binding.ivAgentAvatar.context)
                .load(storageReference)
                .placeholder(
                    AvatarGenerator.avatarImage(
                        binding.ivAgentAvatar.context,
                        agent.lastname
                    )
                )
                .apply(RequestOptions.circleCropTransform())
                .override(500, 500)
                .into(binding.ivAgentAvatar)
        }
    }
}