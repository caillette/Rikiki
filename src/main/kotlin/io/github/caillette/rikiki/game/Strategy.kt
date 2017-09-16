package io.github.caillette.rikiki.game

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.strategy.FirstAvailable

interface Strategy {

  fun bet( hand : List< Card > ) : Int

  /**
   * @param chosable guaranteed to be a subset of `hand`.
   */
  fun decideForTrick( hand : List< Card >, chosable : Set<Card> ) : Card

  companion object {
    val defaultFactory = FirstAvailable.factory
  }

  interface Factory {
    fun name() : String

    fun newStrategy( publicGame : PublicGame, playerIdentity : PlayerIdentity ) : Strategy
  }
}