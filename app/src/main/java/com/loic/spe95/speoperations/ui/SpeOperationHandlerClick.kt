package com.loic.spe95.speoperations.ui

import android.view.View
import androidx.navigation.findNavController
import com.loic.spe95.speoperations.data.SpeOperation
import com.loic.spe95.team.data.Agent
import com.loic.spe95.team.ui.AgentFragmentDirections

class SpeOperationHandlerClick {
    fun onSpeOperationClick(it: View, speOperation: SpeOperation) {
        val direction =
            SpeOperationFragmentDirections.actionSpeOperationFragmentToSpeOperationDetailsFragment()
                .setSpeOperationId(speOperation.id).setSpecialtyDetailsId(speOperation.specialtyId)
        it.findNavController().navigate(direction)
    }

    fun onAgentClick(it: View, agent: Agent) {
        val direction =
            AgentFragmentDirections.actionAgentFragmentToAgentDetailsFragment().setAgentId(agent.id)
        it.findNavController().navigate(direction)
    }

    fun onAddOperationClick(it: View, specialtyId: Int, typeId: Int) {
        val direction =
            SpeOperationFragmentDirections.actionSpeOperationFragmentToAddOperationFragment()
                .setSpecialtyId(specialtyId).setTypeId(typeId)
        it.findNavController().navigate(direction)
    }
}