package com.pomplarg.spe95.speoperations.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.pomplarg.spe95.agent.ui.AgentAdapter
import com.pomplarg.spe95.agent.ui.AgentViewModel
import com.pomplarg.spe95.agent.ui.MaterialCynoAdapter
import com.pomplarg.spe95.agent.ui.MaterialSdAdapter
import com.pomplarg.spe95.databinding.FragmentSpeOperationDetailsBinding
import com.pomplarg.spe95.speoperations.data.MaterialSd
import com.pomplarg.spe95.speoperations.data.SpeOperation
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

        binding.tvOperationEmpty.visibility = View.GONE
        binding.mainLayoutOperationDetail.visibility = View.GONE
        binding.progressbarOperationDetail.visibility = View.VISIBLE

        val speOperationId = args.speOperationId
        specialtyDocument = args.specialtyDetails
        val adapter = AgentAdapter()
        val materialAdapter = MaterialCynoAdapter()
        val materialSdAdapter = MaterialSdAdapter()
        binding.rvTeamAgentList.adapter = adapter
        binding.rvMaterialCynoList.adapter = materialAdapter
        binding.rvMaterialSdList.adapter = materialSdAdapter

        val speOperationHandler = SpeOperationHandlerClick()
        binding.handler = speOperationHandler

        speOperationViewModel.fetchSpeOperationInformation(speOperationId)
        subscribeUi(binding, adapter, materialAdapter, materialSdAdapter)
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun subscribeUi(
        binding: FragmentSpeOperationDetailsBinding,
        adapter: AgentAdapter,
        materialCynoAdapter: MaterialCynoAdapter,
        materialSdAdapter: MaterialSdAdapter
    ) {
        speOperationViewModel.speOperationLd.observe(
            viewLifecycleOwner,
            Observer<SpeOperation> { it ->
                bindView(binding, it, materialCynoAdapter, materialSdAdapter)
                binding.cardsGroup.visibility = View.VISIBLE

                val agentsId = ArrayList<String>()
                for (agents in it.agentOnOperation!!) {
                    agentsId.add(agents.id!!)
                }
                agentsViewModel.fetchAgentsInformationFrom(agentsId.toList())
            })

        agentsViewModel.agentsLd.observe(viewLifecycleOwner, Observer { it ->
            adapter.submitList(it)
        })
    }

    private fun bindView(
        binding: FragmentSpeOperationDetailsBinding,
        speOperation: SpeOperation,
        materialCynoAdapter: MaterialCynoAdapter,
        materialSdAdapter: MaterialSdAdapter
    ) {
        if (speOperation.id != 0) {
            speOperation.apply {
                binding.speOperation = speOperation
                binding.tvOperationEmpty.visibility = View.GONE
                binding.mainLayoutOperationDetail.visibility = View.VISIBLE
                binding.progressbarOperationDetail.visibility = View.GONE
                materialCynoAdapter.submitList(speOperation.materialsCyno)

                val materialSd = ArrayList<MaterialSd>()
                for (material in speOperation.materialsSd!!) {
                    materialSd.add(material)
                }
                materialSdAdapter.submitList(materialSd)
            }
        } else {
            binding.tvOperationEmpty.visibility = View.VISIBLE
            binding.mainLayoutOperationDetail.visibility = View.GONE
            binding.progressbarOperationDetail.visibility = View.GONE
        }
    }
}