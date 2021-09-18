package com.github.jw3.harvest21.prefs

interface DevicePrefs {
    val id: String

    companion object {
        @JvmStatic
        fun newInstance(id: String) = object: DevicePrefs {
            override val id: String
                get() = id
        }
    }
}
