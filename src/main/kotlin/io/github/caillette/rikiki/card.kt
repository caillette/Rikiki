package io.github.caillette.rikiki

import com.google.common.collect.ImmutableSet
import java.util.*
import kotlin.Comparator

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
  KING( 'K' ) ;

  private fun strength() : Int {
    return if( this == ACE ) KING.ordinal + 1 else ordinal
  }

  object comparatorByStrength : Comparator< Figure > {
    override fun compare( o1 : Figure, o2 : Figure ) : Int {
      return o1.strength() - o2.strength()
    }
  }

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
 * There is no support for something like wildcard, which would imply [Figure] and [Suite] nullity.
 */
class Card constructor(
    val figure : Figure,
    val suite : Suite
) {
  override fun toString() : String {
    return javaClass.simpleName + "{" + figure.asciiSymbol + suite.asciiSymbol + "}"
  }

}


/**
 * A [Packet] is an immutable object containing [Card]s.
 */
class Packet {
  val cards : Set< Card >

  /**
   * @param selectors so we can customize what [cards] we put in.
   */
  constructor( vararg selectors : ( figure : Figure, suite : Suite ) -> Boolean ) {
    val builder = ImmutableSet.Builder< Card >()
    // Apply selectors first to make selection match their order.
    for( selector in selectors ) {
      for( suite in Suite.values() ) {
        for( figure in Figure.values() ) {
          if( selector.invoke( figure, suite ) ) builder.add( Card( figure, suite ) )
        }
      }
    }
    cards = builder.build()
  }

  constructor() : this( { _ , _ -> true } )


}

fun shuffle( vararg packets : Packet ) : Set< Card > {
  return shuffle( Random( 0 ), *packets )
}

fun shuffle( random : Random, vararg packets : Packet ) : Set< Card > {
  val list = ArrayList< Card >()
  packets.forEach { packet -> list.addAll( packet.cards ) }
  Collections.shuffle( list, random )
  return ImmutableSet.copyOf( list )
}

