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
