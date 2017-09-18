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
import io.github.caillette.rikiki.strategy.AimZero
import io.github.caillette.rikiki.strategy.ProbabilisticLight
import io.github.caillette.rikiki.toolkit.eol
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class Tournament(
    private val packets : Array< Packet >,
    private val players : Set< PlayerIdentity >
) {

  constructor() : this(
      arrayOf( Packet(), Packet() ),
      players(
        listOf( ProbabilisticLight.factory, AimZero.factory ),
        "Alice", "Bob", "Charlie", "Dylan", "Eddie", "Fitz"
  ) )

  companion object {
    val winToken = "<"
  }

  private val playerNameMaximumLength = players.map { it -> it.name.length }.max() !!
  private val highestTurnCount = packets.flatMap { it -> it.cards }.count() / players.size

  private fun turnCountSequence() : Sequence< Int > {
    val baseRange = 2 until highestTurnCount
    val baseSequence : Sequence< Int > = ( 2 until highestTurnCount ).asSequence()
    return baseSequence + baseRange.reversed()
  }

  /**
   * @return a [Map] containing the final score for each [Strategy], averaged for those which
   *     appear more than once.
   */
  fun run( random: Random, printDetail : Boolean ) : Brief {
    val allScores : MutableMap< Strategy.Factory, Int > = HashMap()
    players.forEach( { allScores[ it.strategyFactory ] = 0 } )

    val strategyAppearance = players.strategyAppearance()

    for( gameIndex in turnCountSequence() ) {
      val cards = shuffle( random, *packets )
      val fullGame = FullGame(
          players,
          cards,
          gameIndex
      )

      val report = StringBuilder()
      fullGame.runTheBids()
      if( printDetail ) appendHeader( report, fullGame )

      while ( fullGame.phase != PublicGame.Phase.COMPLETE ) {
        val winningDecision = fullGame.runTheTurn()
        if( printDetail ) appendDecision(report, fullGame, winningDecision)
      }
      if( printDetail ) appendFooter( report, fullGame )


      fullGame.scores.forEach( {
        val previousScore = allScores[ it.key.strategyFactory ] !!
        allScores[ it.key.strategyFactory ] = previousScore + it.value
      } )

      if( printDetail ) println( report.toString() )
    }

    return Brief(
        allScores.mapValues( { it.value / strategyAppearance[it.key] !! } ),
        turnCountSequence().count()
    )
  }

  private fun appendHeader( report : Appendable, fullGame : FullGame ) {
    report.eol().append( "Starting game with " )
        .append( fullGame.playerIdentities.size.toString() + " players for " )
        .append( fullGame.turnCount.toString() + " turns " )
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
    for( ( playerIdentity, card ) in fullGame.decisionsInThisTurn) {
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

  data class Brief( val strategyScores : Map< Strategy.Factory, Int >, val gameCount : Int ) {

    constructor() : this( mapOf(), 0 )

    operator fun plus( other : Brief ) : Brief {
      val newScores = HashMap< Strategy.Factory, Int >( strategyScores )
      other.strategyScores.forEach(
          { newScores[ it.key ] = newScores.getOrDefault( it.key, 0 ) + it.value } )
      return Brief( newScores, gameCount + other.gameCount )
    }
  }

}

fun appendBrief( report : Appendable, brief : Tournament.Brief ) {
  report
      .eol()
      .append( "Strategy scores (over ${brief.gameCount} games):" )
      .eol()

  brief.strategyScores.entries.joinTo(
      report,
      separator = "\n",
      transform = {
        val scoreAverage = it.value / brief.gameCount
        it.key.name() + ": " + it.value +
            " (" + scoreAverage + " point" + ( if ( scoreAverage > 1 ) "s" else "" ) +
            "/tournament)"
      }
  )

  report.eol()
}


fun runTournaments(
    runCount : Int,
    trueRandom : Boolean,
    printGameReport : Boolean,
    processorUsage : Float = 1.0F
) {
  val parallelism : Int = if( printGameReport ) 1 else numberOfProcessors( processorUsage )
  val executorService : ExecutorService = Executors.newFixedThreadPool( parallelism )

  var consolidatedBrief = Tournament.Brief()

  val futures = Array( parallelism, {
    executorService.submit( Callable< Tournament.Brief >( {
      var brief = Tournament.Brief()
      val localRunCount = runCount / parallelism +
          ( if( it == 1 ) runCount % parallelism else 0 )
      for( tournamenIndex in 1..localRunCount ) {
        val random = if( trueRandom ) Random() else Random( 0 )
        brief += Tournament().run(random, printGameReport )
      }
      brief
    } ) )
  } )
  futures.forEach { consolidatedBrief += it.get() }
  executorService.shutdown()

  val report = StringBuilder()
  appendBrief( report, consolidatedBrief )
  println( report.toString() )

}

private fun numberOfProcessors( availableProcessorUsage : Float ) : Int {
  return (Runtime.getRuntime().availableProcessors() * availableProcessorUsage).toInt()
}

private val logger = KotlinLogging.logger {}


fun main( arguments : Array< String > ) {
  val runCount = 100_000
  logger.info(
      "Now running " + runCount + " " + Tournament::class.simpleName + "s with defaults ..." )
  runTournaments(
      runCount,
      true,
      printGameReport = false,
      processorUsage = 0.95F // Let system breathe. At 1F, VisualVM chokes.
  )
  logger.info( "Run complete." )
}
