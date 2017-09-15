package io.github.caillette.rikiki
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlayerActorTest {

  @Test
  fun junit5IsWorking() {
    val card = Packet().cards.iterator().next()
    assertEquals( card, card )
  }
}