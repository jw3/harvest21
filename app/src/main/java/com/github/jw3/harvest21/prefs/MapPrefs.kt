package com.github.jw3.harvest21.prefs

interface MapPrefs {
    val autoPan: Boolean
    val echoLocation: Boolean

    companion object {
        @JvmStatic
        fun newInstance(autoPan: Boolean, echoLocation: Boolean) = object: MapPrefs {
            override val autoPan: Boolean
                get() = autoPan
            override val echoLocation: Boolean
                get() = echoLocation
        }
    }
}
