package com.loic.spe95.speoperations.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.loic.spe95.R
import com.loic.spe95.databinding.FragmentAddOperationBinding
import com.loic.spe95.databinding.ListItemAddOperationEquipmentSdBinding
import com.loic.spe95.team.data.Agent
import com.loic.spe95.team.ui.AgentViewModel
import com.loic.spe95.utils.Constants
import com.loic.spe95.utils.getFirestoreCollection
import com.loic.spe95.utils.getStringToType
import com.loic.spe95.utils.getTypeToString
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*


class AddOperationFragment : Fragment() {

    private lateinit var specialtyDocument: String
    private val args: AddOperationFragmentArgs by navArgs()
    private val agentViewModel: AgentViewModel by viewModel()
    private val speOperationViewModel: SpeOperationViewModel by viewModel {
        parametersOf(
            specialtyDocument
        )
    }
    private val teamList = arrayListOf<Int>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAddOperationBinding.inflate(inflater, container, false)
        val bindingListEquipmentCyno =
            ListItemAddOperationEquipmentBinding.inflate(inflater, container, false)
        val bindingListEquipmentSd =
            ListItemAddOperationEquipmentSdBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        context ?: return binding.root

        specialtyDocument = args.specialtyId.getFirestoreCollection()
        speOperationViewModel._type.value = getTypeToString(args.typeId)

