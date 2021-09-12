package com.github.jw3.harvest21

interface BrokerPrefs {
    val url: String
    val user: String
    val pass: CharArray

    companion object {
        @JvmStatic
        fun newInstance(url: String, user: String, pass: CharArray) = object: BrokerPrefs {
            override val url: String
                get() = url
            override val user: String
                get() = user
            override val pass: CharArray
                get() = pass
        }
    }
}

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
