package com.loic.spe95.team.data

import com.google.firebase.firestore.FirebaseFirestore
import com.loic.spe95.data.Result
import com.loic.spe95.data.await

class AgentRepository {

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val agentsCollection = firestoreInstance.collection("agents")

    /**
     * Retrieve all agents
     * @return list of agents
     */
    suspend fun getAllAgents(
    ): Result<List<Agent>> {
        return when (val documentSnapshot =
            agentsCollection
                .get().await()) {
            is Result.Success  -> {
                val speOperation =
                    documentSnapshot.data.toObjects(Agent::class.java)
                Result.Success(speOperation)
            }
            is Result.Error    -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }

    /**
     * Retrieve all agents
     * @return list of agents
     */
    suspend fun getAllAgentsPerSpecialty(
        specialty: String
    ): Result<List<Agent>> {
        return when (val documentSnapshot =
            agentsCollection.whereEqualTo("specialties_member.$specialty", true)
                .get().await()) {
            is Result.Success  -> {
                val listAgents =
                    documentSnapshot.data.toObjects(Agent::class.java)
                Result.Success(listAgents)
            }
            is Result.Error    -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }

    /**
     * Retrieve all agents filtered by ID
     * @param agentsId list of ids
     * @return list of agents
     */
    suspend fun getAgentsFromRemoteDB(
        agentsId: List<Int> = listOf(1)
    ): Result<List<Agent>> {
        var agentsIdVar = listOf(1)
        if (!agentsId.isEmpty()) agentsIdVar = agentsId
        return when (val documentSnapshot =
            agentsCollection
                .whereIn("id", agentsIdVar)
                .get().await()) {
            is Result.Success  -> {
                val speOperation =
                    documentSnapshot.data.toObjects(Agent::class.java)
                Result.Success(speOperation)
            }
            is Result.Error    -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }

    /**
     * Retrieve only one agent filtered by its id
     * @param agentId id int
     * @return agent
     */
    suspend fun getAgentFromRemoteDB(agentId: Int): Result<Agent> {
        return when (val documentSnapshot =
            agentsCollection
                .whereEqualTo("id", agentId)
                .limit(1)
                .get().await()) {
            is Result.Success  -> {
                val agent = documentSnapshot.data.documents[0].toObject(Agent::class.java)!!
                Result.Success(agent)
            }
            is Result.Error    -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }
}