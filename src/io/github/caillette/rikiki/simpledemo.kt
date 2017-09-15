package io.github.caillette.rikiki

fun main( args : Array< String > ) {
  println( "Hi Kotlin" )
  println( Packet() )
//  for( card in Packet().cards ) println( card )

  println( ansiString( Packet().cards ) )

  val alice = PlayerIdentity( "Alice" )
  val bob = PlayerIdentity( "Bob" )
//  val cards = shuffle( Packet() )
  val cards = Packet().cards  // No shuffle helps debugging.

  val fullGame = FullGame( setOf( alice, bob ), cards, 2 )

  fullGame.dumpToConsole()
  fullGame.askPlayersToBet()
}