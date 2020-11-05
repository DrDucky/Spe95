package com.loic.spe95.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

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
