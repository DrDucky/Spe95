package com.pomplarg.spe95.agent.ui

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.pomplarg.spe95.R
import com.pomplarg.spe95.agent.data.Agent
import com.pomplarg.spe95.databinding.FragmentAddAgentBinding
import com.pomplarg.spe95.utils.Constants
import com.pomplarg.spe95.utils.hideKeyboard
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddAgentFragment : Fragment() {

    private val args: AddAgentFragmentArgs by navArgs()
    private val agentViewModel: AgentViewModel by viewModel()
    private val agentDetailsViewModel: AgentDetailsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAddAgentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        context ?: return binding.root

        //Check if agent exist (case: edit instead of add)
        if (args.agentId.isNotEmpty())
            agentDetailsViewModel.fetchAgentInformation(args.agentId)

        agentDetailsViewModel.agentLd.observe(viewLifecycleOwner, Observer<Agent> { agent ->
            agentViewModel._firstname.value = agent.firstname
            agentViewModel._lastname.value = agent.lastname
            agent.specialtiesMember?.forEach {
                if (it.key == Constants.FIRESTORE_CYNO_DOCUMENT)
                    agentViewModel._agentCyno.value = true
                if (it.key == Constants.FIRESTORE_SD_DOCUMENT)
                    agentViewModel._agentSd.value = true
                if (it.key == Constants.FIRESTORE_RA_DOCUMENT)
                    agentViewModel._agentRa.value = true
            }

            binding.btnAddAgent.text = getString(R.string.edit)
            binding.btnAddAgent.setOnClickListener(View.OnClickListener {
                if (validate(agentViewModel, binding)) {
                    agentViewModel.addAgentIntoFirestore(agent)
                }
            })
        })

        binding.btnAddAgent.text = getString(R.string.add)
        binding.btnAddAgent.setOnClickListener(View.OnClickListener {
            if (validate(agentViewModel, binding)) {
                agentViewModel.addAgentIntoFirestore(null)
            }
        })

        binding.vmAgents = agentViewModel

        binding.etLastname.filters = binding.etLastname.filters + InputFilter.AllCaps()


        //Observables
        agentViewModel.agentAdded.observe(viewLifecycleOwner, Observer {
            hideKeyboard()
            Snackbar.make(
                requireView(),
                getString(R.string.add_agent_agent_added),
                Snackbar.LENGTH_LONG
            ).show()
            val direction =
                AddAgentFragmentDirections.actionAddAgentFragmentToAgentFragment()
            findNavController().navigate(direction)
        })

        agentViewModel._genericException.observe(viewLifecycleOwner, Observer {
            hideKeyboard()
            Snackbar.make(
                requireView(),
                getString(R.string.add_agent_error, it),
                Snackbar.LENGTH_LONG
            ).show()
            val direction =
                AddAgentFragmentDirections.actionAddAgentFragmentToAgentFragment()
            findNavController().navigate(direction)
        })


        agentViewModel.agentIdAdded.observe(viewLifecycleOwner, Observer { it ->
            agentViewModel.updateAgentId(it)
        })

        agentViewModel.agentEdited.observe(viewLifecycleOwner, Observer {
            hideKeyboard()
            Snackbar.make(
                requireView(),
                getString(R.string.add_agent_agent_edited),
                Snackbar.LENGTH_LONG
            ).show()
            val direction =
                AddAgentFragmentDirections.actionAddAgentFragmentToAgentFragment()
            findNavController().navigate(direction)
        })


        setHasOptionsMenu(false)
        return binding.root
    }

    private fun validate(
        agentViewModel: AgentViewModel,
        binding: FragmentAddAgentBinding,
    ): Boolean {
        val mandatoryFieldError: String = getString(R.string.add_operation_error_mandatory_field)
        var isValid = true

        if (agentViewModel.firstname.value.isNullOrEmpty()) {
            agentViewModel._firstnameError.value = mandatoryFieldError
            isValid = false
        } else {
            agentViewModel._firstnameError.value =
                null //Clear the value if error already displayed
        }

        if (agentViewModel.lastname.value.isNullOrEmpty()) {
            agentViewModel._lastnameError.value = mandatoryFieldError
            isValid = false
        } else {
            agentViewModel._lastnameError.value =
                null //Clear the value if error already displayed
        }

        if (!binding.cbAgentCyno.isChecked && !binding.cbAgentSd.isChecked && !binding.cbAgentRa.isChecked) {
            Snackbar.make(
                requireView(),
                getString(R.string.add_agent_specialty_error),
                Snackbar.LENGTH_LONG
            ).show()
            //Display snackbar because error on checkbox is not well supported
            isValid = false
        }

        return isValid
    }
}