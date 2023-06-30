package com.pomplarg.spe95.agent.ui

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.pomplarg.spe95.R
import com.pomplarg.spe95.databinding.FragmentAgentsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class AgentFragment : Fragment() {

    private val agentsViewModel: AgentViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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
        binding.chipRa.setOnCheckedChangeListener { buttonView, isChecked ->
            agentsViewModel.teamRaSelected.value = isChecked
        }

        subscribeUi(binding, adapter)

        setupMenu()

        return binding.root
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Hide the menu (map and stats button)
                menu.setGroupVisible(R.id.main_menu_group, false)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_toolbar, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun subscribeUi(binding: FragmentAgentsBinding, adapter: AgentAdapter) {
        agentsViewModel.fetchAllAgents()
        agentsViewModel.teamSdSelected.observe(viewLifecycleOwner, Observer {
            agentsViewModel.filterTeamRv()
        })
        agentsViewModel.teamCynoSelected.observe(viewLifecycleOwner, Observer {
            agentsViewModel.filterTeamRv()
        })
        agentsViewModel.teamRaSelected.observe(viewLifecycleOwner, Observer {
            agentsViewModel.filterTeamRv()
        })
        agentsViewModel.agentsLd.observe(viewLifecycleOwner, Observer { it ->
            adapter.submitList(it)
            binding.progressBar.visibility = View.GONE
        })

    }
}