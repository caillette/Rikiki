package io.github.caillette.rikiki

import java.util.*
import kotlin.collections.LinkedHashSet

/**
 * Order is the same as for corresponding Unicode characters.
 */
enum class Figure( val asciiSymbol : Char ) {
  ACE( '1' ),
  TWO( '2' ),
  THREE( '3' ),
  FOUR( '4' ),
  FIVE( '5' ),
  SIX( '6' ),
  SEVEN( '7' ),
  EIGHT( '8' ),
  NINE( '9' ),
  TEN( 'T' ),
  JACK( 'J' ),
  QUEEN( 'Q' ),
  KING( 'K' ),
}


enum class Color {
  RED,
  BLACK,
}

/**
 * Order is the same as for corresponding Unicode characters.
 */
enum class Suite( val color : Color, val asciiSymbol : Char ) {
  SPADE( Color.BLACK, 'S' ),
  HEART( Color.RED, 'H' ),
  DIAMOND( Color.RED, 'D' ),
  CLUB( Color.BLACK, 'C' ),
}

/**
 * A [Card] has a [Figure] and a [Suite].
 * The [Packet] is part of the [Card]'s identity because card's back may differ depending
 * on the originating packet.
 * There is no support for something like wildcard, which would imply [Figure] and [Suite] nullity.
 */
class Card constructor( val packet : Packet, val figure : Figure, val suite : Suite ) {
  override fun toString() : String {
    return super.toString() + "{" + System.identityHashCode( packet) + ";" +
        figure.asciiSymbol + ";" + suite.asciiSymbol + "}"
  }
}


/**
 * A [Packet] contains [Card]s.
 */
class Packet {
  val cards : Set< Card >

  /**
   * @param selector so we can customize what [cards] we put in.
   */
  constructor( selector : ( figure : Figure, suite : Suite ) -> Boolean ) {
    val builder = mutableSetOf< Card >()
    for( suite in Suite.values() ) {
      for( figure in Figure.values() ) {
        if( selector.invoke( figure, suite) ) builder.add( Card( this, figure, suite ) )
      }
    }
    cards = builder.toSet()
  }

  constructor( ) : this( { _, _ -> true } )

}

fun shuffle( vararg packets : Packet ) : Set< Card > {
  val list = ArrayList< Card >()
  packets.forEach { packet -> list.addAll( packet.cards ) }
  Collections.shuffle( list )
  return Collections.unmodifiableSet( LinkedHashSet( list ) )
}

