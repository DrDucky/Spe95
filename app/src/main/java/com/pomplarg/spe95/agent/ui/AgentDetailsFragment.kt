package com.pomplarg.spe95.agent.ui

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
import com.pomplarg.spe95.agent.data.Agent
import com.pomplarg.spe95.databinding.FragmentAgentDetailsBinding
import com.pomplarg.spe95.statistiques.data.Statistique
import com.pomplarg.spe95.statistiques.ui.StatistiquesViewModel
import com.pomplarg.spe95.utils.AvatarGenerator
import com.pomplarg.spe95.utils.configureChart
import com.pomplarg.spe95.utils.setDataToChart
import org.koin.androidx.viewmodel.ext.android.viewModel

class AgentDetailsFragment : Fragment() {

    private val args: AgentDetailsFragmentArgs by navArgs()
    private val agentViewModel: AgentDetailsViewModel by viewModel()
    private val statistiquesViewModel: StatistiquesViewModel by viewModel()

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
        agentId: String
    ) {
        agentViewModel.fetchAgentInformation(agentId)
        statistiquesViewModel.fetchAgentStats(agentId, "cyno", "2020")

        agentViewModel.agentLd.observe(viewLifecycleOwner, Observer<Agent> { it ->
            bindView(binding, it)
            binding.progressBar.visibility = View.GONE
        })

        statistiquesViewModel.statsAgentLd.observe(viewLifecycleOwner, Observer<Statistique> { it ->
            bindStats(binding, it)
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

    private fun bindStats(binding: FragmentAgentDetailsBinding, stats: Statistique) {
        //Update UI
        configureChart(binding.timesChart, context!!)
        configureChart(binding.typeChart, context!!)
        setDataToChart(stats.agentTimes, binding.timesChart, "Temps en opération")
        setDataToChart(stats.agentTypes, binding.typeChart, "Types d'opération")
    }
}