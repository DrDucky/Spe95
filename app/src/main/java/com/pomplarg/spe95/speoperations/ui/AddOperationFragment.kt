package com.pomplarg.spe95.speoperations.ui

import android.app.Activity.RESULT_OK
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.GeoPoint
import com.pomplarg.spe95.BuildConfig
import com.pomplarg.spe95.R
import com.pomplarg.spe95.ToolbarTitleListener
import com.pomplarg.spe95.agent.data.Agent
import com.pomplarg.spe95.agent.ui.AgentViewModel
import com.pomplarg.spe95.databinding.*
import com.pomplarg.spe95.speoperations.data.AgentOnOperation
import com.pomplarg.spe95.utils.Constants
import com.pomplarg.spe95.utils.hasConnectivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class AddOperationFragment : Fragment() {

    private lateinit var specialtyDocument: String
    private lateinit var imageRA: ImageView
    private val args: AddOperationFragmentArgs by navArgs()
    private val agentViewModel: AgentViewModel by viewModel()
    private val regex = Regex("\\d*:\\d*")
    private val speOperationViewModel: SpeOperationViewModel by viewModel {
        parametersOf(
            specialtyDocument
        )
    }
    lateinit var currentPhotoPath: String


    /* List of Agent & time. Exemple :
    Paul ID : 8
    Time on operation : 01:15
    hashmap = <8, Paul - 01:15>

    Pierre ID : 9
    Time on operation : 02:30
    teamList = (<8, Paul - 01:15>, <9, Pierre - 02:30>)
     */
    private val teamListWithTime = arrayListOf<AgentOnOperation>()

    private val teamList = arrayListOf<AgentOnOperation>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAddOperationBinding.inflate(inflater, container, false)
        val bindingListEquipmentCyno =
            ListItemAddOperationEquipmentCynoBinding.inflate(inflater, container, false)
        val bindingListEquipmentSd =
            ListItemAddOperationEquipmentSdBinding.inflate(inflater, container, false)
        val bindingListEquipmentRa =
            ListItemAddOperationEquipmentRaBinding.inflate(inflater, container, false)
        val bindingListEquipmentRaRegulation =
            ListItemAddOperationEquipmentRaRegulationBinding.inflate(inflater, container, false)
        val bindingListEquipmentCynoRegulation =
            ListItemAddOperationEquipmentCynoRegulationBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        context ?: return binding.root
        imageRA = bindingListEquipmentRa.ivAddPicture


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
            bindingListEquipmentRa,
            bindingListEquipmentRaRegulation,
            bindingListEquipmentCynoRegulation,
            connected
        )
        binding.vmSpeOperation = speOperationViewModel
        bindingListEquipmentSd.vmSpeOperation = speOperationViewModel
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
                LatLng(34.6036508, -14.3554670),
                LatLng(71.1969183, 69.1406268)
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

        binding.actvUnitChief.setOnItemClickListener { parent, arg1, position, arg3 ->
            val selected = parent.getItemAtPosition(position) as Agent
            binding.actvUnitChief.setText(getString(R.string.add_operation_chip_team_text, selected.firstname, selected.lastname))
            speOperationViewModel._teamUnitChief.value = selected.id
            addChipToGroup(selected, binding.chipGroupTeam)
        }

        binding.btnAddOperation.setOnClickListener(View.OnClickListener {
            if (validate(speOperationViewModel, binding, bindingListEquipmentSd, args.specialty)) {
                speOperationViewModel._teamAgent.value = teamListWithTime
                displayMainBloc(binding, false)
                speOperationViewModel.addOperationIntoFirestore()
            }
        })

        context?.let {
            if (!it.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                bindingListEquipmentRa.btnAddPicture.visibility = View.GONE
            }
        }
        bindingListEquipmentRa.btnAddPicture.setOnClickListener(View.OnClickListener {
            context?.let { context ->
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    // Ensure that there's a camera activity to handle the intent
                    takePictureIntent.resolveActivity(context.packageManager)?.also {
                        // Create the File where the photo should go
                        val photoFile: File? = try {
                            createImageFile(context)
                        } catch (ex: IOException) {
                            // Error occurred while creating the File
                            Toast.makeText(context, "Erreur lors de la création de la photo", Toast.LENGTH_LONG).show()
                            null
                        }
                        // Continue only if the File was successfully created
                        photoFile?.also {
                            val photoURI: Uri = FileProvider.getUriForFile(
                                context,
                                BuildConfig.APPLICATION_ID + ".fileprovider",
                                it
                            )
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            takePictureLauncher.launch(takePictureIntent)
                        }
                    }
                }
            }
        })
        bindingListEquipmentRa.btnSelectPicture.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            pickImageLauncher.launch(Intent.createChooser(intent, resources.getString(R.string.add_operation_select_picture)))
        }

        val currentTimeInMillis = Calendar.getInstance().timeInMillis
        speOperationViewModel._startDate.value = currentTimeInMillis
        speOperationViewModel._startTime.value = currentTimeInMillis

        //Treatments
        agentViewModel.fetchAllAgentsPerSpecialty(specialtyDocument)

        //Observables
        speOperationViewModel.ldOperationAdded.observe(viewLifecycleOwner, Observer {
            if (it) {
                displayMainBloc(binding, true)
                Snackbar.make(
                    requireView(),
                    getString(R.string.add_operation_operation_added),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                displayMainBloc(binding, true)
                Snackbar.make(
                    requireView(),
                    getString(R.string.add_operation_exception_generic),
                    Snackbar.LENGTH_LONG
                ).show()
            }
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
                teamListWithTime.add(selectedAgent)
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
        if (teamList.contains(selectedAgent)) {
            context?.let {
                MaterialAlertDialogBuilder(it)
                    .setTitle(it.resources.getString(R.string.add_operation_popup_agent_title))
                    .setMessage(it.resources.getString(R.string.add_operation_popup_agent_message))
                    .setPositiveButton(it.resources.getString(android.R.string.ok)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        } else {
            teamList.add(selectedAgent)
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
                teamListWithTime.remove(selectedAgent)
                chipGroup.removeView(chip as View)
            }
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
        bindingListEquipmentRa: ListItemAddOperationEquipmentRaBinding,
        bindingListEquipmentRaRegulation: ListItemAddOperationEquipmentRaRegulationBinding,
        bindingListEquipmentCynoRegulation: ListItemAddOperationEquipmentCynoRegulationBinding,
        connected: Boolean,
    ) {

        bindingListEquipmentCyno.vmSpeOperation = vmSpeOperationViewModel
        bindingListEquipmentSd.vmSpeOperation = vmSpeOperationViewModel
        bindingListEquipmentRa.vmSpeOperation = vmSpeOperationViewModel
        bindingListEquipmentRaRegulation.vmSpeOperation = vmSpeOperationViewModel
        bindingListEquipmentCynoRegulation.vmSpeOperation = vmSpeOperationViewModel

        //Checkboxes cliquables (ouverture de popup)
        bindingListEquipmentSd.lspcc.setOnClickListener { buttonView ->
            context?.let {
                setEquipmentPopupCheckboxes(
                    it,
                    buttonView,
                    speOperationViewModel
                )
            }
        }
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
            Constants.FIRESTORE_CYNO_DOCUMENT -> layoutToAdd =
                if (vmSpeOperationViewModel.type.value!!
                    == Constants.TYPE_OPERATION_REGULATION
                ) {
                    bindingListEquipmentCynoRegulation.root
                } else {
                    bindingListEquipmentCyno.root
                }
            Constants.FIRESTORE_SD_DOCUMENT   -> layoutToAdd = bindingListEquipmentSd.root
            Constants.FIRESTORE_RA_DOCUMENT   -> layoutToAdd =
                if (vmSpeOperationViewModel.type.value!!
                    == Constants.TYPE_OPERATION_REGULATION
                ) {
                    bindingListEquipmentRaRegulation.root
                } else {
                    bindingListEquipmentRa.root
                }
        }

        binding.equipment.addView(layoutToAdd)

        //Motifs
        var arrayForMotifs: Int = R.array.motifs_cyno //Default value
        when (specialtyId) {
            Constants.FIRESTORE_CYNO_DOCUMENT -> arrayForMotifs = R.array.motifs_cyno
            Constants.FIRESTORE_SD_DOCUMENT   -> arrayForMotifs = R.array.motifs_sd
            Constants.FIRESTORE_RA_DOCUMENT   -> arrayForMotifs = R.array.motifs_ra
        }
        val adapter =
            ArrayAdapter(
                this.requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(arrayForMotifs).asList()
            )
        binding.actvMotif.setAdapter(adapter)

        binding.connected = connected

        binding.btnDate.setOnClickListener {
            val maxDate = Calendar.getInstance().timeInMillis
            val constraintsBuilder = CalendarConstraints.Builder()
            constraintsBuilder.setValidator(DateValidatorPointBackward.before(maxDate))
            val dateBuilder = MaterialDatePicker.Builder.datePicker()
            dateBuilder.setSelection(speOperationViewModel._startDate.value)
            dateBuilder.setCalendarConstraints(constraintsBuilder.build()) //user cannot select future date
            val datePicker = dateBuilder.build()
            datePicker.show(parentFragmentManager, datePicker.toString())
            datePicker.addOnPositiveButtonClickListener {
                speOperationViewModel._startDate.value = it
            }
        }

        binding.btnTime.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentTime.get(Calendar.HOUR_OF_DAY))
                .setMinute(currentTime.get(Calendar.MINUTE))
                .build()
            timePicker.show(parentFragmentManager, timePicker.toString())
            timePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                speOperationViewModel._startTime.value = calendar.timeInMillis
            }
        }
    }

    /**
     * Check for mandatory fields to be completed
     */
    private fun validate(
        vmSpeOperationViewModel: SpeOperationViewModel,
        binding: FragmentAddOperationBinding,
        bindingListEquipmentSd: ListItemAddOperationEquipmentSdBinding,
        specialty: String
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

        val agentsId = ArrayList<String>()
        agentViewModel.agentsLd.value?.let {
            for (agents in it) {
                agentsId.add(agents.id)
            }
        }

        if (binding.tilUnitChief.visibility == View.VISIBLE && !agentsId.contains(vmSpeOperationViewModel.teamUnitChief.value)) {
            vmSpeOperationViewModel._unitChiefError.value = mandatoryFieldError
            isValid = false
        } else {
            vmSpeOperationViewModel._unitChiefError.value = null
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

        /*
        SD Specialty Form Validation specificity
         */
        if (Constants.FIRESTORE_SD_DOCUMENT.equals(specialty)) {
            if (vmSpeOperationViewModel._equipementSdEtaiementBoisGousset.value == false &&
                vmSpeOperationViewModel._equipementSdEtaiementBoisGoussetQuantity.value != null &&
                vmSpeOperationViewModel._equipementSdEtaiementBoisGoussetQuantity.value!! != 0
            ) {
                isValid = false
                bindingListEquipmentSd.tvEtaiementCategoryGousset.error = mandatoryFieldError//app:error does not work...
            } else
                bindingListEquipmentSd.tvEtaiementCategoryGousset.error = null

            if (vmSpeOperationViewModel._equipementSdEtaiementBoisVolige.value == false &&
                vmSpeOperationViewModel._equipementSdEtaiementBoisVoligeQuantity.value != null &&
                vmSpeOperationViewModel._equipementSdEtaiementBoisVoligeQuantity.value!! != 0
            ) {
                isValid = false

                bindingListEquipmentSd.tvEtaiementCategoryVolige.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.tvEtaiementCategoryVolige.error = null

            if (vmSpeOperationViewModel._equipementSdEtaiementBoisChevron.value == false &&
                vmSpeOperationViewModel._equipementSdEtaiementBoisChevronQuantity.value != null &&
                vmSpeOperationViewModel._equipementSdEtaiementBoisChevronQuantity.value!! != 0

            ) {
                isValid = false
                bindingListEquipmentSd.tvEtaiementCategoryChevron.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.tvEtaiementCategoryChevron.error = null

            if (vmSpeOperationViewModel._equipementSdEtaiementBoisBastaing.value == false &&
                vmSpeOperationViewModel._equipementSdEtaiementBoisBastaingQuantity.value != null &&
                vmSpeOperationViewModel._equipementSdEtaiementBoisBastaingQuantity.value!! != 0

            ) {
                isValid = false
                bindingListEquipmentSd.tvEtaiementCategoryBastaing.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.tvEtaiementCategoryBastaing.error = null


            if (vmSpeOperationViewModel._equipementSdEtaiementEtaiMetalPetit.value == false &&
                vmSpeOperationViewModel._equipementSdEtaiementEtaiMetalPetitQuantity.value != null &&
                vmSpeOperationViewModel._equipementSdEtaiementEtaiMetalPetitQuantity.value!! != 0

            ) {
                isValid = false
                bindingListEquipmentSd.tvEtaiementCategoryEtaiMetalliquePetit.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.tvEtaiementCategoryEtaiMetalliquePetit.error = null

            if (vmSpeOperationViewModel._equipementSdEtaiementEtaiMetalMoyen.value == false &&
                vmSpeOperationViewModel._equipementSdEtaiementEtaiMetalMoyenQuantity.value != null &&
                vmSpeOperationViewModel._equipementSdEtaiementEtaiMetalMoyenQuantity.value!! != 0

            ) {
                isValid = false
                bindingListEquipmentSd.tvEtaiementCategoryEtaiMetalliqueMoyen.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.tvEtaiementCategoryEtaiMetalliqueMoyen.error = null

            if (vmSpeOperationViewModel._equipementSdEtaiementEtaiMetalGrand.value == false &&
                vmSpeOperationViewModel._equipementSdEtaiementEtaiMetalGrandQuantity.value != null &&
                vmSpeOperationViewModel._equipementSdEtaiementEtaiMetalGrandQuantity.value!! != 0

            ) {
                isValid = false
                bindingListEquipmentSd.tvEtaiementCategoryEtaiMetalliqueGrand.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.tvEtaiementCategoryEtaiMetalliqueGrand.error = null

            if ((vmSpeOperationViewModel._equipementSdPetitMatVis.value == true &&
                        vmSpeOperationViewModel._equipementSdPetitMatVisQuantity.value != null &&
                        vmSpeOperationViewModel._equipementSdPetitMatVisQuantity.value!! <= 0) ||
                (vmSpeOperationViewModel._equipementSdPetitMatVis.value == false &&
                        vmSpeOperationViewModel._equipementSdPetitMatVisQuantity.value != null &&
                        vmSpeOperationViewModel._equipementSdPetitMatVisQuantity.value!! != 0
                        )
            ) {
                isValid = false
                bindingListEquipmentSd.tipPetitMaterielCategoryVis.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.tipPetitMaterielCategoryVis.error = null

            if ((vmSpeOperationViewModel._equipementSdPetitMatCarburantSP95.value == true &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantSP95Quantity.value != null &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantSP95Quantity.value!! <= 0) ||
                (vmSpeOperationViewModel._equipementSdPetitMatCarburantSP95.value == false &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantSP95Quantity.value != null &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantSP95Quantity.value!! != 0
                        )
            ) {
                isValid = false
                bindingListEquipmentSd.etPetitMaterielCategoryCarburantSp95.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.etPetitMaterielCategoryCarburantSp95.error = null

            if ((vmSpeOperationViewModel._equipementSdPetitMatCarburantMarline.value == true &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantMarlineQuantity.value != null &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantMarlineQuantity.value!! <= 0) ||
                (vmSpeOperationViewModel._equipementSdPetitMatCarburantMarline.value == false &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantMarlineQuantity.value != null &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantMarlineQuantity.value!! != 0
                        )
            ) {
                isValid = false
                bindingListEquipmentSd.tipPetitMaterielCategoryCarburantMarline.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.tipPetitMaterielCategoryCarburantMarline.error = null

            if ((vmSpeOperationViewModel._equipementSdPetitMatCarburantMelange.value == true &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantMelangeQuantity.value != null &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantMelangeQuantity.value!! <= 0) ||
                (vmSpeOperationViewModel._equipementSdPetitMatCarburantMelange.value == false &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantMelangeQuantity.value != null &&
                        vmSpeOperationViewModel._equipementSdPetitMatCarburantMelangeQuantity.value!! != 0
                        )
            ) {
                isValid = false
                bindingListEquipmentSd.tipPetitMaterielCategoryCarburantMelange.error = mandatoryFieldError
            } else
                bindingListEquipmentSd.tipPetitMaterielCategoryCarburantMelange.error = null

        }

        return isValid
    }

    private fun setEquipmentPopupCheckboxes(
        context: Context,
        checkboxView: View,
        vmSpeOperationViewModel: SpeOperationViewModel,
    ) {
        val checkedItemsLspcc = booleanArrayOf(
            vmSpeOperationViewModel._equipementSdLspcc44.value!!,
            vmSpeOperationViewModel._equipementSdLspcc46.value!!,
            vmSpeOperationViewModel._equipementSdLspcc221.value!!,
            vmSpeOperationViewModel._equipementSdLspcc224.value!!,
            vmSpeOperationViewModel._equipementSdLspccLmc1.value!!,
            vmSpeOperationViewModel._equipementSdLspccLmc2.value!!
        )
        val checkedItemsGrElec = booleanArrayOf(
            vmSpeOperationViewModel._equipementSdGrElec21.value!!,
            vmSpeOperationViewModel._equipementSdGrElec22.value!!,
            vmSpeOperationViewModel._equipementSdGrElec31.value!!,
            vmSpeOperationViewModel._equipementSdGrElec32.value!!,
            vmSpeOperationViewModel._equipementSdGrElec51.value!!
        )
        val checkedItemsGrEClairage = booleanArrayOf(
            vmSpeOperationViewModel._equipementSdEclSolaris.value!!,
            vmSpeOperationViewModel._equipementSdEclNeon.value!!,
            vmSpeOperationViewModel._equipementSdEclSolaris.value!!,
            vmSpeOperationViewModel._equipementSdEclBaby1.value!!,
            vmSpeOperationViewModel._equipementSdEclBaby2.value!!
        )
        val checkbox = checkboxView as CompoundButton
        when (checkbox.id) {
            R.id.lspcc -> {
                MaterialAlertDialogBuilder(context)
                    .setTitle(context.resources.getString(R.string.equipment_sauvetage_lspcc))
                    .setPositiveButton(context.resources.getString(android.R.string.ok)) { dialog, which ->
                        //Nothing to do, closes automatically with multiChoiceItems
                        //Except set the checkbox check if at least one of the item has been selected
                        checkbox.isChecked = (vmSpeOperationViewModel._equipementSdLspcc44.value!!
                                || vmSpeOperationViewModel._equipementSdLspcc46.value!!
                                || vmSpeOperationViewModel._equipementSdLspcc221.value!!
                                || vmSpeOperationViewModel._equipementSdLspcc224.value!!
                                || vmSpeOperationViewModel._equipementSdLspccLmc1.value!!
                                || vmSpeOperationViewModel._equipementSdLspccLmc2.value!!)
                    }
                    .setMultiChoiceItems(R.array.sd_lspcc, checkedItemsLspcc) { dialog, which, checked ->
                        when (which) {
                            0 -> vmSpeOperationViewModel._equipementSdLspcc44.value = checked
                            1 -> vmSpeOperationViewModel._equipementSdLspcc46.value = checked
                            2 -> vmSpeOperationViewModel._equipementSdLspcc221.value = checked
                            3 -> vmSpeOperationViewModel._equipementSdLspcc224.value = checked
                            4 -> vmSpeOperationViewModel._equipementSdLspccLmc1.value = checked
                            5 -> vmSpeOperationViewModel._equipementSdLspccLmc2.value = checked
                        }
                    }
                    .show()
            }
            R.id.tv_eclairage_category_groupe_electro -> {
                MaterialAlertDialogBuilder(context)
                    .setTitle(context.resources.getString(R.string.equipment_eclairage_groupe_electro))
                    .setPositiveButton(context.resources.getString(android.R.string.ok)) { dialog, which ->
                        //Nothing to do, closes automatically with multiChoiceItems
                        //Except set the checkbox check if at least one of the item has been selected
                        checkbox.isChecked = (vmSpeOperationViewModel._equipementSdGrElec21.value!!
                                || vmSpeOperationViewModel._equipementSdGrElec22.value!!
                                || vmSpeOperationViewModel._equipementSdGrElec31.value!!
                                || vmSpeOperationViewModel._equipementSdGrElec32.value!!
                                || vmSpeOperationViewModel._equipementSdGrElec51.value!!)
                    }
                    .setMultiChoiceItems(R.array.sd_gr_elec, checkedItemsGrElec) { dialog, which, checked ->
                        when (which) {
                            0 -> vmSpeOperationViewModel._equipementSdGrElec21.value = checked
                            1 -> vmSpeOperationViewModel._equipementSdGrElec22.value = checked
                            2 -> vmSpeOperationViewModel._equipementSdGrElec31.value = checked
                            3 -> vmSpeOperationViewModel._equipementSdGrElec32.value = checked
                            4 -> vmSpeOperationViewModel._equipementSdGrElec51.value = checked
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
                                || vmSpeOperationViewModel._equipementSdEclBaby1.value!!
                                || vmSpeOperationViewModel._equipementSdEclBaby2.value!!)
                    }
                    .setMultiChoiceItems(R.array.sd_eclairage, checkedItemsGrEClairage) { dialog, which, checked ->
                        when (which) {
                            0 -> vmSpeOperationViewModel._equipementSdEclSolaris.value = checked
                            1 -> vmSpeOperationViewModel._equipementSdEclNeon.value = checked
                            2 -> vmSpeOperationViewModel._equipementSdEclLumaphore.value = checked
                            3 -> vmSpeOperationViewModel._equipementSdEclBaby1.value = checked
                            4 -> vmSpeOperationViewModel._equipementSdEclBaby2.value = checked
                        }
                    }
                    .show()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private var pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data as Uri
            val inputStream: InputStream? = context?.contentResolver?.openInputStream(imageUri)
            if (inputStream != null) {
                val imageBitmap = BitmapFactory.decodeStream(inputStream)
                imageRA.setImageBitmap(imageBitmap)
                speOperationViewModel._ldPhotoRaAbsolutePath.value = imageUri
                inputStream.close()
            }
        }

    }

    private var takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = File(currentPhotoPath)
            val bitmap = BitmapFactory.decodeFile(file.path)
            imageRA.setImageBitmap(bitmap)
            speOperationViewModel._ldPhotoRaAbsolutePath.value = file.toUri()
        }
    }
}



