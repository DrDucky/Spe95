package com.pomplarg.spe95.speoperations.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.pomplarg.spe95.R
import com.pomplarg.spe95.agent.ui.*
import com.pomplarg.spe95.databinding.FragmentSpeOperationDetailsBinding
import com.pomplarg.spe95.speoperations.data.MaterialSd
import com.pomplarg.spe95.speoperations.data.SpeOperation
import com.pomplarg.spe95.utils.Constants
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
    private val agentDetailsViewModel: AgentDetailsViewModel by viewModel()

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
        val adapter = AgentAdapter(false)
        val materialAdapter = MaterialCynoAdapter()
        val materialSdAdapter = MaterialSdAdapter()
        val materialRaAdapter = MaterialRaAdapter()
        binding.rvTeamAgentList.adapter = adapter
        binding.rvMaterialCynoList.adapter = materialAdapter
        binding.rvMaterialSdList.adapter = materialSdAdapter
        binding.rvMaterialRaList.adapter = materialRaAdapter

        binding.tvMaterialCategoryTitle.text = when (specialtyDocument) {
            Constants.FIRESTORE_CYNO_DOCUMENT -> getString(R.string.chiens_title)
            Constants.FIRESTORE_RA_DOCUMENT -> getString(R.string.action_title)
            else                              -> getString(R.string.equipment_title)
        }

        val speOperationHandler = SpeOperationHandlerClick()
        binding.handler = speOperationHandler

        speOperationViewModel.fetchSpeOperationInformation(speOperationId)
        subscribeUi(binding, adapter, materialAdapter, materialSdAdapter, materialRaAdapter)
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun subscribeUi(
        binding: FragmentSpeOperationDetailsBinding,
        adapter: AgentAdapter,
        materialCynoAdapter: MaterialCynoAdapter,
        materialSdAdapter: MaterialSdAdapter,
        materialRaAdapter: MaterialRaAdapter
    ) {
        speOperationViewModel.speOperationLd.observe(
            viewLifecycleOwner,
            Observer<SpeOperation> { it ->
                bindView(binding, it, materialCynoAdapter, materialSdAdapter, materialRaAdapter)
                binding.cardsGroup.visibility = View.VISIBLE

                val agentsId = ArrayList<String>()
                for (agents in it.agentOnOperation!!) {
                    agentsId.add(agents.id!!)
                }
                agentsViewModel.fetchAgentsInformationFrom(agentsId.toList())
                it.unitChief?.let { unitChiefId -> agentDetailsViewModel.fetchAgentInformation(unitChiefId) }

            })

        agentsViewModel.agentsLd.observe(viewLifecycleOwner, Observer { it ->
            adapter.submitList(it)
        })

        agentDetailsViewModel.agentLd.observe(viewLifecycleOwner, Observer { it ->
            binding.tvTeamChiefUnit.text = getString(R.string.add_operation_chip_team_text, it.firstname, it.lastname)
        })
    }

    private fun bindView(
        binding: FragmentSpeOperationDetailsBinding,
        speOperation: SpeOperation,
        materialCynoAdapter: MaterialCynoAdapter,
        materialSdAdapter: MaterialSdAdapter,
        materialRaAdapter: MaterialRaAdapter
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
                materialRaAdapter.submitList(speOperation.materialsRa)
            }
        } else {
            binding.tvOperationEmpty.visibility = View.VISIBLE
            binding.mainLayoutOperationDetail.visibility = View.GONE
            binding.progressbarOperationDetail.visibility = View.GONE
        }
    }
}