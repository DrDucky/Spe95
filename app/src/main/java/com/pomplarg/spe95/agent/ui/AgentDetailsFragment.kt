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
import com.pomplarg.spe95.R
import com.pomplarg.spe95.agent.data.Agent
import com.pomplarg.spe95.databinding.FragmentAgentDetailsBinding
import com.pomplarg.spe95.statistiques.data.Statistique
import com.pomplarg.spe95.statistiques.ui.StatistiquesViewModel
import com.pomplarg.spe95.utils.AvatarGenerator
import com.pomplarg.spe95.utils.Constants
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

        agentViewModel.agentLd.observe(viewLifecycleOwner, Observer<Agent> { it ->
            bindView(binding, it)
            binding.progressBar.visibility = View.GONE
        })

        statistiquesViewModel.statsAgentLd.observe(viewLifecycleOwner, Observer<Statistique> { it ->
            bindStats(binding, it)
        })

        //By default
        configureChart(binding.timesChart, context)
        configureChart(binding.typeChart, context)
        binding.btnYearSelection.check(R.id.btn_year_2021)
        binding.btnSpecialtySelection.check(R.id.btn_specialty_cyno)
        statistiquesViewModel.fetchAgentStats(agentId, Constants.FIRESTORE_CYNO_DOCUMENT, Constants.YEAR_2021)

        binding.btnYearSelection.addOnButtonCheckedListener { group, checkedId, isChecked ->
            val yearChecked = when (checkedId) {
                R.id.btn_year_2020 -> Constants.YEAR_2020
                R.id.btn_year_2021 -> Constants.YEAR_2021
                else               -> Constants.YEAR_2021
            }
            val specialtyChecked = when (binding.btnSpecialtySelection.checkedButtonId) {
                R.id.btn_specialty_cyno -> Constants.FIRESTORE_CYNO_DOCUMENT
                R.id.btn_specialty_sd -> Constants.FIRESTORE_SD_DOCUMENT
                R.id.btn_specialty_ra -> Constants.FIRESTORE_RA_DOCUMENT
                else                    -> Constants.FIRESTORE_CYNO_DOCUMENT
            }
            statistiquesViewModel.fetchAgentStats(agentId, specialtyChecked, yearChecked)
        }

        binding.btnSpecialtySelection.addOnButtonCheckedListener { group, checkedId, isChecked ->
            val yearChecked = when (binding.btnYearSelection.checkedButtonId) {
                R.id.btn_year_2020 -> Constants.YEAR_2020
                R.id.btn_year_2021 -> Constants.YEAR_2021
                else               -> Constants.YEAR_2021
            }
            val specialtyChecked = when (checkedId) {
                R.id.btn_specialty_cyno -> Constants.FIRESTORE_CYNO_DOCUMENT
                R.id.btn_specialty_sd -> Constants.FIRESTORE_SD_DOCUMENT
                R.id.btn_specialty_ra -> Constants.FIRESTORE_RA_DOCUMENT
                else                    -> Constants.FIRESTORE_CYNO_DOCUMENT
            }
            statistiquesViewModel.fetchAgentStats(agentId, specialtyChecked, yearChecked)
        }
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
            val shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

            binding.ivAgentAvatar.setOnClickListener {
                activity?.let { activity -> AvatarGenerator.zoomImageFromThumb(activity, it, binding.ivAgentAvatar.drawable, shortAnimationDuration, binding.ivAgentAvatarExpended) }
            }
        }

        val agentHandler = AgentHandlerClick()
        binding.handler = agentHandler

    }

    private fun bindStats(binding: FragmentAgentDetailsBinding, stats: Statistique) {
        //Update UI
        configureChart(binding.timesChart, context)
        configureChart(binding.typeChart, context)
        setDataToChart(stats.agentTimes, binding.timesChart, "Temps en opération", true)
        setDataToChart(stats.agentTypes, binding.typeChart, "Types d'opération", false)
    }
}