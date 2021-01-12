package com.pomplarg.spe95.speoperations.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.data.await
import com.pomplarg.spe95.utils.Constants

class SpeOperationRepository {

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val specialtiesCollection = firestoreInstance.collection("specialties")

    /**
     * get a list operations for a given specialty
     */
    suspend fun getSpeOperationsFromRemoteDB(specialtyDocument: String): Result<List<SpeOperation>> {
        return when (val resultDocumentSnapshot =
            specialtiesCollection.document(specialtyDocument).collection("activities").get()
                .await()) {
            is Result.Success -> {
                val speOperations = resultDocumentSnapshot.data.toObjects(SpeOperation::class.java)
                speOperations.sortByDescending { it.startDate } //Sort all operations by Date (most recent in 1st)
                Result.Success(speOperations)
            }
            is Result.Error -> Result.Error(resultDocumentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(resultDocumentSnapshot.exception)
        }
    }

    suspend fun getSpeOperationFromRemoteDB(
        specialtyDocument: String,
        speOperationId: Int
    ): Result<SpeOperation> {
        return when (val documentSnapshot =
            specialtiesCollection.document(specialtyDocument).collection("activities")
                .whereEqualTo("id", speOperationId)
                .limit(1)
                .get().await()) {
            is Result.Success -> {
                var speOperation = SpeOperation(Constants.FIRESTORE_CYNO_DOCUMENT, 0, Constants.TYPE_OPERATION_INTERVENTION, "lala")
                if (documentSnapshot.data.documents.size > 0) {
                    speOperation =
                        documentSnapshot.data.documents[0].toObject(SpeOperation::class.java)!!
                }
                Result.Success(speOperation)
            }
            is Result.Error -> Result.Error(documentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(documentSnapshot.exception)
        }
    }

    /**
     * get a list operations for a given specialty
     */
    suspend fun addSpeOperationIntoRemoteDB(
        specialtyDocument: String,
        speOperation: SpeOperation,
    ): Result<String> {
        return try {
            when (val resultDocumentSnapshot =
                specialtiesCollection.document(specialtyDocument).collection("activities")
                    .add(speOperation)
                    .await()) {
                is Result.Success -> {
                    val speOperationId = resultDocumentSnapshot.data.id
                    Result.Success(speOperationId)
                }
                is Result.Error -> Result.Error(resultDocumentSnapshot.exception)
                is Result.Canceled -> Result.Canceled(resultDocumentSnapshot.exception)
            }
        } catch (exception: FirebaseFirestoreException) {
            Result.Error(exception)
        }
    }
}