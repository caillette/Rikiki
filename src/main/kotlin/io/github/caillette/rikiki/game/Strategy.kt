package io.github.caillette.rikiki.game

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.strategy.FirstAvailable

interface Strategy {

  fun bid( hand : List< Card > ) : Int {
    return 0
  }

  /**
   * @param chosable a subset of `hand` for which every card is playable. Iteration order is
   *     the same as distribution order, which means playing always the first card is pure
   *     random play.
   */
  fun decideForTurn( hand : List< Card >, chosable : Set< Card > ) : Card {
    return chosable.first()
  }

  companion object {
    val defaultFactory = FirstAvailable.factory
  }

  interface Factory {
    fun name() : String
    fun newStrategy( publicGame : PublicGame, playerIdentity : PlayerIdentity ) : Strategy
  }
}