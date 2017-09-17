package io.github.caillette.rikiki.strategy

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.card.Suite
import io.github.caillette.rikiki.game.PlayerIdentity
import io.github.caillette.rikiki.game.PublicGame
import io.github.caillette.rikiki.game.Strategy

abstract class AbstractStrategy(
    protected val game : PublicGame,
    protected val playerIdentity : PlayerIdentity
) : Strategy {

  protected fun myWinsInThisTurn() : Int = game.turnWins[ playerIdentity ]!!

  protected fun myBids() : Int = game.bids[ playerIdentity ]!!

  protected fun cardsPlayedInThisTurn() = game.decisionsForThisTurn.map { it.card }

  /**
   * From 0 (at the start of the turn) to 1 at the end.
   */
  protected fun turnCompletion() : Float =
      game.decisionsForThisTurn.size.toFloat() / game.playerIdentities.size.toFloat()

  /**
   * @param cards must be non-empty.
   */
  protected fun highestStrength( cards : List< Card > ) =
      cards.map { it.figure.strength() }.max() !!

  protected fun strongerCards(
      from : Set< Card >,
      suiteOfInterest : Suite?,
      strengthThreshold : Int
  ) = from.filter {
    ( suiteOfInterest == null || it.suite == suiteOfInterest ) &&
        it.figure.strength() > strengthThreshold
  }.toSet()

  protected fun strongerCards(
      chosable : Set< Card >,
      others : List< Card >,
      trump : Suite?
  ) : Set< Card > {
    val playedWithTrump = others.filter { it.suite == trump }
    if( playedWithTrump.isEmpty() ) {
      val first = others.firstOrNull()
      return if( first == null ) {
        chosable
      } else {
        strongerCards( chosable, first.suite, highestStrength( others ) )
      }
    } else {
      return strongerCards( chosable, trump, highestStrength( playedWithTrump ) )
    }
  }

}