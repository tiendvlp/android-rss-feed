package com.devlogs.rssfeed.common.helper

import java.util.*

fun Date.isSameDate (date: Date) : Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = this
    cal2.time = date
    return cal1[Calendar.DAY_OF_YEAR] === cal2[Calendar.DAY_OF_YEAR] &&
            cal1[Calendar.YEAR] === cal2[Calendar.YEAR]
}
