package com.loic.spe95.statistiques.data

import com.google.firebase.firestore.FirebaseFirestore
import com.loic.spe95.data.Result
import com.loic.spe95.data.await

class StatistiqueRepository {

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val statistiqueCollection = firestoreInstance.collection("statistiques")

    /**
     * Retrieve all stats
     * @return list of stats for a given specialty
     */
    suspend fun getMotifsStatsPerSpecialtyAndYear(
        specialty: String,
        year: String
    ): Result<Statistique> {
        return when (val documentSnapshot =
            statistiqueCollection.document(year)
                .get().await()) {
            is Result.Success -> {
                val stats = Statistique(null, null)
                stats.motifs = documentSnapshot.data[specialty] as HashMap<String?, Long?>?
                Result.Success(stats)
            }
            is Result.Error -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }

    /**
     * Retrieve all stats by agent and year
     * @return list of stats for a given agent
     */
    suspend fun getStatsPerAgentAndSpecialtyAndYear(
        agentId: String,
        specialty: String,
        year: String
    ): Result<Statistique> {
        return when (val documentSnapshot =
            statistiqueCollection.document(year)
                .collection(agentId)
                .document(specialty)
                .get().await()) {
            is Result.Success -> {
                val stats = Statistique()
                stats.agentTypes = documentSnapshot.data["type"] as HashMap<String?, Long?>?
                stats.agentTimes = documentSnapshot.data["time"] as HashMap<String?, Long?>?
                Result.Success(stats)
            }
            is Result.Error -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }
}