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
    var operationsRegulationsLd: MutableLiveData<List<SpeOperation>> = MutableLiveData()
    var operationsRegulationsStatsLd: MutableLiveData<Statistique> = MutableLiveData()


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

    fun fetchRegulationsCount(specialty: String, year: String) {
        if (getRegulationJob?.isActive == true) getRegulationJob?.cancel()
        getRegulationJob = launch {
            repository.getRegulationsList(operationsRegulationsLd, specialty, year)
        }
    }

    fun getRegulationsStatistiques(specialtyDocument: String, regulationsList: List<SpeOperation>) {
        val stat = Statistique()

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
        stat.regulationsDecisions = regulationsDecisions
        operationsRegulationsStatsLd.value = stat
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