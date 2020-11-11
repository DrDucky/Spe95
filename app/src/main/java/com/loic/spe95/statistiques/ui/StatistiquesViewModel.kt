package com.loic.spe95.statistiques.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loic.spe95.data.Result
import com.loic.spe95.statistiques.data.Statistique
import com.loic.spe95.statistiques.data.StatistiqueRepository
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
    private var getStatsJob: Job? = null
    private var getAgentJob: Job? = null

    var statsMotifsLd: MutableLiveData<Statistique> = MutableLiveData()
    var statsAgentLd: MutableLiveData<Statistique> = MutableLiveData()


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

    //fetch all statistiques for a given agent
    fun fetchAgentStats(agentId: String, specialty: String, year: String) {
        if (getAgentJob?.isActive == true) getAgentJob?.cancel()
        getAgentJob = launch {
            when (val result = repository.getStatsPerAgentAndSpecialtyAndYear(agentId, specialty, year)) {
                is Result.Success -> {
                    statsAgentLd.value = result.data
                }
                //is Result2.Error -> _snackbarText.value = R.string.error_fetching
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }
}