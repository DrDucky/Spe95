package com.pomplarg.spe95.speoperations.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.data.SingleLiveEvent
import com.pomplarg.spe95.speoperations.data.*
import com.pomplarg.spe95.utils.Constants
import com.pomplarg.spe95.utils.getTimestamp
import com.pomplarg.spe95.utils.toTime
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

    var _addressOfflineError: MutableLiveData<String> = MutableLiveData()
    val _addressOffline: MutableLiveData<String> = MutableLiveData()
    val addressOffline: LiveData<String> = _addressOffline

    val _startDateTime: MutableLiveData<String> = MutableLiveData()
    val startDateTime: LiveData<String> = _startDateTime

    val _teamUnitChief: MutableLiveData<String> = MutableLiveData()
    val teamUnitChief: LiveData<String> = _teamUnitChief

    val _teamAgent: MutableLiveData<List<AgentOnOperation>> = MutableLiveData()
    val teamAgent: LiveData<List<AgentOnOperation>> = _teamAgent

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

    val _equipementSdForceTirfor: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdForceSangleText: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdForceCoussinLevage: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementEtaiPneu: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementEtaiMetalPetit: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementEtaiMetalMoyen: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementEtaiMetalGrand: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementBoisGousset: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementBoisVolige: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementBoischevron: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementBoisBastaing: MutableLiveData<Boolean> = MutableLiveData(false)

    val _equipementSdExplorationAsb8: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdExplorationCamera: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatVisseuse: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatCordage: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatServante: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatCarburantSP95: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatCarburantMarline: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatCarburantMelange: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatCarburantHuileChaine: MutableLiveData<Boolean> = MutableLiveData(false)

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
        newSpeOperation.type = type.value!!

        newSpeOperation.agentOnOperation = _teamAgent.value!!

        //newSpeOperation.agents = _team.value!!
        newSpeOperation.startDate = _startDateTime.value!!.getTimestamp()
        newSpeOperation.address = _address.value
        newSpeOperation.addressOffline = _addressOffline.value
        newSpeOperation.unitChief = _teamUnitChief.value
        newSpeOperation.specialty = specialtyDocument

        /**
         * CYNO Operation
         */
        if (specialtyDocument == Constants.FIRESTORE_CYNO_DOCUMENT) {
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
        if (specialtyDocument == Constants.FIRESTORE_SD_DOCUMENT) {
            val listOfMaterialsSd = ArrayList<MaterialSd>()

            if (_equipementSdLspcc.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_LSPCC, 1))
            if (_equipementSdGrElecFixe.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_FIXE, 1))
            if (_equipementSdGrElec22001.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_22001, 1))
            if (_equipementSdGrElec22002.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_22002, 1))
            if (_equipementSdGrElec30001.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_30001, 1))
            if (_equipementSdGrElec30002.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_30002, 1))
            if (_equipementSdEclSolaris.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_SOLARIS, 1))
            if (_equipementSdEclNeon.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_NEON, 1))
            if (_equipementSdEclLumaphore.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_LUMAPHORE, 1))
            if (_equipementSdEclBaby.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_BABY, 1))

            if (_equipementSdPercGrHydrau.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_GRP_HYDRAU, 1))
            if (_equipementSdPercTroncDisque.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_TRONC_DISQUE, 1))
            if (_equipementSdPercTroncChaine.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_TRONC_CHAINE, 1))
            if (_equipementSdPercChaineDiamant.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_CHAINE_DIAM, 1))
            if (_equipementSdPercChaineCarbure.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_CHAINE_CARB, 1))
            if (_equipementSdPercPerforateur.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_PERFORATEUR, 1))
            if (_equipementSdPercOxyDecoupeur.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_OXYDECOUPEUR, 1))
            if (_equipementSdForceTirfor.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_FORCE_TIRFOR, 1))
            if (_equipementSdForceSangleText.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_FORCE_SANG_TEXT, 1))
            if (_equipementSdForceCoussinLevage.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_FORCE_COUSS_LEVAGE, 1))
            if (_equipementSdEtaiementEtaiPneu.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_PNEUM, 1))
            if (_equipementSdEtaiementEtaiMetalPetit.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_METAL_PETIT, 1))
            if (_equipementSdEtaiementEtaiMetalMoyen.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_METAL_MOYEN, 1))
            if (_equipementSdEtaiementEtaiMetalGrand.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_METAL_GRAND, 1))
            if (_equipementSdEtaiementBoisGousset.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_BOIS_GOUSSET, 1))
            if (_equipementSdEtaiementBoisVolige.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_BOIS_VOLIGE, 1))
            if (_equipementSdEtaiementBoischevron.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_BOIS_CHEVRON, 1))
            if (_equipementSdEtaiementBoisBastaing.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_BOIS_BASTAING, 1))
            if (_equipementSdExplorationAsb8.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_EXPLO_ASB8, 1))
            if (_equipementSdExplorationCamera.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_EXPLO_CAMERA, 1))
            if (_equipementSdPetitMatVisseuse.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_VISSEUSE, 1))
            if (_equipementSdPetitMatCordage.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CORDAGE, 1))
            if (_equipementSdPetitMatServante.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_SERVANTE, 1))
            if (_equipementSdPetitMatCarburantSP95.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CARBURANT_SP95, 1))
            if (_equipementSdPetitMatCarburantMarline.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CARBURANT_MARLINE, 1))
            if (_equipementSdPetitMatCarburantMelange.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CARBURANT_MELANGE, 1))
            if (_equipementSdPetitMatCarburantHuileChaine.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CARBURANT_HUILE_CHAINE, 1))

            newSpeOperation.materialsSd = listOfMaterialsSd
        }

        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result =
                repository.addSpeOperationIntoRemoteDB(specialtyDocument, newSpeOperation, operationAdded)) {
                is Result.Success -> _operationAdded.call()
                is Result.Error -> _genericException.value = result.exception.message
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }
}