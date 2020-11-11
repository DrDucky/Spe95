package com.pomplarg.spe95.agent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.pomplarg.spe95.databinding.FragmentAgentsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class AgentFragment : Fragment() {

    private val agentsViewModel: AgentViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAgentsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val adapter = AgentAdapter()
        binding.recyclerView.adapter = adapter
        val agentHandler = AgentHandlerClick()
        binding.handler = agentHandler

        binding.chipSd.setOnCheckedChangeListener { buttonView, isChecked ->
            agentsViewModel.teamSdSelected.value = isChecked
        }
        binding.chipCyno.setOnCheckedChangeListener { buttonView, isChecked ->
            agentsViewModel.teamCynoSelected.value = isChecked
        }

        subscribeUi(binding, adapter)

        setHasOptionsMenu(true)

        return binding.root
    }

    private fun subscribeUi(binding: FragmentAgentsBinding, adapter: AgentAdapter) {
        agentsViewModel.fetchAllAgents()
        agentsViewModel.teamSdSelected.observe(viewLifecycleOwner, Observer {
            agentsViewModel.filterTeamRv()
        })
        agentsViewModel.teamCynoSelected.observe(viewLifecycleOwner, Observer {
            agentsViewModel.filterTeamRv()
        })
        agentsViewModel.agentsLd.observe(viewLifecycleOwner, Observer { it ->
            adapter.submitList(it)
            binding.progressBar.visibility = View.GONE
        })

    }
}