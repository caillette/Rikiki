package io.github.caillette.rikiki.toolkit.tournament

import io.github.caillette.rikiki.tournament.runTournaments
import org.junit.jupiter.api.Test

class TournamentTest {

  @Test
  fun singleTournament() {
    runTournaments( 2, true )
  }

}