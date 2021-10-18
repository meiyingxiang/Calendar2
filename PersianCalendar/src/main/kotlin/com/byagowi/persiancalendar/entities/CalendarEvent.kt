package com.byagowi.persiancalendar.entities

import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import java.util.*

sealed class CalendarEvent<T : AbstractDate>(
    val title: String, val isHoliday: Boolean, val date: T
) {
    class GregorianCalendarEvent(title: String, isHoliday: Boolean, date: CivilDate) :
        CalendarEvent<CivilDate>(title, isHoliday, date)

    class IslamicCalendarEvent(title: String, isHoliday: Boolean, date: IslamicDate) :
        CalendarEvent<IslamicDate>(title, isHoliday, date)

    class PersianCalendarEvent(title: String, isHoliday: Boolean, date: PersianDate) :
        CalendarEvent<PersianDate>(title, isHoliday, date)

    class DeviceCalendarEvent(
        date: CivilDate, title: String, isHoliday: Boolean, val id: Int, val description: String,
        val start: Date, val end: Date, val color: String
    ) : CalendarEvent<CivilDate>(title, isHoliday, date)
}
