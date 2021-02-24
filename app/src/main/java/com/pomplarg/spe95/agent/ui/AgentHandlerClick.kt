package com.pomplarg.spe95.agent.ui

import android.view.View
import androidx.navigation.findNavController
import com.pomplarg.spe95.agent.data.Agent
import com.pomplarg.spe95.agent.ui.AgentDetailsFragmentDirections.actionAgentFragmentToEditAgentFragment

class AgentHandlerClick {

    fun onAddAgentClick(it: View) {
        val direction =
            AgentFragmentDirections.actionAgentFragmentToAddAgentFragment()
        it.findNavController().navigate(direction)
    }

    fun onEditAgentClick(it: View, agent: Agent) {
        val direction =
            actionAgentFragmentToEditAgentFragment().setAgentId(agent.id)
        it.findNavController().navigate(direction)
    }
}