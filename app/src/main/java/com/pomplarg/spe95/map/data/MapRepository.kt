package com.pomplarg.spe95.map.data

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.pomplarg.spe95.data.await
import com.pomplarg.spe95.speoperations.data.SpeOperation
import com.pomplarg.spe95.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class MapRepository {

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val specialtiesCollection = firestoreInstance.collection("specialties")

    /**
     * Retrieve all INTERVENTIONS in the current year
     */
    suspend fun getLocationsPerSpecialtyAndYear(
        specialty: String,
        locationsLd: MutableLiveData<List<SpeOperation>>,
        year: Int
    ) {
        val date = "01/01/$year"
        val endDateString = "31/12/$year"
        val formatter = SimpleDateFormat(Constants.ADD_OPERATION_DATE_FORMAT_DISPLAY, Locale.FRANCE)
        val startDate = formatter.parse(date)
        val endDate = formatter.parse(endDateString)

        specialtiesCollection.document(specialty)
            .collection("activities")
            .whereGreaterThan("startDate", startDate)
            .whereLessThan("startDate", endDate)
            .whereEqualTo("type", Constants.TYPE_OPERATION_INTERVENTION)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val speOperations = it.result.toObjects(SpeOperation::class.java)
                    locationsLd.value = speOperations
                }
            }
            .await()
    }
}