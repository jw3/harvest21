package com.github.jw3.harvest21.prefs

interface BrokerPrefs {
    val url: String
    val user: String
    val pass: CharArray

    companion object {
        @JvmStatic
        fun newInstance(url: String, user: String, pass: CharArray) = object : BrokerPrefs {
            override val url: String
                get() = url
            override val user: String
                get() = user
            override val pass: CharArray
                get() = pass
        }
    }
}
