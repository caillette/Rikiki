package io.github.caillette.rikiki.strategy

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.card.Figure
import io.github.caillette.rikiki.game.PlayerIdentity
import io.github.caillette.rikiki.game.PublicGame
import io.github.caillette.rikiki.game.Strategy

class ProbabilisticLight(
    game : PublicGame,
    playerIdentity : PlayerIdentity
) : AbstractStrategy( game, playerIdentity ) {

  companion object {

    private fun averageCardStrength( cards : Iterable< Card > ) : Int {
      return averageFigureStrength( cards.map( { it.figure } ) )
    }

    private fun averageFigureStrength( figures : Iterable< Figure >) : Int {
      return figures.map( { it.strength() } ).average().toInt()
    }

    val figureStrengthThreshold = averageFigureStrength( Figure.values().asList() )
  }

  override fun bid( hand : List< Card > ) : Int {
    val trump = game.trump
    var bid = 0

    if( averageCardStrength( hand ) > ProbabilisticLight.figureStrengthThreshold ) bid ++

    if( trump != null ) {
      val trumpCompatible = hand.filter { it.suite == trump }
      bid += trumpCompatible.filter { it.figure.strength() > figureStrengthThreshold }.count()
    }

    return bid
  }




//  fun strongestNonWinning( inHand : Set< Card >, played : Set< Card >, trump : Suite ) : Card? {
//
//  }

  override fun decideForTurn(
      hand : List< Card >,
      chosable : Set< Card >
  ) : Card {

    if( chosable.size > 1 ) {
      if( myWinsInThisTurn() < myBids() ) {
        val trumpCompliantCards = chosable.filter { it.suite == game.trump }
        if( trumpCompliantCards.isNotEmpty() ) {
          return trumpCompliantCards.first()
        }
        if( turnCompletion() > 0.5 ) {
          val strongerCards = strongerCards( chosable, cardsPlayedInThisTurn(), game.trump )
          if( strongerCards.isNotEmpty() ) return strongerCards.first()
        }
      }
    }
    return chosable.first()
  }



  object factory : Strategy.Factory {

    override fun name() : String {
      return ProbabilisticLight::class.java.simpleName
    }

    override fun newStrategy(
        publicGame : PublicGame,
        playerIdentity : PlayerIdentity
    ) : Strategy {
      return ProbabilisticLight( publicGame, playerIdentity )
    }
  }

}