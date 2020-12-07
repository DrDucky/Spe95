package com.pomplarg.spe95.statistiques.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.data.await
import com.pomplarg.spe95.speoperations.data.MaterialSd
import com.pomplarg.spe95.utils.Constants

class StatistiqueRepository {

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val specialtiesCollection = firestoreInstance.collection("specialties")
    private val statistiqueCollection = firestoreInstance.collection("statistiques")
    private val sdDocument = specialtiesCollection.document("sd")

    /**
     * Retrieve all stats
     * @return list of stats for a given specialty
     */
    suspend fun getStatsPerSpecialtyAndYear(
        specialty: String,
        year: String
    ): Result<Statistique> {
        return when (val documentSnapshot =
            statistiqueCollection.document(year)
                .get().await()) {
            is Result.Success -> {
                val stats = Statistique(null, null)
                stats.motifs = documentSnapshot.data[specialty] as HashMap<String?, Long?>?
                val ipsoTime = documentSnapshot.data[Constants.CYNO_DOG_IPSO] as HashMap<String?, HashMap<String?, Long?>?>?
                val nanoTime = documentSnapshot.data[Constants.CYNO_DOG_NANO] as HashMap<String?, HashMap<String?, Long?>?>?
                val neroneTime = documentSnapshot.data[Constants.CYNO_DOG_NERONE] as HashMap<String?, HashMap<String?, Long?>?>?
                val priaxeTime = documentSnapshot.data[Constants.CYNO_DOG_PRIAXE] as HashMap<String?, HashMap<String?, Long?>?>?

                stats.ipso = ipsoTime?.get("time")
                stats.nano = nanoTime?.get("time")
                stats.nerone = neroneTime?.get("time")
                stats.priaxe = priaxeTime?.get("time")

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

    /**
     * Retrieve all the stock for SD specialty
     * @return list of SD material
     */
    fun getSdStock(statsStocksLd: MutableLiveData<List<MaterialSd>>) {
        sdDocument
            .addSnapshotListener { documentSnapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error when getting SD Stock", e)
                    return@addSnapshotListener
                }
                val materials = arrayListOf<MaterialSd>()
                val materialsFirestore = documentSnapshot?.data?.get("stock") as HashMap<String?, Long?>?
                materialsFirestore?.forEach { (key, value) ->
                    value?.toInt()?.let { valueInt ->
                        materials.add(MaterialSd(key, valueInt))
                    }
                }
                statsStocksLd.value = materials
            }
    }


    /**
     * Update Material SD Stock
     * @return list of stats for a given agent
     */
    fun updateSdStock(
        materialName: String, quantity: Int
    ) {
        val nestedData = hashMapOf(
            materialName to quantity
        )

        val stock = hashMapOf(
            FIRESTORE_MAP_STOCK_KEY to nestedData
        )

        sdDocument
            .set(stock, SetOptions.merge())
            .addOnSuccessListener { Log.v(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error when setting stock", e)
            }
    }

    companion object {
        const val TAG = "StatistiqueRepository"
        const val FIRESTORE_MAP_STOCK_KEY = "stock"
    }
}