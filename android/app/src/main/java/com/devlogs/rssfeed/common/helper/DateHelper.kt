package com.devlogs.rssfeed.common.helper

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.util.*

fun Date.isSameDate (date: Date) : Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = this
    cal2.time = date
    return cal1[Calendar.DAY_OF_YEAR] === cal2[Calendar.DAY_OF_YEAR] &&
            cal1[Calendar.YEAR] === cal2[Calendar.YEAR]
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.isSameDate (date: LocalDateTime) : Boolean {
    return dayOfYear === date.dayOfYear &&
            this.year === date.year
}
