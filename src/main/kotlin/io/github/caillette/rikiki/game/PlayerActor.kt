package io.github.caillette.rikiki.game

import io.github.caillette.rikiki.toolkit.Dumpable
import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.toolkit.eol
import io.github.caillette.rikiki.toolkit.indent
import io.github.caillette.rikiki.toolkit.indentMore
import mu.KotlinLogging

/**
 * Represents a player inside a [FullGame].
 *
 * @param game gives a reference to other [PlayerIdentity]s.
 * @param playerIdentity must exist in given [PublicGame].
 * @param initialHand
 */
class PlayerActor(
    private val game : PublicGame,
    val playerIdentity : PlayerIdentity,
    initialHand : Set< Card >
) : Dumpable {

  private val logger = KotlinLogging.logger(
      PlayerActor::class.simpleName + "." +
          playerIdentity.name.replace( Regex( "[^a-zA-Z0-9]+" ), "" ) )

  val _hand : MutableList< Card >
  val _strategy : Strategy

  init {
    _hand = ArrayList( initialHand )
    _strategy = playerIdentity.strategyFactory.newStrategy( game, playerIdentity )
  }

  /**
   * Must be called before first call to [decisionForCurrentTrick].
   */
  fun bet() : Int {
    val bet = _strategy.bid( _hand )
    logger.info( "Betting $bet." )
    return bet
  }

  fun decisionForCurrentTrick() : Card {
    val chosable = chosable( _hand, game.firstCard )
    val chosen = _strategy.decideForTrick(_hand, chosable )
    check( chosable.contains( chosen ) )
    _hand.remove( chosen )
    logger.info( "Deciding $chosen." )
    return chosen
  }


  override fun dump( i : Int, appendable : Appendable ) {
    appendable
        .indent( i ).append( PlayerActor::class.simpleName ).append( '{' ).eol()
        .indentMore( i ).append( "Player: " ).append( playerIdentity.name ).eol()
        .indentMore( i ).append( "Hand: " ).append( ansiString( _hand ) ).eol()
        .indent( i ).append( '}' ).eol()
  }
}