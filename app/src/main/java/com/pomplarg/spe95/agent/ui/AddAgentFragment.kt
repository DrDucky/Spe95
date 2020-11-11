package com.pomplarg.spe95.agent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.pomplarg.spe95.R
import com.pomplarg.spe95.databinding.FragmentAddAgentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddAgentFragment : Fragment() {

    private val agentViewModel: AgentViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAddAgentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        context ?: return binding.root

        binding.vmAgents = agentViewModel

        binding.btnAddAgent.setOnClickListener(View.OnClickListener {
            agentViewModel.addAgentIntoFirestore()
        })

        //Observables
        agentViewModel.agentAdded.observe(viewLifecycleOwner, Observer {
            Snackbar.make(
                view!!,
                getString(R.string.add_agent_agent_added),
                Snackbar.LENGTH_LONG
            ).show()
            val direction =
                AddAgentFragmentDirections.actionAddAgentFragmentToAgentFragment()
            findNavController().navigate(direction)
        })


        agentViewModel.agentIdAdded.observe(viewLifecycleOwner, Observer { it ->
            agentViewModel.updateAgentId(it)
        })

        setHasOptionsMenu(false)
        return binding.root
    }
}