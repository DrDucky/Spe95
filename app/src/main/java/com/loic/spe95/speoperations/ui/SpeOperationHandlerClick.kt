package com.loic.spe95.speoperations.ui

import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.loic.spe95.R
import com.loic.spe95.speoperations.data.SpeOperation
import com.loic.spe95.team.data.Agent
import com.loic.spe95.team.ui.AgentFragmentDirections

class SpeOperationHandlerClick {

    fun onSpeOperationClick(it: View, speOperation: SpeOperation) {

        when (it.resources.getBoolean(R.bool.isTablet)) {
            false -> {
                val direction =
                    SpeOperationFragmentDirections.actionSpeOperationFragmentToSpeOperationDetailsFragment()
                        .setSpeOperationId(speOperation.id)
                        .setSpecialtyDetailsId(speOperation.specialtyId)
                it.findNavController().navigate(direction)
            }
            true  -> {
                val navHost =
                    it.findFragment<Fragment>().childFragmentManager.findFragmentById(R.id.fragment_spe_operation_details_container) as NavHostFragment
                val bundle = bundleOf(
                    "speOperationId" to speOperation.id,
                    "specialtyDetailsId" to speOperation.specialtyId
                )
                navHost.findNavController()
                    .navigate(R.id.speOperationDetailsDetailsFragment, bundle)
            }
        }
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