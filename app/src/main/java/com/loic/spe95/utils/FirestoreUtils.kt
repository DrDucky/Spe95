package com.loic.spe95.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*


fun Int.getFirestoreCollection(): String {
    when (this) {
        Constants.FIRESTORE_CYNO_ID_DOCUMENT -> return Constants.FIRESTORE_CYNO_DOCUMENT
        Constants.FIRESTORE_SD_ID_DOCUMENT   -> return Constants.FIRESTORE_SD_DOCUMENT
    }
    return Constants.FIRESTORE_CYNO_DOCUMENT //Default one
}

fun String.getType(): Int {
    when (this) {
        Constants.TYPE_OPERATION_INTERVENTION_TITLE -> return Constants.TYPE_OPERATION_INTERVENTION
        Constants.TYPE_OPERATION_TRAINING_TITLE     -> return Constants.TYPE_OPERATION_TRAINING
    }
    return Constants.TYPE_OPERATION_INTERVENTION //Default one
}

fun String.getTimestamp(): Timestamp? {
    val formatter = SimpleDateFormat(Constants.ADD_OPERATION_DATE_FORMAT_DISPLAY, Locale.FRANCE)
    val parsedDate: Date? = formatter.parse(this)
    return parsedDate?.let { Timestamp(it) }
}