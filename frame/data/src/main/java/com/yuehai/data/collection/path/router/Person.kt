package com.yuehai.data.collection.path.router


interface Person {

    interface Person {

        companion object {
            const val PATH = "/Person"
            const val EXTRA_UID = "extra_uid"
        }

    }

    interface WalletCenter {
        companion object {
            const val PATH = "/WalletCenter"
            const val EXTRA_MONEY = "EXTRA_MONEY"
        }

    }

    interface WalletMainCenter {
        companion object {
            const val PATH = "/WalletMainCenter"
            const val EXTRA_MONEY = "EXTRA_MONEY"
            const val EXTRA_GUILD_ID = "EXTRA_GUILD_ID"
        }

    }

    interface WalletDetail {
        companion object {
            const val PATH = "/WalletDetail"
            const val EXTRA_AGENCY_DIAMOND="EXTRA_AGENCY_DIAMOND"
        }

    }

    interface UserReport {

        companion object {
            const val PATH = "/userReport"
        }

    }

    interface WalletAgentUser {

        companion object {
            const val PATH = "/WalletAgentUser"
        }

    }

}