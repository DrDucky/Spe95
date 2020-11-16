package com.pomplarg.spe95.speoperations.ui

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pomplarg.spe95.R
import com.pomplarg.spe95.agent.data.Agent
import com.pomplarg.spe95.agent.ui.AgentFragmentDirections
import com.pomplarg.spe95.speoperations.data.SpeOperation

class SpeOperationHandlerClick {

    fun onSpeOperationClick(it: View, speOperation: SpeOperation) {

        when (it.resources.getBoolean(R.bool.isTablet)) {
            false -> {
                val direction =
                    SpeOperationFragmentDirections.actionSpeOperationFragmentToSpeOperationDetailsFragment()
                        .setSpeOperationId(speOperation.id)
                        .setSpecialtyDetails(speOperation.specialty)
                it.findNavController().navigate(direction)
            }
            true -> {
                val navHost =
                    it.findFragment<Fragment>().childFragmentManager.findFragmentById(R.id.fragment_spe_operation_details_container) as NavHostFragment
                val bundle = bundleOf(
                    "speOperationId" to speOperation.id,
                    "specialtyDetails" to speOperation.specialty
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

    fun onAddOperationClick(it: View, specialty: String, type: String) {
        val direction =
            SpeOperationFragmentDirections.actionSpeOperationFragmentToAddOperationFragment()
                .setSpecialty(specialty).setType(type)
        it.findNavController().navigate(direction)
    }

    fun onAddressClick(it: View, speOperation: SpeOperation) {

        val gmmIntentUri = if (speOperation.address != null) {
            Uri.parse("geo:0,0?q=${speOperation.address?.latitude},${speOperation.address?.longitude}")
        } else {
            //Use offline address
            Uri.parse("geo:0,0?q=${speOperation.addressOffline}")
        }

        MaterialAlertDialogBuilder(it.context)
            .setTitle(it.context.resources.getString(R.string.screen_title_exit_app))
            .setMessage(it.context.resources.getString(R.string.screen_title_go_gmaps))
            .setPositiveButton(it.context.resources.getString(android.R.string.ok)) { dialog, which ->
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                it.context.startActivity(mapIntent)
            }
            .show()
    }
}