package com.pomplarg.spe95.statistiques.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.speoperations.data.DecisionCyno
import com.pomplarg.spe95.speoperations.data.MaterialRa
import com.pomplarg.spe95.speoperations.data.MaterialSd
import com.pomplarg.spe95.speoperations.data.SpeOperation
import com.pomplarg.spe95.statistiques.data.Statistique
import com.pomplarg.spe95.statistiques.data.StatistiqueRepository
import com.pomplarg.spe95.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class StatistiquesViewModel(private val repository: StatistiqueRepository) : ViewModel(), CoroutineScope {

    // set coroutine context
    private val compositeJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + compositeJob

    // -- Coroutine jobs
    private var getRegulationJob: Job? = null
    private var getStatsJob: Job? = null
    private var getStockJob: Job? = null
    private var getAgentJob: Job? = null

    var statsMotifsLd: MutableLiveData<Statistique> = MutableLiveData()
    var statsAgentLd: MutableLiveData<Statistique> = MutableLiveData()
    var statsStocksLd: MutableLiveData<List<MaterialSd>> = MutableLiveData()
    var operationsLd: MutableLiveData<List<SpeOperation>> = MutableLiveData()


    //fetch all statistiques in a specific specialty
    fun fetchStats(specialty: String, year: String) {
        if (getStatsJob?.isActive == true) getStatsJob?.cancel()
        getStatsJob = launch {
            when (val result = repository.getStatsPerSpecialtyAndYear(specialty, year)) {
                is Result.Success -> {
                    statsMotifsLd.value = result.data
                }
                //is Result2.Error -> _snackbarText.value = R.string.error_fetching
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }

    fun fetchOperations(specialty: String, year: String) {
        if (getRegulationJob?.isActive == true) getRegulationJob?.cancel()
        getRegulationJob = launch {
            repository.getAllOperationsCurrentYear(operationsLd, specialty, year)
        }
    }

    fun getRegulationsStatistiques(specialtyDocument: String, regulationsList: List<SpeOperation>): HashMap<String?, Long?> {
        val regulationsDecisions: HashMap<String?, Long?> = HashMap()

        val countDeclenchementSdis = regulationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_DECISION_DECLENCHEMENT_SDIS))
        }.size

        val countPriseMairie = regulationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_DECISION_PRISE_EN_CHARGE_MAIRIE))
        }.size

        val countPrisePolice = regulationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_DECISION_PRISE_EN_CHARGE_POLICE))
        }.size

        val countAutre = regulationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_DECISION_AUTRE))
        }.size

        val countCynoSdis = regulationsList.filter {
            it.decisionsCyno!!.contains(DecisionCyno(Constants.RA_DECISION_CYNO_DECLENCHEMENT_SDIS))
        }.size

        val countCynoPolice = regulationsList.filter {
            it.decisionsCyno!!.contains(DecisionCyno(Constants.RA_DECISION_CYNO_DECLENCHEMENT_POLICE))
        }.size

        if (specialtyDocument == Constants.FIRESTORE_RA_DOCUMENT) {
            regulationsDecisions[Constants.RA_DECISION_DECLENCHEMENT_SDIS] = countDeclenchementSdis.toLong()
            regulationsDecisions[Constants.RA_DECISION_PRISE_EN_CHARGE_MAIRIE] = countPriseMairie.toLong()
            regulationsDecisions[Constants.RA_DECISION_PRISE_EN_CHARGE_POLICE] = countPrisePolice.toLong()
            regulationsDecisions[Constants.RA_DECISION_AUTRE] = countAutre.toLong()
        }
        if (specialtyDocument == Constants.FIRESTORE_CYNO_DOCUMENT) {
            regulationsDecisions[Constants.RA_DECISION_CYNO_DECLENCHEMENT_SDIS] = countCynoSdis.toLong()
            regulationsDecisions[Constants.RA_DECISION_CYNO_DECLENCHEMENT_POLICE] = countCynoPolice.toLong()
        }
        return regulationsDecisions
    }

    fun getDecisionsStatistiques(operationsList: List<SpeOperation>): HashMap<String?, Long?> {
        val interventionsDecisions: HashMap<String?, Long?> = HashMap()

        val countCentreRegroupement = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ANIMAL_DESTINATION_CENTRE_REGROUPEMENT))
        }.size

        val countCedaf = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ANIMAL_DESTINATION_CEDAF))
        }.size

        val countProprietaire = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ANIMAL_DESTINATION_PROPRIETAIRE))
        }.size

        val countVeterinaire = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ANIMAL_DESTINATION_CLINIQUE_VETERINAIRE))
        }.size

        val countFuite = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ANIMAL_DESTINATION_FUITE))
        }.size

        interventionsDecisions[Constants.RA_ANIMAL_DESTINATION_CENTRE_REGROUPEMENT] = countCentreRegroupement.toLong()
        interventionsDecisions[Constants.RA_ANIMAL_DESTINATION_CEDAF] = countCedaf.toLong()
        interventionsDecisions[Constants.RA_ANIMAL_DESTINATION_PROPRIETAIRE] = countProprietaire.toLong()
        interventionsDecisions[Constants.RA_ANIMAL_DESTINATION_CLINIQUE_VETERINAIRE] = countVeterinaire.toLong()
        interventionsDecisions[Constants.RA_ANIMAL_DESTINATION_FUITE] = countFuite.toLong()
        return interventionsDecisions
    }

    fun getTransportsStatistiques(operationsList: List<SpeOperation>): HashMap<String?, Long?> {
        val interventionsTransports: HashMap<String?, Long?> = HashMap()

        val countVira = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ANIMAL_TRANSPORT_VIRA))
        }.size

        val countVtu = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ANIMAL_TRANSPORT_VTU))
        }.size

        interventionsTransports[Constants.RA_ANIMAL_TRANSPORT_VIRA] = countVira.toLong()
        interventionsTransports[Constants.RA_ANIMAL_TRANSPORT_VTU] = countVtu.toLong()
        return interventionsTransports
    }

    fun getActionsStatistiques(operationsList: List<SpeOperation>): HashMap<String?, Long?> {
        val interventionsActions: HashMap<String?, Long?> = HashMap()

        val countApproche = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ACTION_APPROCHE))
        }.size

        val countIdentification = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ACTION_IDENTIFICATION))
        }.size

        val countNeutralisation = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ACTION_NEUTRALISATION))
        }.size

        val countCapture = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ACTION_CAPTURE))
        }.size

        val countRelevage = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ACTION_RELEVAGE))
        }.size

        val countAssistance = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ACTION_ASSISTANCE))
        }.size

        val countConditionnement = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ACTION_CONDITIONNEMENT))
        }.size

        val countTransport = operationsList.filter {
            it.materialsRa!!.contains(MaterialRa(Constants.RA_ANIMAL_TRANSPORT_VTU))
        }.size

        interventionsActions[Constants.RA_ACTION_APPROCHE] = countApproche.toLong()
        interventionsActions[Constants.RA_ACTION_IDENTIFICATION] = countIdentification.toLong()
        interventionsActions[Constants.RA_ACTION_NEUTRALISATION] = countNeutralisation.toLong()
        interventionsActions[Constants.RA_ACTION_CAPTURE] = countCapture.toLong()
        interventionsActions[Constants.RA_ACTION_RELEVAGE] = countRelevage.toLong()
        interventionsActions[Constants.RA_ACTION_ASSISTANCE] = countAssistance.toLong()
        interventionsActions[Constants.RA_ACTION_CONDITIONNEMENT] = countConditionnement.toLong()
        interventionsActions[Constants.RA_ACTION_TRANSPORT] = countTransport.toLong()
        return interventionsActions
    }

    //fetch all the stock for SD Specialty
    fun fetchSdStock(year: String) {
        if (getStockJob?.isActive == true) getStockJob?.cancel()
        getStockJob = launch {
            repository.getSdStock(statsStocksLd, year)
        }
    }

    //fetch all statistiques for a given agent
    fun fetchAgentStats(agentId: String, specialty: String, year: String) {
        if (getAgentJob?.isActive == true) getAgentJob?.cancel()
        getAgentJob = launch {
            repository.getStatsPerAgentAndSpecialtyAndYear(agentId, specialty, year, statsAgentLd)
        }
    }

    //update SD Stock
    fun updateStock(materialName: String, quantity: String, year: String) {
        if (getStockJob?.isActive == true) getStockJob?.cancel()
        getStockJob = launch {
            repository.updateSdStock(materialName, quantity.toInt(), year)
        }
    }
}