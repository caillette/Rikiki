package io.github.caillette.rikiki
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class PlayerActorTest {

  @Test
  fun createPlayerActors() {
    val fullGame = FullGame( setOf( Fixture.alice, Fixture.bob ), Fixture.cards, 2 )
    assertSame( fullGame._players.size, 2 )
    val alice = fullGame._players[ 0 ]
    val bob = fullGame._players[ 1 ]
    assertSame( alice.playerIdentity, Fixture.alice )
    assertSame( bob.playerIdentity, Fixture.bob )

    assertEquals( fullGame._cards.size, 5 )

    logger.info( "Logging works" )

    assertMatches( Fixture.ACE_OF_SPADES, alice._hand[ 0 ] )
    assertMatches( Fixture.KING_OF_CLUBS, bob._hand[ 0 ] )
    assertMatches( Fixture.QUEEN_OF_HEARTS, alice._hand[ 1 ] )
    assertMatches( Fixture.TEN_OF_DIAMONDS, bob._hand[ 1 ] )
    assertNotNull( fullGame.trump )
    assertMatches( Fixture.TWO_OF_CLUBS, fullGame.trump!! )
  }


  @Test
  fun askPlayersToBet() {
    val fullGame = FullGame( setOf( Fixture.alice, Fixture.bob ), Fixture.cards, 2 )
    assertThrows( IllegalStateException::class.javaObjectType, { fullGame.bets[ Fixture.alice ] } )
    fullGame.askPlayersToBet()
    assertEquals( fullGame.bets[ Fixture.alice ], 0 )
    assertEquals( fullGame.bets[ Fixture.bob ], 0 )
  }

  private val logger = KotlinLogging.logger {}

}

fun assertMatches( cardPattern : Fixture.CardPattern, card : Card ) {
  assertTrue( cardPattern.forCard.invoke( card ), "$card doesn't match $cardPattern" )
}

object Fixture {

  data class CardPattern( private val figure : Figure, private val suite : Suite ) {
    val forCard : ( Card ) -> Boolean = { ( _ , f, s ) -> f == figure && s == suite }
    val raw : ( Figure, Suite ) -> Boolean = { f, s -> f == figure && s == suite }
  }

  val ACE_OF_SPADES = CardPattern( Figure.ACE, Suite.SPADE )
  val KING_OF_CLUBS = CardPattern( Figure.KING, Suite.CLUB )
  val QUEEN_OF_HEARTS = CardPattern( Figure.QUEEN, Suite.HEART )
  val TEN_OF_DIAMONDS = CardPattern( Figure.TEN, Suite.DIAMOND )
  val TWO_OF_CLUBS = CardPattern( Figure.TWO, Suite.CLUB )

  private fun Packet.firstMatch( figure : Figure, suite : Suite ) : Card {
    return this.cards.first( { ( _ , f, s ) -> f == figure && s == suite } )
  }

  val alice = PlayerIdentity( "Alice" )
  val bob = PlayerIdentity( "Bob" )

  val cards = Packet(
      ACE_OF_SPADES.raw,
      KING_OF_CLUBS.raw,
      QUEEN_OF_HEARTS.raw,
      TEN_OF_DIAMONDS.raw,
      TWO_OF_CLUBS.raw
  ).cards


}