package io.github.caillette.rikiki.strategy

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.game.PlayerIdentity
import io.github.caillette.rikiki.game.PublicGame
import io.github.caillette.rikiki.game.Strategy

/**
 * The simplest strategy to implement.
 */
class FirstAvailable : Strategy {

  override fun bet( hand : List< Card > ) : Int {
    return 0
  }

  override fun decideForTrick(
      hand : List< Card >,
      chosable : Set< Card >
  ) : Card {
    return chosable.first()
  }

  object factory : Strategy.Factory {

    override fun name() : String {
      return FirstAvailable::class.java.simpleName
    }

    override fun newStrategy( publicGame : PublicGame, playerIdentity : PlayerIdentity) : Strategy {
      return FirstAvailable()
    }
  }
}