        setCustomMaterialView(
            speOperationViewModel,
            args.specialtyId,
            binding,
            bindingListEquipmentCyno,
            bindingListEquipmentSd
        )
        //Binding
        val adapter =
            ArrayAdapter(
                this.context!!,
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.motifs).asList()
            )
        binding.actvMotif.setAdapter(adapter)
        binding.vmSpeOperation = speOperationViewModel
        binding.vmAgents = agentViewModel

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        autocompleteFragment?.setHint(getString(R.string.add_operation_address_hint))
        autocompleteFragment?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
        //Limit results to Ile de France
        autocompleteFragment?.setLocationRestriction(
            RectangularBounds.newInstance(
                LatLng(48.0103, 0.934),
                LatLng(49.4136, 3.8827)
            )
        )
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) { // TODO: Get info about the selected place.
                speOperationViewModel._address.value =
                    GeoPoint(place.latLng!!.latitude, place.latLng!!.longitude)
            }

            override fun onError(status: Status) {
                speOperationViewModel._address.value = null
            }
        })

        binding.actvTeamAgents.setOnItemClickListener { parent, arg1, position, arg3 ->
            binding.actvTeamAgents.text = null
            val selected = parent.getItemAtPosition(position) as Agent
            addChipToGroup(selected, binding.chipGroupTeam)
        }

        binding.btnAddOperation.setOnClickListener(View.OnClickListener {
            if (validate(speOperationViewModel, binding)) {
                speOperationViewModel._team.value = teamList
                displayMainBloc(binding, false)
                speOperationViewModel.addOperationIntoFirestore()
            }
        })

        val formatter = SimpleDateFormat(Constants.ADD_OPERATION_DATE_FORMAT_DISPLAY, Locale.FRANCE)
        val displayDate = formatter.format(Timestamp.now().toDate())
        speOperationViewModel._startDateTime.value = displayDate

        //Treatments
        agentViewModel.fetchAllAgentsPerSpecialty(specialtyDocument)

        //Observables
        speOperationViewModel.operationAdded.observe(viewLifecycleOwner, Observer {
            displayMainBloc(binding, true)
            Snackbar.make(
                view!!,
                getString(R.string.add_operation_operation_added),
                Snackbar.LENGTH_LONG
            ).show()
            val direction =
                AddOperationFragmentDirections.actionAddOperationFragmentToSpeOperationFragment()
                    .setSpecialtyId(args.specialtyId)
            findNavController().navigate(direction)
        })

        speOperationViewModel._genericException.observe(viewLifecycleOwner, Observer {
            displayMainBloc(binding, true)
            Snackbar.make(
                view!!,
                getString(R.string.add_operation_exception, it),
                Snackbar.LENGTH_LONG
            ).show()
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    fun displayMainBloc(binding: FragmentAddOperationBinding, isShow: Boolean) {
        when (isShow) {
            true  -> {
                binding.mainLayoutAddOperation.visibility = View.VISIBLE
                binding.progressbarAddOperation.visibility = View.GONE
            }
            false -> {
                binding.mainLayoutAddOperation.visibility = View.GONE
                binding.progressbarAddOperation.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Set the TimeDialog and assign the text "Martine - 02:34" to the chip
     */
    private fun setTimePicker(v: Chip) {
        TimePickerDialog(
            activity,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                v.text = String.format(
                    getString(R.string.add_operation_chip_team_text_with_time),
                    v.text,
                    hourOfDay,
                    minute
                )
            }
            ,
            0,
            0,
            true).show()
    }

    /**
     * Add a chip dynamically to the chipgroup
     * Used for team members
     */
    private fun addChipToGroup(person: Agent, chipGroup: ChipGroup) {
        val chip = Chip(context)
        chip.text =
            getString(R.string.add_operation_chip_team_text, person.firstname, person.lastname)
        chip.chipIcon = getDrawable(chipGroup.context, R.drawable.ic_account_circle)
        chip.isCloseIconVisible = true
        chip.setChipIconTintResource(R.color.colorSecondary)

        //Adding agent to list
        teamList.add(person.id)

        // necessary to get single selection working
        chip.isClickable = true
        chip.isCheckable = false
        chip.setOnClickListener {
            setTimePicker(chip)
        }
        chipGroup.addView(chip as View)
        chip.setOnCloseIconClickListener {
            //Remove agent from list
            teamList.remove(person.id)
            chipGroup.removeView(chip as View)
        }
    }

    /**
     * Include the specified material (equipment) layout depending of the specialty
     */
    private fun setCustomMaterialView(
        vmSpeOperationViewModel: SpeOperationViewModel,
        specialtyId: Int,
        binding: FragmentAddOperationBinding,
        bindingListEquipmentCyno: ListItemAddOperationEquipmentBinding,
        bindingListEquipmentSd: ListItemAddOperationEquipmentSdBinding
    ) {

        bindingListEquipmentCyno.vmSpeOperation = vmSpeOperationViewModel
        bindingListEquipmentSd.vmSpeOperation = vmSpeOperationViewModel

        var layoutToAdd: View? = null
        when (specialtyId) {
            Constants.FIRESTORE_CYNO_ID_DOCUMENT -> layoutToAdd = bindingListEquipmentCyno.root
            Constants.FIRESTORE_SD_ID_DOCUMENT   -> layoutToAdd = bindingListEquipmentSd.root
        }

        binding.equipment.addView(layoutToAdd)
    }

    /**
     * Check for mandatory fields to be completed
     */
    private fun validate(
        vmSpeOperationViewModel: SpeOperationViewModel,
        binding: FragmentAddOperationBinding
    ): Boolean {
        val mandatoryFieldError: String = getString(R.string.add_operation_error_mandatory_field)
        var isValid = true

        if (vmSpeOperationViewModel.id.value.isNullOrEmpty() && getStringToType(
                vmSpeOperationViewModel.type.value!!
            ) == Constants.TYPE_OPERATION_INTERVENTION
        ) {
            vmSpeOperationViewModel._idError.value = mandatoryFieldError
            isValid = false
        } else {
            vmSpeOperationViewModel._idError.value =
                null //Clear the value if error already displayed
        }

        if (vmSpeOperationViewModel.motif.value.isNullOrEmpty()) {
            vmSpeOperationViewModel._motifError.value = mandatoryFieldError
            isValid = false
        } else {
            vmSpeOperationViewModel._motifError.value =
                null //Clear the value if error already displayed
        }

        if (vmSpeOperationViewModel._address.value == null) {
            vmSpeOperationViewModel._addressError.value = mandatoryFieldError
            isValid = false
        } else {
            vmSpeOperationViewModel._addressError.value =
                null //Clear the value if error already displayed
        }

        if (binding.chipGroupTeam.childCount == 0) {
            vmSpeOperationViewModel._teamError.value = mandatoryFieldError
            isValid = false
        } else {
            vmSpeOperationViewModel._teamError.value =
                null //Clear the value if error already displayed
        }

        return isValid
    }
}