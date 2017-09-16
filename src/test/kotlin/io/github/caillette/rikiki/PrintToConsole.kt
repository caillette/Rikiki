package io.github.caillette.rikiki

import io.github.caillette.rikiki.card.Packet
import io.github.caillette.rikiki.card.shuffle
import io.github.caillette.rikiki.game.FullGame
import io.github.caillette.rikiki.game.PlayerIdentity
import io.github.caillette.rikiki.game.ansiString
import org.junit.jupiter.api.Test

class PrintToConsole {
  @Test
  internal fun fullGame() {
    val alice = PlayerIdentity("Alice")
    val bob = PlayerIdentity("Bob")
  val cards = shuffle(Packet())
//    val cards = Packet().cards  // No shuffle helps debugging.
    val fullGame = FullGame(setOf(alice, bob), cards, 2)
    println()
    println( "All Cards: " + ansiString(fullGame._cards))
    println()
    fullGame.dumpToConsole()
  }
}