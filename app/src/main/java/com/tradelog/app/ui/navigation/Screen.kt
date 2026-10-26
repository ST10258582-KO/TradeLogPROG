package com.tradelog.app.ui.navigation

// Sealed class defining all navigation destinations in the app.
// Each Screen has a route string used by the Jetpack Navigation NavHost.
// Reference: Google LLC, "Jetpack Navigation Compose" Android Developers (2024)
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Trades : Screen("trades")
    object Analytics : Screen("analytics")
    object Journal : Screen("journal")
    object Settings : Screen("settings")
    object RiskCalculator : Screen("risk_calculator")

    // Trade entry with optional tradeId for editing an existing trade
    object TradeEntry : Screen("trade_entry/{tradeId}") {
        fun route(tradeId: String? = null) = "trade_entry/${tradeId ?: "new"}"
    }

    // Trade detail with required tradeId
    object TradeDetail : Screen("trade_detail/{tradeId}") {
        fun route(id: String) = "trade_detail/$id"
    }
}
