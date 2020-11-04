package com.loic.spe95.utils

import android.content.Context
import android.location.Geocoder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.loic.spe95.R
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
        //TODO Check what we do with geocoder offlinen not possible
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

fun getTypeToString(value: Int): String {
    when (value) {
        Constants.TYPE_OPERATION_INTERVENTION -> return Constants.TYPE_OPERATION_INTERVENTION_TITLE
        Constants.TYPE_OPERATION_TRAINING -> return Constants.TYPE_OPERATION_TRAINING_TITLE
        Constants.TYPE_OPERATION_FORMATION -> return Constants.TYPE_OPERATION_FORMATION_TITLE
        Constants.TYPE_OPERATION_INFORMATION -> return Constants.TYPE_OPERATION_INFORMATION_TITLE
    }
    return Constants.TYPE_OPERATION_INTERVENTION_TITLE //Default one
}

fun getTypeToDrawable(value: Int): Int {
    when (value) {
        Constants.TYPE_OPERATION_INTERVENTION -> return R.drawable.ic_type_intervention
        Constants.TYPE_OPERATION_TRAINING -> return R.drawable.ic_type_training
        Constants.TYPE_OPERATION_FORMATION -> return R.drawable.ic_type_formation
        Constants.TYPE_OPERATION_INFORMATION -> return R.drawable.ic_type_information
    }
    return R.drawable.ic_type_intervention //Default one
}

fun getStringToType(value: String): Int {
    when (value) {
        Constants.TYPE_OPERATION_INTERVENTION_TITLE -> return Constants.TYPE_OPERATION_INTERVENTION
        Constants.TYPE_OPERATION_TRAINING_TITLE -> return Constants.TYPE_OPERATION_TRAINING
        Constants.TYPE_OPERATION_FORMATION_TITLE -> return Constants.TYPE_OPERATION_FORMATION
        Constants.TYPE_OPERATION_INFORMATION_TITLE -> return Constants.TYPE_OPERATION_INFORMATION
    }
    return Constants.TYPE_OPERATION_INTERVENTION //Default one
}
