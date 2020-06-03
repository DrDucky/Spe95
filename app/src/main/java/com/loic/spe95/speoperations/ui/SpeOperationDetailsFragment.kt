package com.loic.spe95.speoperations.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.loic.spe95.databinding.FragmentSpeOperationDetailsBinding
import com.loic.spe95.speoperations.data.SpeOperation
import com.loic.spe95.team.ui.AgentAdapter
import com.loic.spe95.team.ui.AgentViewModel
import com.loic.spe95.utils.getFirestoreCollection
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SpeOperationDetailsFragment : Fragment() {

    private lateinit var specialtyDocument: String
    private val args: SpeOperationDetailsFragmentArgs by navArgs()
    private val speOperationViewModel: SpeOperationViewModel by viewModel {
        parametersOf(
            specialtyDocument
        )
    }
    private val agentsViewModel: AgentViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSpeOperationDetailsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.mainLayoutOperationDetail.visibility = View.GONE
        binding.progressbarOperationDetail.visibility = View.VISIBLE

        val speOperationId = args.speOperationId
        specialtyDocument = args.specialtyDetailsId.getFirestoreCollection()
        val adapter = AgentAdapter()
        binding.rvTeamAgentList.adapter = adapter

        speOperationViewModel.fetchSpeOperationInformation(speOperationId)
        subscribeUi(binding, specialtyDocument, adapter)
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun subscribeUi(
        binding: FragmentSpeOperationDetailsBinding,
        specialtyDocument: String,
        adapter: AgentAdapter
    ) {
        speOperationViewModel.speOperationLd.observe(
            viewLifecycleOwner,
            Observer<SpeOperation> { it ->
                bindView(binding, it)
                binding.cardsGroup.visibility = View.VISIBLE

                agentsViewModel.fetchAgentsInformationFrom(it.agents)
            })

        agentsViewModel.agentsLd.observe(viewLifecycleOwner, Observer { it ->
            adapter.submitList(it)
        })
    }

    private fun bindView(binding: FragmentSpeOperationDetailsBinding, speOperation: SpeOperation) {
        speOperation.apply {
            binding.speOperation = speOperation
            binding.mainLayoutOperationDetail.visibility = View.VISIBLE
            binding.progressbarOperationDetail.visibility = View.GONE
        }
    }
}