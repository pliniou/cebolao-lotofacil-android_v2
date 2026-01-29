package br.com.loterias.cebolaolotofacil.presentation.navigation

/**
 * Navigation routes for the application
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Generator : Screen("generator")
    object Games : Screen("games")
    object Checker : Screen("checker")
    object About : Screen("about")
    object ResultDetails : Screen("result_details/{concurso}") {
        fun createRoute(concurso: Int) = "result_details/$concurso"
    }
}

/**
 * Bottom navigation items
 */
enum class BottomNavItem(val route: String, val label: String, val icon: String) {
    HOME("home", "Home", "home"),
    GENERATOR("generator", "Gerador", "tune"),
    GAMES("games", "Jogos", "sports_casino"),
    CHECKER("checker", "Conferência", "done_all"),
    ABOUT("about", "Sobre", "info")
}
