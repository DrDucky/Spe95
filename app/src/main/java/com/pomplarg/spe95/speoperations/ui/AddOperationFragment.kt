package com.pomplarg.spe95.speoperations.ui

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.pomplarg.spe95.R
import com.pomplarg.spe95.ToolbarTitleListener
import com.pomplarg.spe95.agent.data.Agent
import com.pomplarg.spe95.agent.ui.AgentViewModel
import com.pomplarg.spe95.databinding.FragmentAddOperationBinding
import com.pomplarg.spe95.databinding.ListItemAddOperationEquipmentCynoBinding
import com.pomplarg.spe95.databinding.ListItemAddOperationEquipmentSdBinding
import com.pomplarg.spe95.speoperations.data.AgentOnOperation
import com.pomplarg.spe95.utils.Constants
import com.pomplarg.spe95.utils.hasConnectivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*


class AddOperationFragment : Fragment() {

    private lateinit var specialtyDocument: String
    private val args: AddOperationFragmentArgs by navArgs()
    private val agentViewModel: AgentViewModel by viewModel()
    private val regex = Regex("\\d*:\\d*")
    private val speOperationViewModel: SpeOperationViewModel by viewModel {
        parametersOf(
            specialtyDocument
        )
    }

    /* List of Agent & time. Exemple :
    Paul ID : 8
    Time on operation : 01:15
    hashmap = <8, Paul - 01:15>

    Pierre ID : 9
    Time on operation : 02:30
    teamList = (<8, Paul - 01:15>, <9, Pierre - 02:30>)
     */
    private val teamList = arrayListOf<AgentOnOperation>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAddOperationBinding.inflate(inflater, container, false)
        val bindingListEquipmentCyno =
            ListItemAddOperationEquipmentCynoBinding.inflate(inflater, container, false)
        val bindingListEquipmentSd =
            ListItemAddOperationEquipmentSdBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        context ?: return binding.root


        val connected = hasConnectivity(context)

        specialtyDocument = args.specialty
        speOperationViewModel._type.value = args.type

        val title = when (speOperationViewModel._type.value) {
            Constants.TYPE_OPERATION_TRAINING -> getString((R.string.screen_title_add_training), speOperationViewModel._type.value)
            else                              -> getString((R.string.screen_title_add_operation), speOperationViewModel._type.value)
        }
        (activity as ToolbarTitleListener).updateTitle(title)

