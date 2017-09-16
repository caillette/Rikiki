package io.github.caillette.rikiki.strategy

import io.github.caillette.rikiki.game.PlayerIdentity
import io.github.caillette.rikiki.game.PublicGame
import io.github.caillette.rikiki.game.Strategy

abstract class AbstractStrategy(
    protected val game : PublicGame,
    protected val playerIdentity : PlayerIdentity
) : Strategy