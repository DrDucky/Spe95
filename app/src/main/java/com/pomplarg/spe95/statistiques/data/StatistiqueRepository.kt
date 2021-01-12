package com.pomplarg.spe95.statistiques.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.data.SingleLiveEvent
import com.pomplarg.spe95.data.await
import com.pomplarg.spe95.speoperations.data.AgentOnOperation
import com.pomplarg.spe95.speoperations.data.MaterialCyno
import com.pomplarg.spe95.speoperations.data.MaterialSd
import com.pomplarg.spe95.utils.Constants

class StatistiqueRepository {

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val specialtiesCollection = firestoreInstance.collection("specialties")
    private val statistiqueCollection = firestoreInstance.collection("statistiques")

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
    fun getSdStock(statsStocksLd: MutableLiveData<List<MaterialSd>>, year: String) {
        statistiqueCollection.document(year)
            .addSnapshotListener { documentSnapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error when getting SD Stock", e)
                    return@addSnapshotListener
                }
                val materials = arrayListOf<MaterialSd>()
                val materialsFirestore = documentSnapshot?.data?.get("SdStock") as HashMap<String?, Long?>?
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
        materialName: String, quantity: Int, year: String
    ) {
        val nestedData = hashMapOf(
            materialName to quantity
        )

        val stock = hashMapOf(
            FIRESTORE_MAP_STOCK_KEY to nestedData
        )

        statistiqueCollection.document(year)
            .set(stock, SetOptions.merge())
            .addOnSuccessListener { Log.v(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error when setting stock", e)
            }
    }

    fun addOperationStats(specialtyDocument: String, year: String, typeOperation: String, motif: String, operationAdded: SingleLiveEvent<Any>) {
        var count = 0
        val docData: HashMap<Any, Any> = hashMapOf()

        if (typeOperation == Constants.TYPE_OPERATION_INTERVENTION) {
            statistiqueCollection.document(year)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val statMotifs: HashMap<String, Int>? = document.data?.get(specialtyDocument) as? HashMap<String, Int>
                        statMotifs?.let {
                            for (statMotif in statMotifs) {
                                if (statMotif.key == motif) {
                                    count = statMotif.value
                                }
                            }
                        }
                    }

                    val nestedData = hashMapOf(
                        motif to count + 1
                    )
                    docData[specialtyDocument] = nestedData

                    statistiqueCollection.document(year).set(
                        docData, SetOptions.merge()
                    )
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

    fun addMaterialStat(specialtyDocument: String, year: String, type: String, materialCyno: List<MaterialCyno>?, materialSd: List<MaterialSd>?, operationAdded: SingleLiveEvent<Any>) {

        when (specialtyDocument) {
            Constants.FIRESTORE_CYNO_DOCUMENT -> {

                statistiqueCollection.document(year)
                    .get()
                    .addOnSuccessListener { document ->
                        materialCyno?.let {
                            for (material in it) {

                                val docDataTypeAndTime: HashMap<Any, Any> = hashMapOf()
                                val docData: HashMap<Any, Any> = hashMapOf()

                                var countType = 0
                                var countTime = 0

                                if (document.exists()) {
                                    val statsCynoRemote: HashMap<String?, HashMap<String?, Long?>?>? = document.data?.get(material.name) as? HashMap<String?, HashMap<String?, Long?>?>?
                                    statsCynoRemote?.forEach { (key, value) ->
                                        if (key.equals("type")) {
                                            for (i in value!!.keys) {
                                                if (i.equals(type)) {
                                                    Log.e(TAG, "count already present : $i and $value[i]")
                                                    countType = value[i]!!.toInt()
                                                }
                                            }
                                        }
                                        if (key.equals("time")) {
                                            for (i in value!!.keys) {
                                                if (i.equals(type)) {
                                                    Log.e(TAG, "count already present : $i and $value[i]")
                                                    countTime = value[i]!!.toInt()
                                                }
                                            }
                                        }
                                    }
                                }

                                val nestedDataType = hashMapOf(
                                    type to countType + 1
                                )

                                val nestedDataTime = hashMapOf(
                                    type to countTime + material.time!!
                                )
                                docDataTypeAndTime["type"] = nestedDataType
                                docDataTypeAndTime["time"] = nestedDataTime
                                docData[material.name!!] = docDataTypeAndTime

                                statistiqueCollection.document(year).set(
                                    docData, SetOptions.merge()
                                )
                            }
                        }

                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                    }
            }
            Constants.FIRESTORE_SD_DOCUMENT -> {
                statistiqueCollection.document(year)
                    .get()
                    .addOnSuccessListener { document ->
                        materialSd?.let {
                            for (material in it) {
                                material.quantity?.let { quantity ->
                                    if (quantity > 0) {

                                        val docData: HashMap<Any, Any> = hashMapOf()

                                        var countMaterial = 0

                                        if (document.exists()) {
                                            val statsSdRemote: HashMap<String?, Long?>? = document.data?.get(FIRESTORE_MAP_STOCK_KEY) as? HashMap<String?, Long?>?
                                            statsSdRemote?.forEach { (key, value) ->
                                                if (key.equals(material.name)) {
                                                    countMaterial = value!!.toInt()
                                                }
                                            }
                                        }

                                        val nestedDataStock = hashMapOf(
                                            material.name to countMaterial - quantity
                                        )

                                        docData[FIRESTORE_MAP_STOCK_KEY] = nestedDataStock

                                        statistiqueCollection.document(year).set(
                                            docData, SetOptions.merge()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                    }
            }
        }
    }

    fun addAgentStats(specialtyDocument: String, year: String, typeOperation: String, motifOperation: String, agents: List<AgentOnOperation>?, operationAdded: SingleLiveEvent<Any>) {
        agents?.forEach {
            it.id?.let { agentId ->
                statistiqueCollection.document(year).collection(agentId).document(specialtyDocument)
                    .get()
                    .addOnSuccessListener { document ->
                        val docData: HashMap<Any, Any> = hashMapOf()

                        var countType = 0
                        var countTime = 0

                        if (document.exists()) {
                            val statType: HashMap<String, Int>? = document.data?.get("type") as? HashMap<String, Int>
                            statType?.let { typeMap ->
                                for (statTypeIt in typeMap) {
                                    if (statTypeIt.key == motifOperation) {
                                        countType = statTypeIt.value
                                    }
                                }
                            }
                            val statTime: HashMap<String, Int>? = document.data?.get("time") as? HashMap<String, Int>
                            statTime?.let { timeMap ->
                                for (statTimeIt in timeMap) {
                                    if (statTimeIt.key == motifOperation) {
                                        countTime = statTimeIt.value
                                    }
                                }
                            }
                        }

                        val nestedDataType = hashMapOf(
                            typeOperation to countType + 1
                        )

                        val nestedDataTime = hashMapOf(
                            typeOperation to countTime + it.time!!
                        )
                        docData["type"] = nestedDataType
                        docData["time"] = nestedDataTime

                        statistiqueCollection.document(year).collection(agentId).document(specialtyDocument).set(
                            docData, SetOptions.merge()
                        )
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                    }
            }
        }
    }

    companion object {
        const val TAG = "StatistiqueRepository"
        const val FIRESTORE_MAP_STOCK_KEY = "SdStock"
    }
}