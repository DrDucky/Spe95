package com.pomplarg.spe95.agent.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pomplarg.spe95.agent.data.Agent
import com.pomplarg.spe95.agent.data.AgentRepository
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.utils.Constants
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
    private val _agentAdded = MutableLiveData<Boolean>(false)
    val agentAdded = _agentAdded

    var _agentIdAdded: MutableLiveData<String> = MutableLiveData()
    val agentIdAdded = _agentIdAdded

    var _genericException: MutableLiveData<String> = MutableLiveData()

    private var agentsAllLd: MutableLiveData<List<Agent>> = MutableLiveData()
    var agentsLd: MutableLiveData<List<Agent>> = MutableLiveData()

    private val newAgent = Agent()

    var teamSdSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    var teamCynoSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    var teamRaSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    val _firstname: MutableLiveData<String> = MutableLiveData()
    val firstname: LiveData<String> = _firstname
    var _firstnameError: MutableLiveData<String> = MutableLiveData()

    val _lastname: MutableLiveData<String> = MutableLiveData()
    val lastname: LiveData<String> = _lastname
    var _lastnameError: MutableLiveData<String> = MutableLiveData()

    val _agentCyno: MutableLiveData<Boolean> = MutableLiveData()
    val agentCyno: LiveData<Boolean> = _agentCyno

    val _agentSd: MutableLiveData<Boolean> = MutableLiveData()
    val agentSd: LiveData<Boolean> = _agentSd

    val _agentRa: MutableLiveData<Boolean> = MutableLiveData()
    val agentRa: LiveData<Boolean> = _agentRa

    val _agentEdited: MutableLiveData<Boolean> = MutableLiveData()
    val agentEdited: LiveData<Boolean> = _agentEdited

    val _agentDeleted: MutableLiveData<Boolean> = MutableLiveData()
    val agentDeleted: LiveData<Boolean> = _agentDeleted

    //fetch all agents in a specific specialty
    fun fetchAllAgents() {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result = repository.getAllAgents()) {
                is Result.Success -> {
                    agentsAllLd.value = result.data
                    agentsLd.value = result.data
                }
                else              -> {}//Nothing to do
            }
        }
    }

    //fetch all agents in a specific specialty
    fun fetchAllAgentsPerSpecialty(specialty: String) {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result = repository.getAllAgentsPerSpecialty(specialty)) {
                is Result.Success -> agentsLd.value = result.data
                else              -> {}//Nothing to do
            }
        }
    }

    // get all agents within agentsId list
    fun fetchAgentsInformationFrom(agentsId: List<String>) {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            agentsLd.value = repository.getAgentsFromRemoteDB(agentsId)
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

        if (teamRaSelected.value!!) {
            currentAgentsList =
                currentAgentsList?.filter {
                    it.specialtiesMember!!.containsKey(Constants.FIRESTORE_RA_DOCUMENT)
                }
        }

        //Default value : display all list if no selection
        if (!teamCynoSelected.value!! && !teamSdSelected.value!! && !teamRaSelected.value!!) {
            currentAgentsList = agentsAllLd.value
        }

        agentsLd.value = currentAgentsList
    }

    /**
     * Add an agent into Firestore
     */
    fun addAgentIntoFirestore(agent: Agent?) {
        //if agent is not null, we are in edit mode
        if (agent != null) {
            newAgent.id = agent.id
            newAgent.avatar = agent.avatar
        } else {
            newAgent.id = "0"
        }
        newAgent.firstname = firstname.value!!
        newAgent.lastname = lastname.value!!
        val mapSpecialties = mutableMapOf<String, Boolean>()
        if (_agentCyno.value == true) {
            mapSpecialties[Constants.FIRESTORE_CYNO_DOCUMENT] = true
        }
        if (_agentSd.value == true) {
            mapSpecialties[Constants.FIRESTORE_SD_DOCUMENT] = true
        }
        if (_agentRa.value == true) {
            mapSpecialties[Constants.FIRESTORE_RA_DOCUMENT] = true
        }
        newAgent.specialtiesMember = mapSpecialties

        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result =
                if (agent != null) {
                    repository.updateAgentIntoRemoteDB(newAgent)
                } else {
                    repository.addAgentIntoRemoteDB(newAgent)
                }) {
                is Result.Success -> {
                    if (agent != null) {
                        _agentEdited.value = true
                    } else {
                        _agentIdAdded.value = result.data
                        agentAdded.value = true
                    }
                }
                is Result.Error   -> _genericException.value = result.exception.message
                else              -> {}//Nothing to do
            }
        }
    }

    // update id with firestore uid
    fun updateAgentId(agentId: String) {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            repository.updateAgentIdIntoRemoteDB(agentId)
        }
    }

    // delete agent
    fun deleteAgent(agentId: String) {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result =
                repository.deleteAgentIntoRemoteDB(agentId)) {
                is Result.Success -> _agentDeleted.value = true
                is Result.Error   -> _genericException.value = result.exception.message
                else              -> {}//Nothing to do
            }
        }
    }
}