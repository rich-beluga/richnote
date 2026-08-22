package com.rich_beluga.richnote.ui.navigation

import kotlinx.serialization.Serializable

sealed class NavRoutes {
    @Serializable
    data object Settings : NavRoutes()

    @Serializable
    data object About : NavRoutes()
}
