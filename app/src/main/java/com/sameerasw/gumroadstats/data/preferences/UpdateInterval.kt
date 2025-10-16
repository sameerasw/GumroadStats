package com.sameerasw.gumroadstats.data.preferences

enum class UpdateInterval(val displayName: String, val minutes: Long?) {
    NEVER("Never", null),
    FIFTEEN_MINUTES("15 minutes", 15),
    THIRTY_MINUTES("30 minutes", 30),
    ONE_HOUR("1 hour", 60),
    SIX_HOURS("6 hours", 360);

    companion object {
        fun fromMinutes(minutes: Long?): UpdateInterval {
            return entries.find { it.minutes == minutes } ?: NEVER
        }
    }
}

