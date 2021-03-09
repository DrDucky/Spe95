package com.pomplarg.spe95.utils

import com.google.firebase.Timestamp
import java.util.*

fun Long.getTimestamp(): Timestamp? {
    val parsedDate: Date? = Date(this)
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
