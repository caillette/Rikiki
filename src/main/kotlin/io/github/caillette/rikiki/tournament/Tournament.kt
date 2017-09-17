package io.github.caillette.rikiki.tournament

import io.github.caillette.rikiki.card.Packet
import io.github.caillette.rikiki.card.shuffle
import io.github.caillette.rikiki.game.Decision
import io.github.caillette.rikiki.game.FullGame
import io.github.caillette.rikiki.game.PlayerIdentity
import io.github.caillette.rikiki.game.PublicGame
import io.github.caillette.rikiki.game.Strategy
import io.github.caillette.rikiki.game.ansiString
import io.github.caillette.rikiki.game.appendPlayerValues
import io.github.caillette.rikiki.game.players
import io.github.caillette.rikiki.game.strategyAppearance
import io.github.caillette.rikiki.strategy.ProbabilisticLight
import io.github.caillette.rikiki.toolkit.eol
import java.util.*
import kotlin.collections.HashMap

class Tournament(
    private val packets : Array< Packet >,
    private val players : Set< PlayerIdentity >
) {

  constructor() : this(
      arrayOf( Packet(), Packet() ),
      players(
        listOf( ProbabilisticLight.factory ),
        "Alice", "Bob", "Charlie", "Dylan", "Eddie", "Fitz"
  ) )

  companion object {
    val winToken = "<"
  }

  private val playerNameMaximumLength = players.map { it -> it.name.length }.max() !!
  private val highestTrickCount = packets.flatMap { it -> it.cards }.count() / players.size

  fun trickCountSequence() : Sequence< Int > {
    val baseRange = 2 until highestTrickCount
    val baseSequence : Sequence< Int > = ( 2 until highestTrickCount ).asSequence()
    return baseSequence + baseRange.reversed()
  }

  /**
   * @return a [Map] containing the final score for each [Strategy], averaged for those which
   *     appear more than once.
   */
  fun run( printDetail : Boolean ) : Map< Strategy.Factory, Int > {
    val allScores : MutableMap< Strategy.Factory, Int > = HashMap()
    players.forEach( { allScores[ it.strategyFactory ] = 0 } )

    val strategyAppearance = players.strategyAppearance()

    for( gameIndex in trickCountSequence() ) {
      val cards = shuffle( Random(), *packets )
      val fullGame = FullGame(
          players,
          cards,
          gameIndex
      )

      val report = StringBuilder()
      fullGame.runTheBids()
      if( printDetail ) appendHeader( report, fullGame )

      while ( fullGame.phase != PublicGame.Phase.COMPLETE ) {
        val winningDecision = fullGame.runTheTrick()
        if( printDetail ) appendDecision(report, fullGame, winningDecision)
      }
      if( printDetail ) appendFooter( report, fullGame )


      fullGame.scores.forEach( {
        val previousScore = allScores[ it.key.strategyFactory ] !!
        allScores[ it.key.strategyFactory ] = previousScore + it.value
      } )

      if( printDetail ) println( report.toString() )
    }

    return allScores.mapValues( { it.value / strategyAppearance[it.key] !! } )
  }

  private fun appendHeader( report : Appendable, fullGame : FullGame ) {
    report.eol().append( "Starting game with " )
        .append( fullGame.playerIdentities.size.toString() + " players for " )
        .append( fullGame.trickCount.toString() + " tricks " )
        .append( "using " + fullGame.cardCount + " cards" )
        .eol()

    val trump = fullGame.trump
    if( trump == null ) {
      report.append("No trump").eol()
    } else {
      report.append( "Trump: " + ansiString( trump ) ).eol()
    }
    appendPlayerValues( report, "Bids", fullGame.bids )
    report.eol()
  }

  private fun appendDecision(
      report : Appendable,
      fullGame : FullGame,
      winningDecision : Decision
  ) {
    for( ( playerIdentity, card ) in fullGame.decisionsForThisTrick ) {
      report.append( playerIdentity.name.padStart(playerNameMaximumLength) )
      report.append( ": " ).append( ansiString( card ) )
      if( playerIdentity == winningDecision.playerIdentity ) {
        report.append( winToken )
      }
      else {
        report.append( " ".repeat( winToken.length ) )
      }
      report.append( "  " )
    }
    report.eol()
  }

  private fun appendFooter( report : Appendable, fullGame : FullGame) {
    report.eol()
    appendPlayerValues( report, "Scores", fullGame.scores )
    report.eol()

    fullGame.playerIdentities.joinTo(
        report,
        separator = "\n",
        transform = {
          e -> e.name.padStart( playerNameMaximumLength ) + " using " + e.strategyFactory.name()
        }
    )

  }


}

fun appendStrategyScores(
    report : Appendable,
    scores : Map< Strategy.Factory, Int >,
    gameCount : Int
) {
  report
      .eol()
      .append( "Strategy scores (over $gameCount games):" )
      .eol()

  scores.entries.joinTo(
      report,
      separator = "\n",
      transform = { e ->
        e.key.name() + ": " + e.value
      }
  )

  report.eol()
}

fun runTournaments(runCount : Int, printGameReport : Boolean) {
  val strategyScores : MutableMap< Strategy.Factory, Int > = HashMap()
  var gameCount = 0
  for( tournamenIndex in 1..runCount ) {
    val tournament = Tournament()
    val newScores = tournament.run( printGameReport )
    gameCount += tournament.trickCountSequence().count()
    newScores.forEach(
        { strategyScores[ it.key ] = strategyScores.getOrDefault( it.key, 0 ) + it.value } )
  }
  val report = StringBuilder()
  appendStrategyScores( report, strategyScores, gameCount )
  println( report.toString() )

}

fun main( arguments : Array< String > ) {
  runTournaments( 1000, false )
}