        setCustomMaterialView(
            speOperationViewModel,
            args.specialty,
            binding,
            bindingListEquipmentCyno,
            bindingListEquipmentSd,
            connected
        )
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
                speOperationViewModel._teamAgent.value = teamList
                //speOperationViewModel._team.value = teamList
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
                requireView(),
                getString(R.string.add_operation_operation_added),
                Snackbar.LENGTH_LONG
            ).show()
            val direction =
                AddOperationFragmentDirections.actionAddOperationFragmentToSpeOperationFragment()
                    .setSpecialty(args.specialty)
            findNavController().navigate(direction)
        })

        speOperationViewModel._genericException.observe(viewLifecycleOwner, Observer {
            displayMainBloc(binding, true)
            Snackbar.make(
                requireView(),
                getString(R.string.add_operation_exception, it),
                Snackbar.LENGTH_LONG
            ).show()
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    fun displayMainBloc(binding: FragmentAddOperationBinding, isShow: Boolean) {
        when (isShow) {
            true -> {
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
    private fun setTimePicker(v: Chip, person: Agent, selectedAgent: AgentOnOperation) {
        var defaultHour = 0
        var defaultMinute = 0
        val oldText = v.text
        //Retrieve time setted by parsing the chip text - not ideal, but it works
        val oldAgentTime: String? = regex.find(oldText)?.groups?.first()?.value
        if (oldAgentTime != null && oldAgentTime.isNotEmpty()) {
            defaultHour = oldAgentTime.substringBefore(":").toInt()
            defaultMinute = oldAgentTime.substringAfter(":").toInt()
        }
        v.text =
            getString(R.string.add_operation_chip_team_text, person.firstname, person.lastname)
        TimePickerDialog(
            activity,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                v.text = String.format(
                    getString(R.string.add_operation_chip_team_text_with_time),
                    v.text,
                    hourOfDay,
                    minute
                )
                val c = Calendar.getInstance()
                c.set(Calendar.HOUR, hourOfDay)
                c.set(Calendar.MINUTE, minute)

                //Adding agent to list
                selectedAgent.time = hourOfDay * 60 + minute
                teamList.add(selectedAgent)
            },
            defaultHour,
            defaultMinute,
            true
        ).show()
    }

    /**
     * Add a chip dynamically to the chipgroup
     * Used for team members
     */
    private fun addChipToGroup(person: Agent, chipGroup: ChipGroup) {
        val selectedAgent = AgentOnOperation(person.id)
        val chip = Chip(context)
        chip.text =
            getString(R.string.add_operation_chip_team_text, person.firstname, person.lastname)
        chip.chipIcon = getDrawable(chipGroup.context, R.drawable.ic_account_circle)
        chip.isCloseIconVisible = true
        chip.setChipIconTintResource(R.color.colorSecondary)

        // necessary to get single selection working
        chip.isClickable = true
        chip.isCheckable = false
        chip.setOnClickListener {
            setTimePicker(chip, person, selectedAgent)
        }
        chipGroup.addView(chip as View)
        chip.setOnCloseIconClickListener {
            //Remove agent from list
            teamList.remove(selectedAgent)
            chipGroup.removeView(chip as View)
        }
    }

    /**
     * Include the specified material (equipment) layout depending of the specialty
     */
    private fun setCustomMaterialView(
        vmSpeOperationViewModel: SpeOperationViewModel,
        specialtyId: String,
        binding: FragmentAddOperationBinding,
        bindingListEquipmentCyno: ListItemAddOperationEquipmentCynoBinding,
        bindingListEquipmentSd: ListItemAddOperationEquipmentSdBinding,
        connected: Boolean
    ) {

        bindingListEquipmentCyno.vmSpeOperation = vmSpeOperationViewModel
        bindingListEquipmentSd.vmSpeOperation = vmSpeOperationViewModel
        //Checkboxes cliquables (ouverture de popup)
        bindingListEquipmentSd.tvEclairageCategoryGroupeElectro.setOnClickListener { buttonView ->
            context?.let {
                setEquipmentPopupCheckboxes(
                    it,
                    buttonView,
                    speOperationViewModel
                )
            }
        }
        bindingListEquipmentSd.tvEclairageCategoryEclairage.setOnClickListener { buttonView ->
            context?.let {
                setEquipmentPopupCheckboxes(
                    it,
                    buttonView,
                    speOperationViewModel
                )
            }
        }

        var layoutToAdd: View? = null
        when (specialtyId) {
            Constants.FIRESTORE_CYNO_DOCUMENT -> layoutToAdd = bindingListEquipmentCyno.root
            Constants.FIRESTORE_SD_DOCUMENT -> layoutToAdd = bindingListEquipmentSd.root
        }

        binding.equipment.addView(layoutToAdd)

        //Motifs
        var arrayForMotifs: Int = R.array.motifs_cyno //Default value
        when (specialtyId) {
            Constants.FIRESTORE_CYNO_DOCUMENT -> arrayForMotifs = R.array.motifs_cyno
            Constants.FIRESTORE_SD_DOCUMENT -> arrayForMotifs = R.array.motifs_sd
        }
        val adapter =
            ArrayAdapter(
                this.requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(arrayForMotifs).asList()
            )
        binding.actvMotif.setAdapter(adapter)

        binding.connected = connected
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

        if (vmSpeOperationViewModel.id.value.isNullOrEmpty() &&
            vmSpeOperationViewModel.type.value!!
            == Constants.TYPE_OPERATION_INTERVENTION
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


        if (binding.spAddress.visibility == View.VISIBLE && vmSpeOperationViewModel._address.value == null) {
            vmSpeOperationViewModel._addressError.value = mandatoryFieldError
            isValid = false
        } else {
            vmSpeOperationViewModel._addressError.value =
                null //Clear the value if error already displayed
        }

        if (binding.spAddressOffline.visibility == View.VISIBLE && vmSpeOperationViewModel._addressOffline.value.isNullOrEmpty()) {
            vmSpeOperationViewModel._addressOfflineError.value = mandatoryFieldError
            isValid = false
        } else {
            vmSpeOperationViewModel._addressOfflineError.value =
                null //Clear the value if error already displayed
        }

        if (binding.chipGroupTeam.childCount == 0) {
            vmSpeOperationViewModel._teamError.value = mandatoryFieldError
            isValid = false
        } else {
            //Une durée de travail est requise
            var inError: Boolean = false
            for (index in 0 until binding.chipGroupTeam.childCount) {
                val childView: Chip = binding.chipGroupTeam.getChildAt(index) as Chip
                if (!childView.text.contains(regex))
                    inError = true
                break
            }
            if (inError) {
                vmSpeOperationViewModel._teamError.value = getString(R.string.add_operation_error_time_required)
                isValid = false
            } else
                vmSpeOperationViewModel._teamError.value =
                    null //Clear the value if error already displayed
        }

        return isValid
    }
}

private fun setEquipmentPopupCheckboxes(
    context: Context,
    checkboxView: View,
    vmSpeOperationViewModel: SpeOperationViewModel
) {
    val checkedItemsGrElec = booleanArrayOf(
        vmSpeOperationViewModel._equipementSdGrElecFixe.value!!,
        vmSpeOperationViewModel._equipementSdGrElec22001.value!!,
        vmSpeOperationViewModel._equipementSdGrElec22002.value!!,
        vmSpeOperationViewModel._equipementSdGrElec30001.value!!,
        vmSpeOperationViewModel._equipementSdGrElec30002.value!!
    )
    val checkedItemsGrEClairage = booleanArrayOf(
        vmSpeOperationViewModel._equipementSdEclSolaris.value!!,
        vmSpeOperationViewModel._equipementSdEclNeon.value!!,
        vmSpeOperationViewModel._equipementSdEclSolaris.value!!,
        vmSpeOperationViewModel._equipementSdEclBaby.value!!
    )
    val checkbox = checkboxView as CompoundButton
    when (checkbox.id) {
        R.id.tv_eclairage_category_groupe_electro -> {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.resources.getString(R.string.equipment_eclairage_groupe_electro))
                .setPositiveButton(context.resources.getString(android.R.string.ok)) { dialog, which ->
                    //Nothing to do, closes automatically with multiChoiceItems
                    //Except set the checkbox check if at least one of the item has been selected
                    checkbox.isChecked = (vmSpeOperationViewModel._equipementSdGrElecFixe.value!!
                            || vmSpeOperationViewModel._equipementSdGrElec22001.value!!
                            || vmSpeOperationViewModel._equipementSdGrElec22002.value!!
                            || vmSpeOperationViewModel._equipementSdGrElec30001.value!!
                            || vmSpeOperationViewModel._equipementSdGrElec30002.value!!)
                }
                .setMultiChoiceItems(R.array.sd_gr_elec, checkedItemsGrElec) { dialog, which, checked ->
                    when (which) {
                        0 -> vmSpeOperationViewModel._equipementSdGrElecFixe.value = checked
                        1 -> vmSpeOperationViewModel._equipementSdGrElec22001.value = checked
                        2 -> vmSpeOperationViewModel._equipementSdGrElec22002.value = checked
                        3 -> vmSpeOperationViewModel._equipementSdGrElec30001.value = checked
                        4 -> vmSpeOperationViewModel._equipementSdGrElec30002.value = checked
                    }
                }
                .show()
        }
        R.id.tv_eclairage_category_eclairage -> {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.resources.getString(R.string.equipment_eclairage_eclairage))
                .setPositiveButton(context.resources.getString(android.R.string.ok)) { dialog, which ->
                    //Nothing to do, closes automatically with multiChoiceItems
                    //Except set the checkbox check if at least one of the item has been selected
                    checkbox.isChecked = (vmSpeOperationViewModel._equipementSdEclSolaris.value!!
                            || vmSpeOperationViewModel._equipementSdEclNeon.value!!
                            || vmSpeOperationViewModel._equipementSdEclLumaphore.value!!
                            || vmSpeOperationViewModel._equipementSdEclBaby.value!!)
                }
                .setMultiChoiceItems(R.array.sd_eclairage, checkedItemsGrEClairage) { dialog, which, checked ->
                    when (which) {
                        0 -> vmSpeOperationViewModel._equipementSdEclSolaris.value = checked
                        1 -> vmSpeOperationViewModel._equipementSdEclNeon.value = checked
                        2 -> vmSpeOperationViewModel._equipementSdEclLumaphore.value = checked
                        3 -> vmSpeOperationViewModel._equipementSdEclBaby.value = checked
                    }
                }
                .show()
        }
    }

}
