package com.loic.spe95.speoperations.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.loic.spe95.data.Result
import com.loic.spe95.data.SingleLiveEvent
import com.loic.spe95.speoperations.data.MaterialCyno
import com.loic.spe95.speoperations.data.SpeOperation
import com.loic.spe95.speoperations.data.SpeOperationRepository
import com.loic.spe95.utils.Constants
import com.loic.spe95.utils.getTimestamp
import com.loic.spe95.utils.getType
import com.loic.spe95.utils.toTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class SpeOperationViewModel(
    private val specialtyDocument: String,
    private val repository: SpeOperationRepository
) :
    ViewModel(), CoroutineScope {
    // set coroutine context
    private val compositeJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + compositeJob

    // -- Coroutine jobs
    private var getUserJob: Job? = null

    var speOperationsLd: MutableLiveData<List<SpeOperation>> = MutableLiveData()
    var speOperationLd: MutableLiveData<SpeOperation> = MutableLiveData()
    private val newSpeOperation = SpeOperation()

    private val _operationAdded = SingleLiveEvent<Any>()
    val operationAdded = _operationAdded

    // VM attributes
    val _id: MutableLiveData<String> = MutableLiveData()
    val id: LiveData<String> = _id
    var _idError: MutableLiveData<String> = MutableLiveData()

    val _type: MutableLiveData<String> = MutableLiveData()
    val type: LiveData<String> = _type

    val _motif: MutableLiveData<String> = MutableLiveData()
    val motif: LiveData<String> = _motif
    var _motifError: MutableLiveData<String> = MutableLiveData()

    var _addressError: MutableLiveData<String> = MutableLiveData()
    val _address: MutableLiveData<GeoPoint> = MutableLiveData()
    val address: LiveData<GeoPoint> = _address

    val _startDateTime: MutableLiveData<String> = MutableLiveData()
    val startDateTime: LiveData<String> = _startDateTime

    val _teamUnitChief: MutableLiveData<String> = MutableLiveData()
    val teamUnitChief: LiveData<String> = _teamUnitChief

    val _team: MutableLiveData<List<Int>> = MutableLiveData()
    val team: LiveData<List<Int>> = _team
    var _teamError: MutableLiveData<String> = MutableLiveData()

    var _genericException: MutableLiveData<String> = MutableLiveData()

    val _equipementCynoIpso: MutableLiveData<String> = MutableLiveData()
    val _equipementCynoNano: MutableLiveData<String> = MutableLiveData()
    val _equipementCynoNerone: MutableLiveData<String> = MutableLiveData()
    val _equipementCynoPriaxe: MutableLiveData<String> = MutableLiveData()

    /**
     * Get an operation with its id
     */
    fun fetchSpeOperationInformation(speOperationId: Int) {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result =
                repository.getSpeOperationFromRemoteDB(specialtyDocument, speOperationId)) {
                is Result.Success -> speOperationLd.value = result.data
                //is Result2.Error -> _snackbarText.value = R.string.error_fetching
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }

    //
    /**
     * Get all operations from Firestore for a given specialty
     */
    fun fetchSpeOperationsInformationFromFirestore() {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result = repository.getSpeOperationsFromRemoteDB(specialtyDocument)) {
                is Result.Success -> speOperationsLd.value = result.data
                //is Result2.Error -> _snackbarText.value = R.string.error_fetching
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }

    /**
     * Add an operation into Firestore
     */
    fun addOperationIntoFirestore() {
        newSpeOperation.id = Integer.valueOf(id.value!!)
        newSpeOperation.motif = motif.value!!
        newSpeOperation.type = type.value!!.getType()
        newSpeOperation.agents = _team.value!!
        newSpeOperation.startDate = _startDateTime.value!!.getTimestamp()
        newSpeOperation.address = _address.value!!
        newSpeOperation.unitChief = _teamUnitChief.value
        val listOfMaterials = ArrayList<MaterialCyno>()
        _equipementCynoIpso.value?.let {
            listOfMaterials.add(MaterialCyno(Constants.CYNO_DOG_IPSO, it.toTime()))
        }
        _equipementCynoNano.value?.let {
            listOfMaterials.add(MaterialCyno(Constants.CYNO_DOG_NANO, it.toTime()))
        }
        _equipementCynoNerone.value?.let {
            listOfMaterials.add(MaterialCyno(Constants.CYNO_DOG_NERONE, it.toTime()))
        }
        _equipementCynoPriaxe.value?.let {
            listOfMaterials.add(MaterialCyno(Constants.CYNO_DOG_PRIAXE, it.toTime()))
        }
        newSpeOperation.materialsCyno = listOfMaterials

        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result =
                repository.addSpeOperationIntoRemoteDB(specialtyDocument, newSpeOperation)) {
                is Result.Success -> _operationAdded.call()
                is Result.Error   -> _genericException.value = result.exception.message
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }
}