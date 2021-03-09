package com.pomplarg.spe95.agent.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
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
            agentsCollection.orderBy("lastname", Query.Direction.ASCENDING)
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
    ): List<Agent> {
        var agentsIdVar = listOf("")
        var agents = arrayListOf<Agent>()
        if (agentsId.isNotEmpty()) agentsIdVar = agentsId
        val queries = arrayListOf<Task<QuerySnapshot>>() //Need to do multiple queries because whereIn limitation to 10
        agentsIdVar.forEach {
            queries.add(
                agentsCollection
                    .whereEqualTo("id", it)
                    .limit(1)
                    .get()
            )
        }
        val allTask: Task<List<QuerySnapshot>> = Tasks.whenAllSuccess(queries)
        allTask.addOnSuccessListener {
            for (querySnapshot in it) {
                for (documentSnapshot in querySnapshot) {
                    agents.add(
                        documentSnapshot.toObject(Agent::class.java)
                    )
                }
            }
        }.await()

        return agents.sortedWith(compareBy { it.lastname })
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
                if (documentSnapshot.data.isEmpty) {
                    Result.Error(Exception("No agent present"))
                } else {
                    val agent = documentSnapshot.data.documents[0].toObject(Agent::class.java)!!
                    Result.Success(agent)
                }
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
     * Update agent
     */
    suspend fun updateAgentIntoRemoteDB(
        agent: Agent
    ): Result<String> {
        return try {
            agentsCollection
                .document(agent.id)
                .set(agent)
                .await()
            Result.Success(agent.id)
        } catch (exception: FirebaseFirestoreException) {
            Log.e("TAG", "Firebase exception when updating agent ID : $exception.message")
            Result.Error(exception)
        }
    }

    /**
     * Update agent with its own id (based on the guid document)
     * By default, id is "0"
     * It will be replace by something like "74wRU4xHrcV9oWAXEkKeRNp41c53"
     */
    suspend fun updateAgentIdIntoRemoteDB(
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

    /**
     * Delete specified agent
     */
    suspend fun deleteAgentIntoRemoteDB(
        agentId: String
    ): Result<String> {
        return try {
            agentsCollection
                .document(agentId)
                .delete()
                .await()
            Result.Success(agentId)
        } catch (exception: FirebaseFirestoreException) {
            Log.e("TAG", "Firebase exception when deleting agent : $exception.message")
            Result.Error(exception)
        }
    }
}