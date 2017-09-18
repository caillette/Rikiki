package io.github.caillette.rikiki.toolkit.tournament

import io.github.caillette.rikiki.tournament.runTournaments
import org.junit.jupiter.api.Test

class TournamentTest {

  @Test
  fun singleTournament() {
    runTournaments( 1, false, true )
  }

  @Test
  fun manyTournaments() {
    runTournaments(
        Runtime.getRuntime().availableProcessors() * 2,
        trueRandom = true,
        printGameReport = false // Triggers parallel run.
    )
  }

}