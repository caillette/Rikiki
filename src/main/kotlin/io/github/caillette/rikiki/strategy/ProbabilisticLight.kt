package io.github.caillette.rikiki.strategy

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.game.PlayerIdentity
import io.github.caillette.rikiki.game.PublicGame
import io.github.caillette.rikiki.game.Strategy

class ProbabilisticLight(
    game : PublicGame,
    playerIdentity : PlayerIdentity
) : AbstractStrategy(game, playerIdentity) {

  override fun bet( hand : List< Card > ) : Int {
    val trump = game.trump
    return if( trump == null ) {
      0
    } else {
      hand.filter { it.suite == trump }.count() / 2
    }
  }

  override fun decideForTrick(
      hand : List< Card >,
      chosable : Set< Card >
  ) : Card {
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