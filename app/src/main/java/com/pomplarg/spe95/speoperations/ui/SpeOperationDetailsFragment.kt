package com.pomplarg.spe95.speoperations.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.pomplarg.spe95.FullScreenActivity
import com.pomplarg.spe95.R
import com.pomplarg.spe95.agent.ui.*
import com.pomplarg.spe95.databinding.FragmentSpeOperationDetailsBinding
import com.pomplarg.spe95.speoperations.data.DecisionCyno
import com.pomplarg.spe95.speoperations.data.EnginSd
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
    private lateinit var agentAdapter: AgentAdapter

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
        val materialAdapter = MaterialCynoAdapter()
        val decisionsCynoAdapter = DecisionsCynoAdapter()
        val materialSdAdapter = MaterialSdAdapter()
        val materialRaAdapter = MaterialRaAdapter()
        val enginsSdAdapter = EnginsSdAdapter()
        agentAdapter = AgentAdapter()
        binding.rvMaterialCynoList.adapter = materialAdapter
        binding.rvDecisionsCynoList.adapter = decisionsCynoAdapter
        binding.rvMaterialSdList.adapter = materialSdAdapter
        binding.rvEnginsSdList.adapter = enginsSdAdapter
        binding.rvMaterialRaList.adapter = materialRaAdapter

        val speOperationHandler = SpeOperationHandlerClick()
        binding.handler = speOperationHandler

        speOperationViewModel.fetchSpeOperationInformation(speOperationId)
        subscribeUi(binding, materialAdapter, decisionsCynoAdapter, materialSdAdapter, enginsSdAdapter, materialRaAdapter)
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun subscribeUi(
        binding: FragmentSpeOperationDetailsBinding,
        materialCynoAdapter: MaterialCynoAdapter,
        decisionsCynoAdapter: DecisionsCynoAdapter,
        materialSdAdapter: MaterialSdAdapter,
        enginsSdAdapter: EnginsSdAdapter,
        materialRaAdapter: MaterialRaAdapter
    ) {
        speOperationViewModel.speOperationLd.observe(
            viewLifecycleOwner,
            Observer<SpeOperation> { it ->
                bindView(binding, it, materialCynoAdapter, decisionsCynoAdapter, materialSdAdapter, enginsSdAdapter, materialRaAdapter)
                binding.cardsGroup.visibility = View.VISIBLE

                val agentsId = ArrayList<String>()
                for (agents in it.agentOnOperation!!) {
                    agentsId.add(agents.id!!)
                }
                agentsViewModel.fetchAgentsInformationFrom(agentsId.toList())
                it.unitChief?.let { unitChiefId -> agentDetailsViewModel.fetchAgentInformation(unitChiefId) }

            })

        agentsViewModel.agentsLd.observe(viewLifecycleOwner, Observer { it ->
            agentAdapter.submitList(it)
        })

        agentDetailsViewModel.agentLd.observe(viewLifecycleOwner, Observer { it ->
            binding.tvTeamChiefUnit.text = getString(R.string.add_operation_chip_team_text, it.firstname, it.lastname)
        })
    }

    private fun bindView(
        binding: FragmentSpeOperationDetailsBinding,
        speOperation: SpeOperation,
        materialCynoAdapter: MaterialCynoAdapter,
        decisionsCynoAdapter: DecisionsCynoAdapter,
        materialSdAdapter: MaterialSdAdapter,
        enginsSdAdapter: EnginsSdAdapter,
        materialRaAdapter: MaterialRaAdapter
    ) {
        if (speOperation.id != 0L) {
            speOperation.apply {
                agentAdapter = AgentAdapter(false, this.agentOnOperation)
                binding.rvTeamAgentList.adapter = agentAdapter
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

                val decisionsSd = ArrayList<DecisionCyno>()
                for (decision in speOperation.decisionsCyno!!) {
                    decisionsSd.add(decision)
                }
                decisionsCynoAdapter.submitList(decisionsSd)

                val enginsSdList = ArrayList<EnginSd>()
                for (engin in speOperation.enginsSd!!) {
                    enginsSdList.add(engin)
                }
                enginsSdAdapter.submitList(enginsSdList)
                materialRaAdapter.submitList(speOperation.materialsRa)

                speOperation.photoRa?.let { photoRa ->
                    if (photoRa.isNotEmpty()) {
                        binding.cvPhoto.visibility = View.VISIBLE
                        binding.tvPhotoPicture.visibility = View.VISIBLE
                        val reference = FirebaseStorage.getInstance().getReference(photoRa)
                        context?.let { context ->
                            Glide.with(context)
                                .load(reference)
                                .into(binding.ivPhoto)

                            binding.ivPhoto.setOnClickListener {
                                val i = Intent(context, FullScreenActivity::class.java)
                                i.putExtra(Constants.PATH_PHOTO_FULLSCREEN_KEY, photoRa)
                                startActivity(i)
                            }
                        }
                    } else {
                        binding.cvPhoto.visibility = View.GONE
                        binding.tvPhotoPicture.visibility = View.GONE
                    }
                }
                binding.tvMaterialCategoryTitle.text = when (specialtyDocument) {
                    Constants.FIRESTORE_CYNO_DOCUMENT ->
                        if (
                            speOperation.type
                            == Constants.TYPE_OPERATION_REGULATION) {
                            getString(R.string.decision_subtitle)
                        } else {
                            getString(R.string.chiens_title)
                        }
                    Constants.FIRESTORE_RA_DOCUMENT -> getString(R.string.action_title)
                    else                              -> getString(R.string.equipment_title)
                }
            }
        } else {
            binding.tvOperationEmpty.visibility = View.VISIBLE
            binding.mainLayoutOperationDetail.visibility = View.GONE
            binding.progressbarOperationDetail.visibility = View.GONE
        }
    }
}