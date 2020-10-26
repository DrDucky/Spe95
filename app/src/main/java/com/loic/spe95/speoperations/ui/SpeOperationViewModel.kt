package com.loic.spe95.speoperations.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.loic.spe95.data.Result
import com.loic.spe95.data.SingleLiveEvent
import com.loic.spe95.speoperations.data.MaterialCyno
import com.loic.spe95.speoperations.data.MaterialSd
import com.loic.spe95.speoperations.data.SpeOperation
import com.loic.spe95.speoperations.data.SpeOperationRepository
import com.loic.spe95.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ThreadLocalRandom
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

    val _equipementSdLspcc: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElecFixe: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElec22001: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElec22002: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElec30001: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElec30002: MutableLiveData<Boolean> = MutableLiveData(false)

    val _equipementSdEclSolaris: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEclNeon: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEclLumaphore: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEclBaby: MutableLiveData<Boolean> = MutableLiveData(false)

    val _equipementSdPercGrHydrau: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPercTroncDisque: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPercTroncChaine: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPercChaineDiamant: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPercChaineCarbure: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPercPerforateur: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPercOxyDecoupeur: MutableLiveData<Boolean> = MutableLiveData(false)


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
        if (id.value == null) newSpeOperation.id =
            ThreadLocalRandom.current().nextInt(
                100000,
                500000 + 1
            ) else //If there is no id (for "Entrainements" for example), we generate a random ID between 100k & 500k
            newSpeOperation.id = Integer.valueOf(id.value!!)
        newSpeOperation.motif = motif.value!!
        newSpeOperation.type = type.value!!.getType()
        newSpeOperation.agents = _team.value!!
        newSpeOperation.startDate = _startDateTime.value!!.getTimestamp()
        newSpeOperation.address = _address.value!!
        newSpeOperation.unitChief = _teamUnitChief.value

        /**
         * CYNO Operation
         */
        if (specialtyDocument.getFirestoreIdCollection() == Constants.FIRESTORE_CYNO_ID_DOCUMENT) {
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
        }

        /**
         * SD Operation
         */
        if (specialtyDocument.getFirestoreIdCollection() == Constants.FIRESTORE_SD_ID_DOCUMENT) {
            val listOfMaterialsSd = ArrayList<MaterialSd>()

            listOfMaterialsSd.add(MaterialSd(Constants.SD_LSPCC, _equipementSdLspcc.value))
            listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_FIXE, _equipementSdGrElecFixe.value))
            listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_22001, _equipementSdGrElec22001.value))
            listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_22002, _equipementSdGrElec22002.value))
            listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_30001, _equipementSdGrElec30001.value))
            listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_30002, _equipementSdGrElec30002.value))
            listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_SOLARIS, _equipementSdEclSolaris.value))
            listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_NEON, _equipementSdEclNeon.value))
            listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_LUMAPHORE, _equipementSdEclLumaphore.value))
            listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_BABY, _equipementSdEclBaby.value))

            newSpeOperation.materialsSd = listOfMaterialsSd
        }

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