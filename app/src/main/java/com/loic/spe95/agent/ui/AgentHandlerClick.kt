package com.loic.spe95.agent.ui

import android.view.View
import androidx.navigation.findNavController

class AgentHandlerClick {

    fun onAddAgentClick(it: View) {
        val direction =
            AgentFragmentDirections.actionAgentFragmentToAddOperationFragment()
        it.findNavController().navigate(direction)
    }
}