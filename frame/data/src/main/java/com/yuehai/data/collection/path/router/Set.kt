package com.yuehai.data.collection.path.router

interface Set {
    interface Language {

        companion object {
            const val PATH = "/language"

        }

    }
    interface BindPhone {

        companion object {
            const val PATH = "/bind/phone"
            const val  EXTRA_BIND_STATE="extra_bind_state"

        }

    }

    interface RestPassword {

        companion object {
            const val PATH = "/restpassword"
            const val  EXTRA_BIND_STATE="extra_bind_state"

        }

    }

    interface CountryList {

        companion object {
            const val PATH = "/countrylist"

        }

    }
}