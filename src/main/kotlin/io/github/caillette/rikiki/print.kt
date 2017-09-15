package io.github.caillette.rikiki

/**
 * There are compact Unicode characters:
 * https://en.wikipedia.org/wiki/Playing_cards_in_Unicode
 */
fun unicodeCharacter( card : Card) : Char {
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
        .append(ansiColor(card.suite))
        .append( ";m" )
        .append( "\uD83C" )  // Unicode stuff, too.
        .append(unicodeCharacter(card))

  }
  builder.append( "\u001B[0m" )
  return builder.toString()
}

interface Dumpable {

  fun Appendable.eol() : Appendable {
    append( "\n" )
    return this
  }

  fun Appendable.indentMore( indentCount : Int ) : Appendable {
    return indent( indentCount + 1 )
  }

  fun Appendable.indent( indentCount : Int ) : Appendable {
    for( i in 0 until indentCount ) {
      append( "  " )
    }
    return this
  }

  fun dump( i : Int, appendable : Appendable )

  fun dumpToConsole() {
    val builder = StringBuilder()
    dump( 0, builder )
    println( builder.toString() )
  }
}