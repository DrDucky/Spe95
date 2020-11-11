package com.pomplarg.spe95.utils

import android.content.Context
import android.location.Geocoder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.pomplarg.spe95.R
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
        val hours = value / 60
        val minutes = value % 60

        return String.format("%02d h %02d min", hours, minutes);
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
        try {
            val adressList = geocoder.getFromLocation(value.latitude, value.longitude, 1)
            return adressList[0].getAddressLine(0) //Display correct address
        } catch (e: Exception) {
            return ""
        }

    } else {
        return ""
    }
}

fun getTypeToDrawable(value: String?): Int {
    when (value) {
        Constants.TYPE_OPERATION_INTERVENTION -> return R.drawable.ic_type_intervention
        Constants.TYPE_OPERATION_TRAINING -> return R.drawable.ic_type_training
        Constants.TYPE_OPERATION_FORMATION -> return R.drawable.ic_type_formation
        Constants.TYPE_OPERATION_INFORMATION -> return R.drawable.ic_type_information
    }
    return R.drawable.ic_type_intervention //Default one
}