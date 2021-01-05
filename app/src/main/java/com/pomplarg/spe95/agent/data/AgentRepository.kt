package com.pomplarg.spe95.agent.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.data.await

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
            is Result.Success -> {
                val speOperation =
                    documentSnapshot.data.toObjects(Agent::class.java)
                Result.Success(speOperation)
            }
            is Result.Error -> Result.Error(documentSnapshot.exception)
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
            agentsCollection.whereEqualTo("specialtiesMember.$specialty", true)
                .get().await()) {
            is Result.Success -> {
                val listAgents =
                    documentSnapshot.data.toObjects(Agent::class.java)
                Result.Success(listAgents)
            }
            is Result.Error -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }

    /**
     * Retrieve all agents filtered by ID
     * @param agentsId list of ids
     * @return list of agents
     */
    suspend fun getAgentsFromRemoteDB(
        agentsId: List<String> = listOf("")
    ): Result<List<Agent>> {
        var agentsIdVar = listOf("")
        if (agentsId.isNotEmpty()) agentsIdVar = agentsId
        return when (val documentSnapshot =
            agentsCollection
                .whereIn("id", agentsIdVar)
                .get().await()) {
            is Result.Success -> {
                val speOperation =
                    documentSnapshot.data.toObjects(Agent::class.java)
                Result.Success(speOperation)
            }
            is Result.Error -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }

    /**
     * Retrieve only one agent filtered by its id
     * @param agentId id string
     * @return agent
     */
    suspend fun getAgentFromRemoteDB(agentId: String): Result<Agent> {
        return when (val documentSnapshot =
            agentsCollection
                .whereEqualTo("id", agentId)
                .limit(1)
                .get().await()) {
            is Result.Success -> {
                val agent = documentSnapshot.data.documents[0].toObject(Agent::class.java)!!
                Result.Success(agent)
            }
            is Result.Error -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }


    /**
     * Add an agent into Firestore
     */
    suspend fun addAgentIntoRemoteDB(
        agent: Agent,
    ): Result<String> {
        return try {
            when (val resultDocumentSnapshot =
                agentsCollection
                    .add(agent)
                    .await()) {
                is Result.Success -> {
                    val agentId = resultDocumentSnapshot.data.id
                    Result.Success(agentId)
                }
                is Result.Error -> Result.Error(resultDocumentSnapshot.exception)
                is Result.Canceled -> Result.Canceled(resultDocumentSnapshot.exception)
            }
        } catch (exception: FirebaseFirestoreException) {
            Result.Error(exception)
        }
    }

    /**
     * Update agent with its own id (based on the guid document)
     * By default, id is "0"
     * It will be replace by something like "74wRU4xHrcV9oWAXEkKeRNp41c53"
     */
    suspend fun updateAgentIntoRemoteDB(
        agentId: String
    ) {
        try {
            agentsCollection
                .whereEqualTo("id", "0")
                .limit(1)
                .get()
                .addOnCompleteListener {
                    val data = hashMapOf("id" to agentId)
                    agentsCollection
                        .document(it.result!!.documents[0].id)
                        .set(data, SetOptions.merge())
                }
                .await()
        } catch (exception: FirebaseFirestoreException) {
            Log.e("TAG", "Firebase exception when updating agent ID : $exception.message")
        }
    }
}