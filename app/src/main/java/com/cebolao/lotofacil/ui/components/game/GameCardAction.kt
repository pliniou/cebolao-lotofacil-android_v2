package com.cebolao.lotofacil.ui.components.game

sealed class GameCardAction {
    data object Analyze : GameCardAction()
    data object Pin : GameCardAction()
    data object Delete : GameCardAction()
    data object Check : GameCardAction()
    data object Share : GameCardAction()
}
