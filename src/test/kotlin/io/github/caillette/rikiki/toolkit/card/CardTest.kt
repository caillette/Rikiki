package io.github.caillette.rikiki.toolkit.card

import io.github.caillette.rikiki.card.Figure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CardTest {

  @Test
  fun figureStrength() {
    val unit = 0.077F
    assertEquals( unit * 13, Figure.ACE.strength,   0.01F )
    assertEquals( unit * 1,  Figure.TWO.strength,   0.01F )
    assertEquals( unit * 2,  Figure.THREE.strength, 0.01F )
    assertEquals( unit * 3,  Figure.FOUR.strength,  0.01F )
    assertEquals( unit * 4,  Figure.FIVE.strength,  0.01F )
    assertEquals( unit * 5,  Figure.SIX.strength,   0.01F )
    assertEquals( unit * 6,  Figure.SEVEN.strength, 0.01F )
    assertEquals( unit * 7,  Figure.EIGHT.strength, 0.01F )
    assertEquals( unit * 8,  Figure.NINE.strength,  0.01F )
    assertEquals( unit * 9,  Figure.TEN.strength,   0.01F )
    assertEquals( unit * 10, Figure.JACK.strength,  0.01F )
    assertEquals( unit * 11, Figure.QUEEN.strength, 0.01F )
    assertEquals( unit * 12, Figure.KING.strength,  0.01F )

    assertTrue( Figure.comparatorByStrength.compare( Figure.ACE,   Figure.KING ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.KING,  Figure.QUEEN ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.QUEEN, Figure.JACK ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.JACK,  Figure.TEN ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.TEN,   Figure.NINE ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.NINE,  Figure.EIGHT ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.EIGHT, Figure.SEVEN ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.SEVEN, Figure.SIX ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.SIX,   Figure.FIVE ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.FIVE,  Figure.FOUR ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.FOUR,  Figure.THREE ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.THREE, Figure.TWO ) > 0 )
    assertTrue( Figure.comparatorByStrength.compare( Figure.TWO,   Figure.ACE ) < 0 )
  }

}