package com.example.travelhub.utils

import android.text.format.DateUtils
import com.google.firebase.Timestamp
import java.util.Calendar

fun getRelativeTime(timestamp: Timestamp?): String {
    if (timestamp == null) return "À l'instant"

    val now = Calendar.getInstance().timeInMillis
    val time = timestamp.toDate().time

    // Cette fonction Android intégrée gère automatiquement "il y a 5 min", "il y a 1 heure", etc.
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        time,
        now,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()

    return relativeTime
}