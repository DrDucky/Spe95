package com.pomplarg.spe95.speoperations.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.pomplarg.spe95.databinding.FragmentSpeOperationsBinding
import com.pomplarg.spe95.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SpeOperationFragment : Fragment() {

    private lateinit var specialtyDocument: String
    private var isRotate = false
    private val args: SpeOperationFragmentArgs by navArgs()
    private val speOperationViewModel: SpeOperationViewModel by viewModel {
        parametersOf(
            specialtyDocument
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentSpeOperationsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        specialtyDocument = args.specialty

        val adapter = SpeOperationAdapter()
        val speOperationHandler = SpeOperationHandlerClick()
        binding.recyclerView.adapter = adapter
        binding.specialty = specialtyDocument
        binding.handler = speOperationHandler

        /**
         * Floating Action Buttons for adding operations
         */
        fabInit(binding.fabAddRegulation)
        fabInit(binding.fabAddIntervention)
        fabInit(binding.fabAddTraining)
        fabInit(binding.fabAddFormation)
        fabInit(binding.fabAddInformation)
        isRotate = false //Reinit the rotation (if leave the screen for example and go back)
        binding.fabAddOperation.setOnClickListener(View.OnClickListener {
            isRotate = it.rotateFab(!isRotate)
            if (isRotate) {
                if(specialtyDocument != Constants.FIRESTORE_SD_DOCUMENT) { //Hide the fab regulation button for SD
                    fabShowIn(binding.fabAddRegulation)
                }
                fabShowIn(binding.fabAddIntervention)
                fabShowIn(binding.fabAddTraining)
                fabShowIn(binding.fabAddFormation)
                fabShowIn(binding.fabAddInformation)
            } else {
                if(specialtyDocument != Constants.FIRESTORE_SD_DOCUMENT) {
                    fabShowOut(binding.fabAddRegulation)
                }
                fabShowOut(binding.fabAddIntervention)
                fabShowOut(binding.fabAddTraining)
                fabShowOut(binding.fabAddFormation)
                fabShowOut(binding.fabAddInformation)
            }
        })

        speOperationViewModel.fetchSpeOperationsInformationFromFirestore()
        subscribeUi(binding, adapter)

        setHasOptionsMenu(true)

        return binding.root
    }

    private fun subscribeUi(
        binding: FragmentSpeOperationsBinding,
        adapter: SpeOperationAdapter
    ) {
        speOperationViewModel.speOperationsLd.observe(viewLifecycleOwner, Observer { it ->
            adapter.submitList(it)
            binding.progressBar.visibility = View.GONE
        })
    }
}