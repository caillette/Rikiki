package io.github.caillette.rikiki

import io.github.caillette.rikiki.card.Packet
import io.github.caillette.rikiki.card.shuffle
import io.github.caillette.rikiki.game.FullGame
import io.github.caillette.rikiki.game.PublicGame
import io.github.caillette.rikiki.game.ansiString
import io.github.caillette.rikiki.game.appendPlayerValues
import io.github.caillette.rikiki.game.players
import io.github.caillette.rikiki.toolkit.eol
import java.util.*

fun main( arguments : Array< String > ) {
  val cards = shuffle( Random(), Packet(), Packet() )
  val fullGame = FullGame( players( "Alice", "Bob", "Charlie", "Dylan" ), cards, 6 )
  val report = StringBuilder()

  report.eol().append( "Starting game with " )
  fullGame.playerIdentities.joinTo( report, transform = { ( name ) -> name } )
  report.eol()

  val trump = fullGame.trump
  if( trump == null ) {
    report.append( "No trump").eol()
  } else {
    report.append( "Trump: ").append( ansiString( trump ) ).eol()
  }

  fullGame.runTheBids()
  appendPlayerValues( report, "Bids", fullGame.bids )
  report.eol()

  while( fullGame.phase != PublicGame.Phase.COMPLETE ) {
    val winningDecision = fullGame.runTheTrick()
    for( ( playerIdentity, card ) in fullGame.decisionsForThisTrick ) {
      report
          .append( playerIdentity.name ).append( " played " )
          .append( ansiString( card ) )
          .eol()
    }
    report
        .append( "Winner of this trick: " ).append( winningDecision.playerIdentity.name )
        .eol().eol()
  }
  appendPlayerValues( report, "Scores", fullGame.scores )
  report.eol()


  println( report.toString() )

}

