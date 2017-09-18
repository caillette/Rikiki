package io.github.caillette.rikiki.strategy

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.game.PlayerIdentity
import io.github.caillette.rikiki.game.PublicGame
import io.github.caillette.rikiki.game.Strategy

class AimZero(
    game : PublicGame,
    playerIdentity : PlayerIdentity
) : AbstractStrategy( game, playerIdentity ) {

  override fun decideForTurn( hand : List< Card >, chosable : Set< Card > ) : Card {
    val firstCard = game.firstCard
    val trump = game.trump
    val cardsPlayedInThisTurn = game.cardsPlayedInThisTurn()
    var chosableCardsWeakestFirst = chosable.sortedBy { it.figure.strength }

    // Play strongest trump-suited card weaker than strongest trump-suited card played.
    val strongestTrumpSuitedCardPlayed = cardsPlayedInThisTurn
        .filter { it.suite == trump }
        .sortedByDescending{ it.figure.strength }
        .firstOrNull()
    if( strongestTrumpSuitedCardPlayed != null ) {
      val weakerCard = chosable
          .filter( strongestTrumpSuitedCardPlayed.weakerInSameSuite() )
          .sortedByDescending { it.figure.strength }
          .firstOrNull()
      if( weakerCard != null ) return weakerCard
    }

    if( firstCard != null ) {

      // Play strongest non trump-suited card out of first card's suite.
      val strongestCardOutOfFirstCardSuite = chosable
          .filter { it.suite != firstCard.suite && it.suite != trump }
          .sortedByDescending { it.figure.strength }
          .firstOrNull()
      if( strongestCardOutOfFirstCardSuite != null ) return strongestCardOutOfFirstCardSuite

      // Play strongest card weaker that strongest played card in first card's suite.
      val strongestCardInFirstCardSuite = cardsPlayedInThisTurn
          .filter { it.suite == firstCard.suite }
          .sortedByDescending { it.figure.strength }
          .firstOrNull()
      if( strongestCardInFirstCardSuite != null ) {
        val weakerCard = chosable.firstOrNull( strongestCardInFirstCardSuite.weakerInSameSuite() )
        if( weakerCard != null ) return weakerCard
      }
    }

    // Play weakest non-trump card when playing first.
    val weakestNonTrump = chosableCardsWeakestFirst
        .filter { it.suite != trump }
        .firstOrNull()
    if( weakestNonTrump != null ) return weakestNonTrump

    // Play weakest card in last resort.
    return chosableCardsWeakestFirst.first()
  }



  object factory : Strategy.Factory {

    override fun name() : String {
      return AimZero::class.java.simpleName
    }

    override fun newStrategy(
        publicGame : PublicGame,
        playerIdentity : PlayerIdentity
    ) : Strategy {
      return AimZero( publicGame, playerIdentity )
    }
  }

}