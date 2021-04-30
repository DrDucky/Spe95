package com.pomplarg.spe95.speoperations.data

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.data.await
import com.pomplarg.spe95.statistiques.data.StatistiqueRepository
import com.pomplarg.spe95.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

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
    fun addSpeOperationIntoRemoteDB(
        specialtyDocument: String,
        speOperation: SpeOperation,
        ldOperationAdded: MutableLiveData<Boolean>,
        ldPhotoRaAbsolutePath: MutableLiveData<Uri>
    ) {
        specialtiesCollection.document(specialtyDocument).collection("activities")
            .add(speOperation)
            .addOnSuccessListener {
                ldPhotoRaAbsolutePath.value?.let { absolutePath -> addPhoto(specialtyDocument, it.id, absolutePath) }
            }
        ldOperationAdded.value = true
    }

    /**
     * get a list operations for a given specialty
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


    companion object {
        const val TAG = "SpeOperationRepository"
        const val FIRESTORE_MAP_PHOTO_RA_KEY = "photoRa"
    }
}