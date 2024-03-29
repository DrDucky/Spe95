package com.pomplarg.spe95.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.pomplarg.spe95.R
import java.text.SimpleDateFormat
import java.util.*

fun dateTimestampToString(
    value: Timestamp?
): String {
    return if (value != null) {
        val formatter = SimpleDateFormat(Constants.ADD_OPERATION_DATE_FORMAT_DISPLAY + " - " + Constants.ADD_OPERATION_TIME_FORMAT_DISPLAY, Locale.FRANCE)
        formatter.format(value.toDate())
    } else {
        ""
    }
}

fun dateLongToString(
    value: Long?
): String {
    return if (value != null) {
        val formatter = SimpleDateFormat(Constants.ADD_OPERATION_DATE_FORMAT_DISPLAY, Locale.FRANCE)
        formatter.format(Date(value))
    } else {
        ""
    }
}

fun timeLongToString(
    value: Long?
): String {
    return if (value != null) {
        val formatter = SimpleDateFormat(Constants.ADD_OPERATION_TIME_FORMAT_DISPLAY, Locale.FRANCE)
        formatter.format(Date(value))
    } else {
        ""
    }
}

fun timeToString(
    value: Int?
): String {
    return if (value != null) {
        val hours = value / 60
        val minutes = value % 60

        String.format("%02d h %02d min", hours, minutes);
    } else {
        ""
    }
}

fun geopointToString(
    context: Context,
    value: GeoPoint?
): String {
    return if (value != null) {
        val geocoder = Geocoder(context)
        try {
            val adressList = geocoder.getFromLocation(value.latitude, value.longitude, 1)
            adressList?.firstOrNull()!!.getAddressLine(0) //Display correct address
        } catch (e: Exception) {
            ""
        }

    } else {
        ""
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