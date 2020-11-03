package com.loic.spe95.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

fun String.getType(): Int {
    when (this) {
        Constants.TYPE_OPERATION_INTERVENTION_TITLE -> return Constants.TYPE_OPERATION_INTERVENTION
        Constants.TYPE_OPERATION_TRAINING_TITLE -> return Constants.TYPE_OPERATION_TRAINING
        Constants.TYPE_OPERATION_FORMATION_TITLE -> return Constants.TYPE_OPERATION_FORMATION
        Constants.TYPE_OPERATION_INFORMATION_TITLE -> return Constants.TYPE_OPERATION_INFORMATION
    }
    return Constants.TYPE_OPERATION_INTERVENTION //Default one
}

fun String.getTimestamp(): Timestamp? {
    val formatter = SimpleDateFormat(Constants.ADD_OPERATION_DATE_FORMAT_DISPLAY, Locale.FRANCE)
    val parsedDate: Date? = formatter.parse(this)
    return parsedDate?.let { Timestamp(it) }
}


/**
 * Time format hh:mm to minutes
 */
fun String.toTime(): Int {
    val values = this.split(":")
    val hours = values[0].toInt() * 60
    val minutes = values[1].toInt()
    return hours + minutes
}
