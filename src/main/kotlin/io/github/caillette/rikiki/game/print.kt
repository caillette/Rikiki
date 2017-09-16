package io.github.caillette.rikiki.game

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.card.Color
import io.github.caillette.rikiki.card.Figure
import io.github.caillette.rikiki.card.Suite
import io.github.caillette.rikiki.toolkit.eol

/**
 * There are nice compact
 * [Unicode characters](https://en.wikipedia.org/wiki/Playing_cards_in_Unicode)
 * for cards.
 */
fun unicodeCharacter( card : Card ) : Char {
  val figureOffset = when( card.figure ) {
    Figure.ACE -> 0x1
    Figure.TWO -> 0x2
    Figure.THREE -> 0x3
    Figure.FOUR -> 0x4
    Figure.FIVE -> 0x5
    Figure.SIX -> 0x6
    Figure.SEVEN -> 0x7
    Figure.EIGHT -> 0x8
    Figure.NINE -> 0x9
    Figure.TEN -> 0xa
    Figure.JACK -> 0xb
    // 0x1f0ac is for Knight.
    Figure.QUEEN -> 0xd
    Figure.KING -> 0xe
  }
  val suiteOffset = when( card.suite ) {
    Suite.SPADE -> 0
    Suite.HEART -> 0x10
    Suite.DIAMOND -> 0x20
    Suite.CLUB -> 0x30
  }
  return ( 0xdca0 + figureOffset + suiteOffset ).toChar()
}

fun unicodeCharacter( suite : Suite ) : Char {
  return when( suite ) {
    Suite.SPADE -> '\u2660'
    Suite.HEART -> '\u2665'
    Suite.DIAMOND -> '\u2666'
    Suite.CLUB -> '\u2663'
  }
}

fun ansiString( suite : Suite ) : String {
  val builder = StringBuilder()
    builder
        .append( "\u001B[" )
        .append( ansiColor( suite ) )
        .append( ";m" )
        .append( unicodeCharacter( suite ) )

  builder.append( "\u001B[0m" )
  return builder.toString()
}

fun ansiColor( suite : Suite, whiteBackground : Boolean = false ) : Int {
  return when( suite.color ) {
    Color.RED -> 31
    Color.BLACK -> if ( whiteBackground ) 0 else 231
  }
}

fun ansiString( card : Card ) : String {
  return ansiString( listOf( card ) )
}

fun ansiString( cards : Collection< Card > ) : String {
  val builder = StringBuilder()
  for( card in cards ) {
    builder
        .append( "\u001B[" )
        .append( ansiColor( card.suite ) )
        .append( ";m" )
        .append( "\uD83C" )  // Unicode stuff, too.
        .append( unicodeCharacter( card ) )

  }
  builder.append( "\u001B[0m" )
  return builder.toString()
}

fun appendPlayerValues(
    appendable : Appendable,
    title : String,
    map : Map< PlayerIdentity, Int >
) {
  appendable.append( "$title: " )
  map.entries.joinTo( appendable, transform = { e -> e.key.name + "=" + e.value } )
  appendable.eol()
}

fun main( parameters : Array< String > ) {
  println( "\u2660\u2665\u2666\u2663" )
  println( "" +
      unicodeCharacter( Suite.SPADE ) +
      unicodeCharacter( Suite.HEART ) +
      unicodeCharacter( Suite.DIAMOND ) +
      unicodeCharacter( Suite.CLUB )
  )
  println( 
      ansiString( Suite.SPADE ) +
      ansiString( Suite.HEART ) +
      ansiString( Suite.DIAMOND ) +
      ansiString( Suite.CLUB )
  )
}



