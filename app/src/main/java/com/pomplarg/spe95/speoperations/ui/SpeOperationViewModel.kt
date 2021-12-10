package com.pomplarg.spe95.speoperations.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.data.SingleLiveEvent
import com.pomplarg.spe95.speoperations.data.*
import com.pomplarg.spe95.statistiques.data.StatistiqueRepository
import com.pomplarg.spe95.utils.Constants
import com.pomplarg.spe95.utils.getTimestamp
import com.pomplarg.spe95.utils.toTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext


class SpeOperationViewModel(
    private val specialtyDocument: String,
    private val repository: SpeOperationRepository,
    private val statsRepository: StatistiqueRepository
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

    val _ldOperationAdded: MutableLiveData<Boolean> = MutableLiveData()
    val ldOperationAdded: LiveData<Boolean> = _ldOperationAdded

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

    val _observations: MutableLiveData<String> = MutableLiveData()
    val _requisitionPolice: MutableLiveData<Boolean> = MutableLiveData(false)

    var _startDate: MutableLiveData<Long> = MutableLiveData()
    val startDate: LiveData<Long> = _startDate

    var _startTime: MutableLiveData<Long> = MutableLiveData()
    val startTime: LiveData<Long> = _startTime

    val _teamUnitChief: MutableLiveData<String> = MutableLiveData()
    val teamUnitChief: LiveData<String> = _teamUnitChief

    val _teamAgent: MutableLiveData<List<AgentOnOperation>> = MutableLiveData()
    val teamAgent: LiveData<List<AgentOnOperation>> = _teamAgent

    val _team: MutableLiveData<List<Int>> = MutableLiveData()
    val team: LiveData<List<Int>> = _team
    var _teamError: MutableLiveData<String> = MutableLiveData()
    var _unitChiefError: MutableLiveData<String> = MutableLiveData()

    var _genericException: MutableLiveData<String> = MutableLiveData()

    val _equipementCynoIpso: MutableLiveData<String> = MutableLiveData()
    val _equipementCynoNano: MutableLiveData<String> = MutableLiveData()
    val _equipementCynoNerone: MutableLiveData<String> = MutableLiveData()
    val _equipementCynoPriaxe: MutableLiveData<String> = MutableLiveData()
    val _equipementCynoSniper: MutableLiveData<String> = MutableLiveData()

    val _equipementSdLspcc44: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdLspcc46: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdLspcc221: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdLspcc224: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdLspccLmc1: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdLspccLmc2: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEchCoulisse: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdTelestep: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdKitTyrolienne: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdKitTelepherique: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElec21: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElec22: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElec31: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElec32: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdGrElec51: MutableLiveData<Boolean> = MutableLiveData(false)

    val _equipementSdEclSolaris: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEclNeon: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEclLumaphore: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEclBaby1: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEclBaby2: MutableLiveData<Boolean> = MutableLiveData(false)

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
    val _equipementSdEtaiementEtaiMetalPetitQuantity: MutableLiveData<Int> = MutableLiveData(0)
    val _equipementSdEtaiementEtaiMetalMoyenQuantity: MutableLiveData<Int> = MutableLiveData(0)
    val _equipementSdEtaiementEtaiMetalGrandQuantity: MutableLiveData<Int> = MutableLiveData(0)

    val _equipementSdEtaiementBoisGousset: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementBoisVolige: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementBoisChevron: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdEtaiementBoisBastaing: MutableLiveData<Boolean> = MutableLiveData(false)

    val _equipementSdEtaiementBoisGoussetQuantity: MutableLiveData<Int> = MutableLiveData(0)
    val _equipementSdEtaiementBoisVoligeQuantity: MutableLiveData<Int> = MutableLiveData(0)
    val _equipementSdEtaiementBoisChevronQuantity: MutableLiveData<Int> = MutableLiveData(0)
    val _equipementSdEtaiementBoisBastaingQuantity: MutableLiveData<Int> = MutableLiveData(0)

    val _equipementSdExplorationAsb6: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdExplorationAsb8: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdExplorationAsb9: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdExplorationCamera: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatVisseuse: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatVis: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatVisQuantity: MutableLiveData<Int> = MutableLiveData(0)
    val _equipementSdPetitMatCordage: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatServante: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatCarburantSP95: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatCarburantMarline: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatCarburantMelange: MutableLiveData<Boolean> = MutableLiveData(false)
    val _equipementSdPetitMatCarburantSP95Quantity: MutableLiveData<Int> = MutableLiveData(0)
    val _equipementSdPetitMatCarburantMarlineQuantity: MutableLiveData<Int> = MutableLiveData(0)
    val _equipementSdPetitMatCarburantMelangeQuantity: MutableLiveData<Int> = MutableLiveData(0)
    val _equipementSdPetitMatCarburantHuileChaine: MutableLiveData<Boolean> = MutableLiveData(false)

    val _enginSdVlrSd: MutableLiveData<Boolean> = MutableLiveData(false)
    val _enginSdCeSd: MutableLiveData<Boolean> = MutableLiveData(false)
    val _enginSdVtuSdArg: MutableLiveData<Boolean> = MutableLiveData(false)
    val _enginSdVtuSdCep: MutableLiveData<Boolean> = MutableLiveData(false)
    val _enginSdVtuSdBez: MutableLiveData<Boolean> = MutableLiveData(false)
    val _enginSdVtuSdGra: MutableLiveData<Boolean> = MutableLiveData(false)
    val _enginSdVidSpe: MutableLiveData<Boolean> = MutableLiveData(false)

    val _especeDog: MutableLiveData<Boolean> = MutableLiveData(false)
    val _especeCat: MutableLiveData<Boolean> = MutableLiveData(false)
    val _especeOther: MutableLiveData<String> = MutableLiveData()

    val _animalTypeDomestique: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalTypeSauvage: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalTypeElevage: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalTypeAutochtone: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalPriseChargeCooperatif: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalPriseChargeMenacant: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalPriseChargeBlesse: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalPriseChargeMordeur: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalPriseChargeMort: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalPriseChargeAnesthesie: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalDestinationCentreRegroupement: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalDestinationCedaf: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalDestinationProprietaire: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalDestinationCliniqueVeterinaire: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalDestinationFuite: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalDestinationOther: MutableLiveData<String> = MutableLiveData()
    val _animalTransportVira: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalTransportVtu: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalTransportOther: MutableLiveData<String> = MutableLiveData()
    val _animalEnginsSurLieuxVtu: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalEnginsSurLieuxFpt: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalEnginsSurLieuxVsr: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalEnginsSurLieuxMoyenAerien: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalEnginsSurLieuxVsav: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalEnginsSurLieuxVsso: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalSpecialitesSurLieuxGrimp: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalSpecialitesSurLieuxSd: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalSpecialitesSurLieuxNautique: MutableLiveData<Boolean> = MutableLiveData(false)
    val _animalSpecialitesSurLieuxGred: MutableLiveData<Boolean> = MutableLiveData(false)

    val _actionApproche: MutableLiveData<Boolean> = MutableLiveData(false)
    val _actionIdentification: MutableLiveData<Boolean> = MutableLiveData(false)
    val _actionNeutralisation: MutableLiveData<Boolean> = MutableLiveData(false)
    val _actionCapture: MutableLiveData<Boolean> = MutableLiveData(false)
    val _actionRelevage: MutableLiveData<Boolean> = MutableLiveData(false)
    val _actionAssistance: MutableLiveData<Boolean> = MutableLiveData(false)
    val _actionConditionnement: MutableLiveData<Boolean> = MutableLiveData(false)
    val _actionTransport: MutableLiveData<Boolean> = MutableLiveData(false)

    val _ldPhotoRaAbsolutePath: MutableLiveData<Uri> = MutableLiveData()

    /**
     * Get an operation with its id
     */
    fun fetchSpeOperationInformation(speOperationId: Long) {
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
        val cDate = Calendar.getInstance()
        newSpeOperation.id = cDate.time.time
        id.value?.let {
            newSpeOperation.idIntervention = Integer.valueOf(it)
        }
        newSpeOperation.motif = motif.value!!
        newSpeOperation.type = type.value!!

        newSpeOperation.agentOnOperation = _teamAgent.value!!

        //newSpeOperation.agents = _team.value!!
        cDate.timeInMillis = _startDate.value!!
        val cTime = Calendar.getInstance()
        cTime.timeInMillis = _startTime.value!!
        val cDateAndTime = Calendar.getInstance()
        cDateAndTime.set(cDate.get(Calendar.YEAR), cDate.get(Calendar.MONTH), cDate.get(Calendar.DAY_OF_MONTH), cTime.get(Calendar.HOUR_OF_DAY), cTime.get(Calendar.MINUTE))
        newSpeOperation.startDate = cDateAndTime.timeInMillis.getTimestamp()
        newSpeOperation.address = _address.value
        newSpeOperation.addressOffline = _addressOffline.value
        newSpeOperation.observations = _observations.value
        newSpeOperation.requisitionPolice = _requisitionPolice.value
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
            _equipementCynoSniper.value?.let {
                listOfMaterials.add(MaterialCyno(Constants.CYNO_DOG_SNIPER, it.toTime()))
            }
            newSpeOperation.materialsCyno = listOfMaterials
        }

        /**
         * SD Operation
         */
        if (specialtyDocument == Constants.FIRESTORE_SD_DOCUMENT) {
            val listOfMaterialsSd = ArrayList<MaterialSd>()
            val listOfEnginsSd = ArrayList<EnginSd>()

            if (_equipementSdLspcc44.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_LSPCC44))
            if (_equipementSdLspcc46.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_LSPCC46))
            if (_equipementSdLspcc221.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_LSPCC221))
            if (_equipementSdLspcc224.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_LSPCC224))
            if (_equipementSdLspccLmc1.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_LSPCCLMC1))
            if (_equipementSdLspccLmc2.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_LSPCCLMC2))
            if (_equipementSdEchCoulisse.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECH_COULISSE))
            if (_equipementSdTelestep.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_TELESTEP))
            if (_equipementSdKitTyrolienne.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_KIT_TYROLIENNE))
            if (_equipementSdKitTelepherique.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_KIT_TELEPHERIQUE))
            if (_equipementSdGrElec21.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_21))
            if (_equipementSdGrElec22.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_22))
            if (_equipementSdGrElec31.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_31))
            if (_equipementSdGrElec32.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_32))
            if (_equipementSdGrElec51.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_GR_ELEC_51))
            if (_equipementSdEclSolaris.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_SOLARIS))
            if (_equipementSdEclNeon.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_NEON))
            if (_equipementSdEclLumaphore.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_LUMAPHORE))
            if (_equipementSdEclBaby1.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_BABY1))
            if (_equipementSdEclBaby2.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ECL_BABY2))

            if (_equipementSdPercGrHydrau.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_GRP_HYDRAU))
            if (_equipementSdPercTroncDisque.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_TRONC_DISQUE))
            if (_equipementSdPercTroncChaine.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_TRONC_CHAINE))
            if (_equipementSdPercChaineDiamant.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_CHAINE_DIAM))
            if (_equipementSdPercChaineCarbure.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_CHAINE_CARB))
            if (_equipementSdPercPerforateur.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_PERFORATEUR))
            if (_equipementSdPercOxyDecoupeur.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PERC_OXYDECOUPEUR))
            if (_equipementSdForceTirfor.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_FORCE_TIRFOR))
            if (_equipementSdForceSangleText.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_FORCE_SANG_TEXT))
            if (_equipementSdForceCoussinLevage.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_FORCE_COUSS_LEVAGE))
            if (_equipementSdEtaiementEtaiPneu.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_PNEUM))
            if (_equipementSdEtaiementEtaiMetalPetit.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_METAL_PETIT, _equipementSdEtaiementEtaiMetalPetitQuantity.value))
            if (_equipementSdEtaiementEtaiMetalMoyen.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_METAL_MOYEN, _equipementSdEtaiementEtaiMetalMoyenQuantity.value))
            if (_equipementSdEtaiementEtaiMetalGrand.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_METAL_GRAND, _equipementSdEtaiementEtaiMetalGrandQuantity.value))
            if (_equipementSdEtaiementBoisGousset.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_BOIS_GOUSSET, _equipementSdEtaiementBoisGoussetQuantity.value))
            if (_equipementSdEtaiementBoisVolige.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_BOIS_VOLIGE, _equipementSdEtaiementBoisVoligeQuantity.value))
            if (_equipementSdEtaiementBoisChevron.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_BOIS_CHEVRON, _equipementSdEtaiementBoisChevronQuantity.value))
            if (_equipementSdEtaiementBoisBastaing.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_ETAIEMENT_BOIS_BASTAING, _equipementSdEtaiementBoisBastaingQuantity.value))
            if (_equipementSdExplorationAsb6.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_EXPLO_ASB6))
            if (_equipementSdExplorationAsb8.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_EXPLO_ASB8))
            if (_equipementSdExplorationAsb9.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_EXPLO_ASB9))
            if (_equipementSdExplorationCamera.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_EXPLO_CAMERA))
            if (_equipementSdPetitMatVisseuse.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_VISSEUSE))
            if (_equipementSdPetitMatVis.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_VIS, _equipementSdPetitMatVisQuantity.value))
            if (_equipementSdPetitMatCordage.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CORDAGE))
            if (_equipementSdPetitMatServante.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_SERVANTE))
            if (_equipementSdPetitMatCarburantSP95.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CARBURANT_SP95, _equipementSdPetitMatCarburantSP95Quantity.value))
            if (_equipementSdPetitMatCarburantMarline.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CARBURANT_MARLINE, _equipementSdPetitMatCarburantMarlineQuantity.value))
            if (_equipementSdPetitMatCarburantMelange.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CARBURANT_MELANGE, _equipementSdPetitMatCarburantMelangeQuantity.value))
            if (_equipementSdPetitMatCarburantHuileChaine.value == true) listOfMaterialsSd.add(MaterialSd(Constants.SD_PETIT_MAT_CARBURANT_HUILE_CHAINE))

            if (_enginSdVlrSd.value == true) listOfEnginsSd.add(EnginSd(Constants.SD_ENGINS_VLRSD))
            if (_enginSdCeSd.value == true) listOfEnginsSd.add(EnginSd(Constants.SD_ENGINS_CESD))
            if (_enginSdVtuSdArg.value == true) listOfEnginsSd.add(EnginSd(Constants.SD_ENGINS_VTU_ARG))
            if (_enginSdVtuSdCep.value == true) listOfEnginsSd.add(EnginSd(Constants.SD_ENGINS_VTU_CEP))
            if (_enginSdVtuSdBez.value == true) listOfEnginsSd.add(EnginSd(Constants.SD_ENGINS_VTU_BEZ))
            if (_enginSdVtuSdGra.value == true) listOfEnginsSd.add(EnginSd(Constants.SD_ENGINS_VTU_GRA))
            if (_enginSdVidSpe.value == true) listOfEnginsSd.add(EnginSd(Constants.SD_ENGINS_VID_SPE))

            newSpeOperation.materialsSd = listOfMaterialsSd
            newSpeOperation.enginsSd = listOfEnginsSd
        }

        /**
         * RA Operation
         */
        if (specialtyDocument == Constants.FIRESTORE_RA_DOCUMENT) {
            val listOfMaterials = ArrayList<MaterialRa>()
            if (_especeCat.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_ESPECE_CAT))
            if (_especeDog.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_ESPECE_DOG))
            if (!_especeOther.value.isNullOrEmpty()) listOfMaterials.add(MaterialRa((Constants.RA_ANIMAL_ESPECE_OTHER) + _especeOther.value))

            if (_animalTypeDomestique.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_TYPE_DOMESTIQUE))
            if (_animalTypeSauvage.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_TYPE_SAUVAGE))
            if (_animalTypeElevage.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_TYPE_ELEVAGE))
            if (_animalTypeAutochtone.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_TYPE_AUTOCHTONE))
            if (_animalPriseChargeCooperatif.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_PRISE_CHARGE_COOPERATIF))
            if (_animalPriseChargeMenacant.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_PRISE_CHARGE_MENACANT))
            if (_animalPriseChargeBlesse.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_PRISE_CHARGE_BLESSE))
            if (_animalPriseChargeMordeur.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_PRISE_CHARGE_MORDEUR))
            if (_animalPriseChargeMort.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_PRISE_CHARGE_MORT))
            if (_animalPriseChargeAnesthesie.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_PRISE_CHARGE_ANESTHESIE))
            if (_animalDestinationCentreRegroupement.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_DESTINATION_CENTRE_REGROUPEMENT))
            if (_animalDestinationCedaf.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_DESTINATION_CEDAF))
            if (_animalDestinationProprietaire.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_DESTINATION_PROPRIETAIRE))
            if (_animalDestinationCliniqueVeterinaire.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_DESTINATION_CLINIQUE_VETERINAIRE))
            if (_animalDestinationFuite.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_DESTINATION_FUITE))
            if (!_animalDestinationOther.value.isNullOrEmpty()) listOfMaterials.add(MaterialRa((Constants.RA_ANIMAL_DESTINATION_OTHER) + _animalDestinationOther.value))
            if (_animalTransportVira.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_TRANSPORT_VIRA))
            if (_animalTransportVtu.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_TRANSPORT_VTU))
            if (!_animalTransportOther.value.isNullOrEmpty()) listOfMaterials.add(MaterialRa((Constants.RA_ANIMAL_TRANSPORT_OTHER) + _animalTransportOther.value))
            if (_animalEnginsSurLieuxVtu.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_ENGINS_SUR_LIEUX_VTU))
            if (_animalEnginsSurLieuxFpt.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_ENGINS_SUR_LIEUX_FPT))
            if (_animalEnginsSurLieuxVsr.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_ENGINS_SUR_LIEUX_VSR))
            if (_animalEnginsSurLieuxMoyenAerien.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_ENGINS_SUR_LIEUX_MOYEN_AERIEN))
            if (_animalEnginsSurLieuxVsav.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_ENGINS_SUR_LIEUX_VSAV))
            if (_animalEnginsSurLieuxVsso.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_ENGINS_SUR_LIEUX_VSSO))
            if (_animalSpecialitesSurLieuxGrimp.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_SPECIALITES_SUR_LIEUX_GRIMP))
            if (_animalSpecialitesSurLieuxSd.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_SPECIALITES_SUR_LIEUX_SD))
            if (_animalSpecialitesSurLieuxNautique.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_SPECIALITES_SUR_LIEUX_NAUTIQUE))
            if (_animalSpecialitesSurLieuxGred.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ANIMAL_SPECIALITES_SUR_LIEUX_GRED))

            if (_actionApproche.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ACTION_APPROCHE))
            if (_actionIdentification.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ACTION_IDENTIFICATION))
            if (_actionNeutralisation.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ACTION_NEUTRALISATION))
            if (_actionCapture.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ACTION_CAPTURE))
            if (_actionRelevage.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ACTION_RELEVAGE))
            if (_actionAssistance.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ACTION_ASSISTANCE))
            if (_actionConditionnement.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ACTION_CONDITIONNEMENT))
            if (_actionTransport.value == true) listOfMaterials.add(MaterialRa(Constants.RA_ACTION_TRANSPORT))

            newSpeOperation.materialsRa = listOfMaterials
        }

        repository.addSpeOperationIntoRemoteDB(specialtyDocument, newSpeOperation, _ldOperationAdded, _ldPhotoRaAbsolutePath)

    }

    fun addPostOperation() {
        val speOperationDate = Date(newSpeOperation.startDate!!.seconds * 1000)
        val c = Calendar.getInstance()
        c.time = speOperationDate
        val monthNumber = c.get(Calendar.MONTH) + 1 //return 1-12

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        statsRepository.addOperationStats(specialtyDocument, currentYear.toString(), newSpeOperation.type, newSpeOperation.motif)
        statsRepository.addMaterialStat(specialtyDocument, currentYear.toString(), newSpeOperation.type, newSpeOperation.materialsCyno, newSpeOperation.materialsSd, newSpeOperation.requisitionPolice)
        statsRepository.addAgentStats(specialtyDocument, currentYear.toString(), monthNumber, newSpeOperation.type, newSpeOperation.agentOnOperation)
        if (newSpeOperation.requisitionPolice == true)
            statsRepository.addRequisitionPolice(specialtyDocument, currentYear.toString(), newSpeOperation.type)
        _operationAdded.call()
    }
}