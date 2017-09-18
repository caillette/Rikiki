package io.github.caillette.rikiki.strategy

import io.github.caillette.rikiki.game.PlayerIdentity
import io.github.caillette.rikiki.game.PublicGame
import io.github.caillette.rikiki.game.Strategy

/**
 * The simplest strategy to implement.
 */
class FirstAvailable : Strategy {

  object factory : Strategy.Factory {

    override fun name() : String {
      return FirstAvailable::class.java.simpleName
    }

    override fun newStrategy( publicGame : PublicGame, playerIdentity : PlayerIdentity) : Strategy {
      return FirstAvailable()
    }
  }
}