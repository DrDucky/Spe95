package com.pomplarg.spe95.map.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pomplarg.spe95.map.data.MapRepository
import com.pomplarg.spe95.speoperations.data.SpeOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class MapViewModel(private val repository: MapRepository) : ViewModel(), CoroutineScope {

    // set coroutine context
    private val compositeJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + compositeJob

    var locationsLd: MutableLiveData<List<SpeOperation>> = MutableLiveData()

    // -- Coroutine jobs
    private var getStatsJob: Job? = null

    //fetch all locations of interventions in a specific specialty
    fun fetchLocations(specialty: String, year: Int) {
        if (getStatsJob?.isActive == true) getStatsJob?.cancel()
        getStatsJob = launch {
            repository.getLocationsPerSpecialtyAndYear(specialty, locationsLd, year)
        }
    }
}