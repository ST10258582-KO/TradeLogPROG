package com.tradelog.app.ui.navigation

// Sealed class defining all navigation routes in the TradeLog app.
// Navigation architecture follows the Jetpack Navigation Compose pattern.
// Reference: Google LLC, "Navigation with Compose" Android Developers (2024)
sealed class Screen(val route: String) {
    // Auth flow
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Main app — accessible via bottom navigation bar
    object Dashboard : Screen("dashboard")
    object Trades : Screen("trades")
    object Analytics : Screen("analytics")
    object Journal : Screen("journal")
    object Settings : Screen("settings")

    // Detail screens launched from the Trades list
    object TradeEntry : Screen("trade_entry?tradeId={tradeId}") {
        fun route(tradeId: String? = null) =
            if (tradeId != null) "trade_entry?tradeId=$tradeId" else "trade_entry"
    }
    object TradeDetail : Screen("trade_detail/{tradeId}") {
        fun route(tradeId: String) = "trade_detail/$tradeId"
    }
}
