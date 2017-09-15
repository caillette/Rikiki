package io.github.caillette.rikiki

import org.junit.jupiter.api.Test

class PrintToConsole {
  @Test
  internal fun fullGame() {
    val alice = PlayerIdentity( "Alice" )
    val bob = PlayerIdentity( "Bob" )
  val cards = shuffle( Packet() )
//    val cards = Packet().cards  // No shuffle helps debugging.
    val fullGame = FullGame( setOf( alice, bob ), cards, 2 )
    println()
    println( "All Cards: " + ansiString( fullGame._cards ) )
    println()
    fullGame.dumpToConsole()
  }
}