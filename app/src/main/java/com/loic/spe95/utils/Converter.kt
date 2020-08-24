package com.loic.spe95.utils

import android.content.Context
import android.location.Geocoder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.*

fun dateToString(
    value: Timestamp?
): String {
    if (value != null) {
        val formatter = SimpleDateFormat(Constants.ADD_OPERATION_DATE_FORMAT_DISPLAY, Locale.FRANCE)
        return formatter.format(value.toDate())
    } else {
        return ""
    }
}

fun timeToString(
    value: Int?
): String {
    if (value != null) {
        val hours = value / 3600
        val minutes = (value % 3600) / 60

        return String.format("%02d:%02d", hours, minutes);
    } else {
        return ""
    }
}

fun geopointToString(
    context: Context,
    value: GeoPoint?
): String {
    if (value != null) {
        val geocoder = Geocoder(context)
        val adressList = geocoder.getFromLocation(value.latitude, value.longitude, 1)
        return adressList[0].getAddressLine(0) //Display correct address
    } else {
        return ""
    }
}

fun getTypeToString(value: Int): String {
    when (value) {
        Constants.TYPE_OPERATION_INTERVENTION -> return Constants.TYPE_OPERATION_INTERVENTION_TITLE
        Constants.TYPE_OPERATION_TRAINING     -> return Constants.TYPE_OPERATION_TRAINING_TITLE
    }
    return Constants.TYPE_OPERATION_INTERVENTION_TITLE //Default one
}
