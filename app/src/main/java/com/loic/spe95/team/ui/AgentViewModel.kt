package com.loic.spe95.team.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loic.spe95.data.Result
import com.loic.spe95.team.data.Agent
import com.loic.spe95.team.data.AgentRepository
import com.loic.spe95.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class AgentViewModel(private val repository: AgentRepository) : ViewModel(), CoroutineScope {

    // set coroutine context
    private val compositeJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + compositeJob

    // -- Coroutine jobs
    private var getUserJob: Job? = null

    private var agentsAllLd: MutableLiveData<List<Agent>> = MutableLiveData()
    var agentsLd: MutableLiveData<List<Agent>> = MutableLiveData()

    var teamSdSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    var teamCynoSelected: MutableLiveData<Boolean> = MutableLiveData(false)


    //fetch all agents in a specific specialty
    fun fetchAllAgents() {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result = repository.getAllAgents()) {
                is Result.Success -> {
                    agentsAllLd.value = result.data
                    agentsLd.value = result.data
                }
                //is Result2.Error -> _snackbarText.value = R.string.error_fetching
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }

    //fetch all agents in a specific specialty
    fun fetchAllAgentsPerSpecialty(specialty: String) {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result = repository.getAllAgentsPerSpecialty(specialty)) {
                is Result.Success -> agentsLd.value = result.data
                //is Result2.Error -> _snackbarText.value = R.string.error_fetching
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }

    // get all agents within agentsId list
    fun fetchAgentsInformationFrom(agentsId: List<Int>) {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result = repository.getAgentsFromRemoteDB(agentsId)) {
                is Result.Success -> agentsLd.value = result.data
                //is Result2.Error -> _snackbarText.value = R.string.error_fetching
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }


    /**
     * Filter the Agent section with thei specialties
     */
    fun filterTeamRv() {
        var currentAgentsList: List<Agent>? = agentsAllLd.value

        if (teamCynoSelected.value!!) {
            currentAgentsList =
                currentAgentsList?.filter {
                    it.specialtiesMember!!.containsKey(Constants.FIRESTORE_CYNO_DOCUMENT)
                }
        }

        if (teamSdSelected.value!!) {
            currentAgentsList =
                currentAgentsList?.filter {
                    it.specialtiesMember!!.containsKey(Constants.FIRESTORE_SD_DOCUMENT)
                }
        }

        //Default value : display all list if no selection
        if (!teamCynoSelected.value!! && !teamSdSelected.value!!) {
            currentAgentsList = agentsAllLd.value
        }

        agentsLd.value = currentAgentsList
    }


}