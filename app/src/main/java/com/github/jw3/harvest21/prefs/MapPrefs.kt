package com.github.jw3.harvest21.prefs

interface MapPrefs {
    val autoPan: Boolean

    companion object {
        @JvmStatic
        fun newInstance(autoPan: Boolean) = object: MapPrefs {
            override val autoPan: Boolean
                get() = autoPan
        }
    }
}