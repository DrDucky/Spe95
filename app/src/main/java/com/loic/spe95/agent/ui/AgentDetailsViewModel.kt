package com.loic.spe95.agent.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loic.spe95.agent.data.Agent
import com.loic.spe95.agent.data.AgentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class AgentDetailsViewModel(private val repository: AgentRepository) : ViewModel(), CoroutineScope {

    // set coroutine context
    private val compositeJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + compositeJob

    // -- Coroutine jobs
    private var getUserJob: Job? = null
    var agentLd: MutableLiveData<Agent> = MutableLiveData()

    // get a user infromation with its id
    fun fetchAgentInformation(agentId: String) {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result = repository.getAgentFromRemoteDB(agentId)) {
                is com.loic.spe95.data.Result.Success -> agentLd.value = result.data
                //is Result2.Error -> _snackbarText.value = R.string.error_fetching
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }


}