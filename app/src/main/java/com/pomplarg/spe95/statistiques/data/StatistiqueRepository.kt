package com.pomplarg.spe95.statistiques.data

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.data.await
import com.pomplarg.spe95.speoperations.data.*
import com.pomplarg.spe95.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

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
            is Result.Success  -> {
                val stats = Statistique(null, null)
                stats.motifs = documentSnapshot.data[specialty] as HashMap<String?, Long?>?
                val ipsoTime = documentSnapshot.data[Constants.CYNO_DOG_IPSO] as HashMap<String?, HashMap<String?, Long?>?>?
                val nanoTime = documentSnapshot.data[Constants.CYNO_DOG_NANO] as HashMap<String?, HashMap<String?, Long?>?>?
                val neroneTime = documentSnapshot.data[Constants.CYNO_DOG_NERONE] as HashMap<String?, HashMap<String?, Long?>?>?
                val priaxeTime = documentSnapshot.data[Constants.CYNO_DOG_PRIAXE] as HashMap<String?, HashMap<String?, Long?>?>?
                val sniperTime = documentSnapshot.data[Constants.CYNO_DOG_SNIPER] as HashMap<String?, HashMap<String?, Long?>?>?

                stats.ipso = ipsoTime?.get("time")
                stats.nano = nanoTime?.get("time")
                stats.nerone = neroneTime?.get("time")
                stats.priaxe = priaxeTime?.get("time")
                stats.sniper = sniperTime?.get("time")

                Result.Success(stats)
            }
            is Result.Error    -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }

    fun getRegulationsList(statsRegulationsLd: MutableLiveData<List<SpeOperation>>, specialtyDocument: String, year: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.parse("$year-01-01")
        val endDate = dateFormat.parse("$year-12-31")
        val activitiesRegulationQuery = specialtiesCollection.document(specialtyDocument).collection("activities")
            .whereEqualTo("type", Constants.TYPE_OPERATION_REGULATION)
            .whereGreaterThanOrEqualTo("startDate", startDate!!)
            .whereLessThanOrEqualTo("startDate", endDate!!)
        activitiesRegulationQuery.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val regulationsList = task.result.toObjects(SpeOperation::class.java)
                statsRegulationsLd.value = regulationsList
                Log.d(TAG, "All regulations = " + regulationsList.size)
            } else {
                Log.d(TAG, "Count failed: ", task.exception)
            }
        }
    }

    /**
     * Retrieve all stats by agent and year
     * @return list of stats for a given agent
     */
    fun getStatsPerAgentAndSpecialtyAndYear(
        agentId: String,
        specialty: String,
        year: String,
        statsAgentLd: MutableLiveData<Statistique>
    ) {
        val mapMonthsTimes = HashMap<Int, HashMap<String?, Long?>?>()
        val mapMonthsTypes = HashMap<Int, HashMap<String?, Long?>?>()
        statistiqueCollection.document(year)
            .collection(agentId)
            .document(specialty)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val stats = Statistique()
                    stats.agentTypes = it.result.data?.get("type") as HashMap<String?, Long?>?
                    stats.agentTimes = it.result.data?.get("time") as HashMap<String?, Long?>?
                    for (i in 1..12) {
                        mapMonthsTimes[i] = it.result.data?.get("time-$i") as HashMap<String?, Long?>?
                    }
                    for (i in 1..12) {
                        mapMonthsTypes[i] = it.result.data?.get("type-$i") as HashMap<String?, Long?>?
                    }
                    stats.agentTimesByMonth = mapMonthsTimes
                    stats.agentTypesByMonth = mapMonthsTypes
                    statsAgentLd.value = stats
                }
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

    fun addOperationStats(
        specialtyDocument: String,
        year: String,
        currentMonth: Int,
        newSpeOperation: SpeOperation,
        ldPhotoRaAbsolutePath: MutableLiveData<Uri>,
        ldOperationAdded: MutableLiveData<Boolean>,
    ) {
        val materialCyno = newSpeOperation.materialsCyno
        val materialSd = newSpeOperation.materialsSd
        val agents = newSpeOperation.agentOnOperation
        val typeOperation = newSpeOperation.type
        val motif = newSpeOperation.motif
        val statDoc = statistiqueCollection.document(year)
        val activitiesDoc = specialtiesCollection.document(specialtyDocument).collection("activities").document()
        var docDataMotifs = HashMap<Any, Any>()
        var docDataMaterialCyno = HashMap<Any, Any>()
        var docDataMaterialSd = HashMap<Any, Any>()
        statistiqueCollection.document(year)
            .get()
            .addOnSuccessListener { document ->
                docDataMotifs = getMotifsData(document, specialtyDocument, motif, typeOperation)
                docDataMaterialCyno = getMaterialCynoData(document, typeOperation, materialCyno, specialtyDocument)
                docDataMaterialSd = getMaterialSdData(document, materialSd, specialtyDocument)

                agents?.forEach {
                    it.id?.let { agentId ->
                        statistiqueCollection.document(year).collection(agentId).document(specialtyDocument)
                            .get()
                            .addOnSuccessListener { document ->
                                val agentDoc = statistiqueCollection.document(year).collection(agentId).document(specialtyDocument)
                                val docDataAgent = getAgentData(document, typeOperation, currentMonth, it)
                                firestoreInstance.runBatch { batch ->
                                    batch.set(activitiesDoc, newSpeOperation)
                                    batch.set(statDoc, docDataMotifs, SetOptions.merge())
                                    batch.set(statDoc, docDataMaterialCyno, SetOptions.merge())
                                    batch.set(statDoc, docDataMaterialSd, SetOptions.merge())
                                    batch.set(agentDoc, docDataAgent, SetOptions.merge())
                                }.addOnCompleteListener { response ->
                                    if (response.isSuccessful) {
                                        ldPhotoRaAbsolutePath.value?.let { absolutePath -> addPhoto(specialtyDocument, activitiesDoc.id, absolutePath) }
                                        Log.d(TAG, "Batch success")
                                        ldOperationAdded.value = true
                                    } else {
                                        Log.w(TAG, "Transaction failure : " + response.exception)
                                        ldOperationAdded.value = false
                                    }
                                }
                            }
                    }
                }

            }
    }

    private fun getMotifsData(document: DocumentSnapshot, specialtyDocument: String, motif: String, typeOperation: String): HashMap<Any, Any> {
        val docData: HashMap<Any, Any> = hashMapOf()
        var count = 0

        if (typeOperation == Constants.TYPE_OPERATION_INTERVENTION) {
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
        }
        return docData
    }

    private fun getMaterialCynoData(document: DocumentSnapshot, typeOperation: String, materialCyno: List<MaterialCyno>?, specialtyDocument: String): HashMap<Any, Any> {
        val docData: HashMap<Any, Any> = hashMapOf()
        when (specialtyDocument) {
            Constants.FIRESTORE_CYNO_DOCUMENT -> {
                materialCyno?.let {
                    for (material in it) {

                        val docDataTypeAndTime: HashMap<Any, Any> = hashMapOf()

                        var countType = 0
                        var countTime = 0

                        if (document.exists()) {
                            val statsCynoRemote: HashMap<String?, HashMap<String?, Long?>?>? = document.data?.get(material.name) as? HashMap<String?, HashMap<String?, Long?>?>?
                            statsCynoRemote?.forEach { (key, value) ->
                                if (key.equals("type")) {
                                    for (i in value!!.keys) {
                                        if (i.equals(typeOperation)) {
                                            countType = value[i]!!.toInt()
                                        }
                                    }
                                }
                                if (key.equals("time")) {
                                    for (i in value!!.keys) {
                                        if (i.equals(typeOperation)) {
                                            countTime = value[i]!!.toInt()
                                        }
                                    }
                                }
                            }
                        }

                        val nestedDataType = hashMapOf(
                            typeOperation to countType + 1
                        )

                        val nestedDataTime = hashMapOf(
                            typeOperation to countTime + material.time!!
                        )
                        docDataTypeAndTime["type"] = nestedDataType
                        docDataTypeAndTime["time"] = nestedDataTime
                        docData[material.name!!] = docDataTypeAndTime
                    }
                }
            }
        }
        return docData
    }

    private fun getMaterialSdData(document: DocumentSnapshot, materialSd: List<MaterialSd>?, specialtyDocument: String): HashMap<Any, Any> {
        val docData: HashMap<Any, Any> = hashMapOf()
        when (specialtyDocument) {
            Constants.FIRESTORE_SD_DOCUMENT -> {
                materialSd?.let {
                    for (material in it) {
                        material.quantity?.let { quantity ->
                            if (quantity > 0) {

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
                            }
                        }
                    }
                }
            }
        }
        return docData
    }

    private fun getAgentData(document: DocumentSnapshot, typeOperation: String, currentMonth: Int, agentOnOperation: AgentOnOperation): HashMap<Any, Any> {
        val docData: HashMap<Any, Any> = hashMapOf()

        var countType = 0
        var countTime = 0
        var countTypeByMonth = 0
        var countTimeByMonth = 0

        if (document.exists()) {
            val statType: HashMap<String, Int>? = document.data?.get("type") as? HashMap<String, Int>
            statType?.let { typeMap ->
                for (statTypeIt in typeMap) {
                    if (statTypeIt.key == typeOperation) {
                        countType = statTypeIt.value
                    }
                }
            }
            val statTime: HashMap<String, Int>? = document.data?.get("time") as? HashMap<String, Int>
            statTime?.let { timeMap ->
                for (statTimeIt in timeMap) {
                    if (statTimeIt.key == typeOperation) {
                        countTime = statTimeIt.value
                    }
                }
            }
            val statTypeByMonth: HashMap<String, Int>? = document.data?.get("type-$currentMonth") as? HashMap<String, Int>
            statTypeByMonth?.let { typeMap ->
                for (statTypeIt in typeMap) {
                    if (statTypeIt.key == typeOperation) {
                        countTypeByMonth = statTypeIt.value
                    }
                }
            }
            val statTimeByMonth: HashMap<String, Int>? = document.data?.get("time-$currentMonth") as? HashMap<String, Int>
            statTimeByMonth?.let { timeMap ->
                for (statTimeIt in timeMap) {
                    if (statTimeIt.key == typeOperation) {
                        countTimeByMonth = statTimeIt.value
                    }
                }
            }
        }

        val nestedDataType = hashMapOf(
            typeOperation to countType + 1
        )

        val nestedDataTime = hashMapOf(
            typeOperation to countTime + agentOnOperation.time!!
        )

        val nestedDataTypeByMonth = hashMapOf(
            typeOperation to countTypeByMonth + 1
        )

        val nestedDataTimeByMonth = hashMapOf(
            typeOperation to countTimeByMonth + agentOnOperation.time!!
        )

        docData["type"] = nestedDataType
        docData["time"] = nestedDataTime

        docData["type-$currentMonth"] = nestedDataTypeByMonth
        docData["time-$currentMonth"] = nestedDataTimeByMonth

        return docData
    }

    companion object {
        const val TAG = "StatistiqueRepository"
        const val FIRESTORE_MAP_STOCK_KEY = "SdStock"
        const val FIRESTORE_MAP_PHOTO_RA_KEY = "photoRa"
    }

    /**
     * add a photo in firebase storage
     */
    fun addPhoto(
        specialtyDocument: String,
        speOperationDocId: String,
        path: Uri
    ) {

        val storageRef = FirebaseStorage.getInstance().reference
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
        var pathOnServer = "JPEG_${timeStamp}_.jpg"
        val photosRef = storageRef.child("images/${pathOnServer}")
        var uploadTask = photosRef.putFile(path)
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...

            val data = hashMapOf(FIRESTORE_MAP_PHOTO_RA_KEY to photosRef.path)

            //We set the photo Path into speOperation object into firestore
            specialtiesCollection.document(specialtyDocument).collection("activities")
                .document(speOperationDocId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener { Log.v(SpeOperationRepository.TAG, "Photo path successfully written!") }
                .addOnFailureListener { e ->
                    Log.e(StatistiqueRepository.TAG, "Error when setting photo path stock", e)
                }
        }
    }
}