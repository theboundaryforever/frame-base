package com.yuehai.data.collection.path.router

interface Unions {

    //工会
    interface Unions {

        companion object {
            const val PATH = "/Unions"

        }

    }

    interface UnionsSearch {

        companion object {
            const val PATH = "/UnionsSearch"

        }

    }

    interface UnionsCreate {

        companion object {
            const val PATH = "/UnionsCreate"
            const val EXTRA_GUILD_ID = "EXTRA_GUILD_ID"
            const val EXTRA_GUILD_INFO = "EXTRA_GUILD_INFO"

        }

    }

    interface UnionsMyAgency {

        companion object {
            const val PATH = "/UnionsMyAgency"
            const val EXTRA_GUILD_ID = "EXTRA_GUILD_ID"

        }

    }

    interface UnionsDetailAgency {

        companion object {
            const val PATH = "/UnionsDetailAgency"
            const val EXTRA_GUILD_TYPE_ID = "EXTRA_GUILD_TYPE_ID"
            const val EXTRA_AGENCY_TYPE = "EXTRA_AGENCY_TYPE"
            const val EXTRA_GUILD_ID = "EXTRA_GUILD_ID"
            const val EXTRA_HALF_MONTH_INFO = "EXTRA_HALF_MONTH_INFO"
            const val EXTRA_TIME_GUILD_INFO = "EXTRA_TIME_GUILD_INFO"

        }

    }

    interface UnionsIndividualDetailAgency {

        companion object {
            const val PATH = "/UnionsIndividualDetailAgency"
            const val EXTRA_GUILD_TYPE_ID = "EXTRA_GUILD_TYPE_ID"
            const val EXTRA_AGENCY_TYPE = "EXTRA_AGENCY_TYPE"
            const val EXTRA_GUILD_ID = "EXTRA_GUILD_ID"
            const val EXTRA_HALF_MONTH_INFO = "EXTRA_HALF_MONTH_INFO"
            const val EXTRA_TIME_GUILD_INFO = "EXTRA_TIME_GUILD_INFO"

        }

    }

    interface UnionsAubAgentDetailAgency {

        companion object {
            const val PATH = "/UnionsAubAgentDetailAgency"
            const val EXTRA_GUILD_TYPE_ID = "EXTRA_GUILD_TYPE_ID"
            const val EXTRA_AGENCY_TYPE = "EXTRA_AGENCY_TYPE"
            const val EXTRA_GUILD_ID = "EXTRA_GUILD_ID"
            const val EXTRA_HALF_MONTH_INFO = "EXTRA_HALF_MONTH_INFO"
            const val EXTRA_TIME_GUILD_INFO = "EXTRA_TIME_GUILD_INFO"
            const val EXTRA_AGENT_COUNT = "EXTRA_AGENT_COUNT"

        }

    }


    interface UnionsSubAgent {

        companion object {
            const val PATH = "/UnionsSubAgent"
            const val EXTRA_AGENT_GUILD_ID = "EXTRA_AGENT_GUILD_ID"
            const val EXTRA_AGENT_GUILD_INFO = "EXTRA_AGENT_GUILD_INFO"
        }

    }

    interface UnionsSubChildAgent {

        companion object {
            const val PATH = "/UnionsSubChildAgent"
            const val EXTRA_AGENT_GUILD_ID = "EXTRA_AGENT_GUILD_ID"
            const val EXTRA_AGENT_GUILD_COUNT = "EXTRA_AGENT_GUILD_COUNT"

        }

    }


    interface UnionsSubInviteAgent {

        companion object {
            const val PATH = "/UnionsSubInviteAgent"
            const val EXTRA_GUILD_ID = "EXTRA_GUILD_ID"
            const val EXTRA_GUILD_OTHER_AGENCY = "EXTRA_GUILD_OTHER_AGENCY"
        }

    }

    interface UnionApplyAgent {

        companion object {
            const val PATH = "/UnionApplyAgent"
            const val EXTRA_GUILD_ID = "EXTRA_GUILD_ID"

        }

    }
    interface UnionsRulesAgency {

        companion object {
            const val PATH = "/UnionsRulesAgency"
            const val EXTRA_GUILD_IS_AGENT = "EXTRA_GUILD_IS_AGENT"

        }

    }

}