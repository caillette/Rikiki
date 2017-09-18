package io.github.caillette.rikiki.toolkit.card

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.card.Figure
import io.github.caillette.rikiki.card.Packet
import io.github.caillette.rikiki.card.Suite

object CardFixture {

  data class CardPattern( private val figure : Figure, val suite : Suite) {
    val forCard : ( Card ) -> Boolean = { card -> card.figure == figure && card.suite == suite }
    val raw : ( Figure, Suite ) -> Boolean = { f, s -> f == figure && s == suite }
  }

  val ACE_OF_SPADES = CardPattern( Figure.ACE, Suite.SPADE )
  val KING_OF_CLUBS = CardPattern( Figure.KING, Suite.CLUB )
  val QUEEN_OF_HEARTS = CardPattern( Figure.QUEEN, Suite.HEART )
  val TEN_OF_DIAMONDS = CardPattern( Figure.TEN, Suite.DIAMOND )
  val TWO_OF_CLUBS = CardPattern( Figure.TWO, Suite.CLUB )

  val cards = Packet(
      ACE_OF_SPADES.raw,
      KING_OF_CLUBS.raw,
      QUEEN_OF_HEARTS.raw,
      TEN_OF_DIAMONDS.raw,
      TWO_OF_CLUBS.raw
  ).cards

  fun card( cardPattern : CardPattern ) : Card =
      cards.toList().first { cardPattern.forCard.invoke( it ) }
}
