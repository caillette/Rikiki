package io.github.caillette.rikiki.game

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import io.github.caillette.rikiki.toolkit.Dumpable
import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.card.Suite
import io.github.caillette.rikiki.toolkit.addTo
import io.github.caillette.rikiki.toolkit.append
import io.github.caillette.rikiki.toolkit.eol
import io.github.caillette.rikiki.toolkit.indent
import io.github.caillette.rikiki.toolkit.indentMore
import io.github.caillette.rikiki.toolkit.newFilledMap
import io.github.caillette.rikiki.toolkit.rollFirst
import mu.KotlinLogging

/**
 * Contains game's internals.
 *
 * @param playerIdentities the [PlayerIdentity] in the order defined by the default iterator
 *     of the given object. Order matters because it defines order of play.
 *
 * @param cards all the [Card]s, in the order defined by default iterator of the given object.
 *     Orders matters because we assume that shuffling is correct. Use of a Set guarantees
 *     the same [Card] doesn't appear twice (since we can mix packets).
 */
class FullGame(
    playerIdentities : Set< PlayerIdentity >,
    cards : Set< Card >,
    decisionCount : Int
) : PublicGame(
    ImmutableSet.copyOf( playerIdentities ), decisionCount ),
    Dumpable
{

  private val logger = KotlinLogging.logger { }

  /**
   * All the [Card]s in the game, in distribution order. Immutable, no duplicates.
   */
  internal val _cards : ImmutableList< Card >

  internal val _players : ImmutableList< PlayerActor >

  private var _firstToPlay : PlayerActor

  private val _trump : Suite?

  private var _trickWins : ImmutableMap< PlayerIdentity, Int >

  private var _scores : ImmutableMap< PlayerIdentity, Int >

  private var _bid : ImmutableMap< PlayerIdentity, Int >? = null

  /**
   * We recreate a fresh instance each time we add an element. But this saves defensive copies
   * when [PlayerActor] calls [PublicGame.decisionsForThisTrick].
   * TODO: use a backward-chained list.
   */
  private var _decisionsForThisTrick : ImmutableList< Decision > = ImmutableList.of()

  private var _trick = 0

  private var _phase = Phase.NEW

  init {
    check( cards.size >= playerIdentities.size )
    check( playerIdentities.size > 1 )
    _cards = ImmutableSet.copyOf(cards).asList()

    val numberOfCardsPlayed = playerIdentities.size * decisionCount
    val initialHands : Array< MutableSet< Card > > =
        Array( playerIdentities.size, { _ -> LinkedHashSet< Card >() } )
    for( cardIndex in ( 0 until numberOfCardsPlayed ) ) {
      initialHands[ cardIndex % playerIdentities.size ].add( _cards[ cardIndex ] )
    }

    val playerActorsBuilder = ArrayList< PlayerActor >( playerIdentities.size )
    playerIdentities.mapIndexedTo( playerActorsBuilder ) {
      index, playerIdentity ->
      PlayerActor(this, playerIdentity, initialHands[index])
    }
    _players = ImmutableList.copyOf( playerActorsBuilder )
    _firstToPlay = _players.first()

    _trump = if( numberOfCardsPlayed < _cards.size ) _cards[ numberOfCardsPlayed ].suite else null
    _trickWins = newFilledMap(playerIdentities, 0)
    _scores = newFilledMap(playerIdentities, 0)
  }

  fun runTheBids() {
    val betsBuilder : MutableMap<PlayerIdentity, Int > = mutableMapOf()
    for( playerActor in _players ) {
      betsBuilder[ playerActor.playerIdentity ] = playerActor.bet()
    }
    _bid = ImmutableMap.copyOf( betsBuilder )
    _phase = Phase.BIDS_DONE
  }

  fun runTheTrick() : Decision {
    check( trick < trickCount )
    check( _phase == Phase.BIDS_DONE || _phase == Phase.DECIDING )
    _phase = Phase.DECIDING

    _decisionsForThisTrick = ImmutableList.of()
    val playersInOrder = rollFirst( _players, _firstToPlay )

    for( playerActor in playersInOrder ) {
      val cardPlayed = playerActor.decisionForCurrentTrick()
      val decision = Decision(playerActor.playerIdentity, cardPlayed )
      _decisionsForThisTrick = _decisionsForThisTrick.append( decision )
    }
    val winningDecision = best(decisionsForThisTrick, trump )
    logger.info( "Winning decision: $winningDecision " )

    _firstToPlay = _players.find( winningDecision.playerIdentity )
    _trickWins = _trickWins.addTo( winningDecision.playerIdentity, 1 )
    _trick ++

    if( trick >= trickCount) {
      _phase = Phase.COMPLETE
      for( player in playerIdentities ) {
        _scores = _scores.addTo( player, score( bids[ player ]!!, trickWins[ player ] !! ) )
      }
    }

    return winningDecision
  }

  override val trump : Suite?
    get() = _trump

  override val trick : Int
    get() = _trick

  override val bids : Map< PlayerIdentity, Int >
    get() {
      val current = _bid
      if( current == null ) {
        throw IllegalStateException( "Bids must occur first" )
      } else {
        return current
      }
    }

  override val scores : Map< PlayerIdentity, Int >
    get() = _scores

  override val trickWins : Map< PlayerIdentity, Int >
    get() = _trickWins

  override val decisionsForThisTrick : List< Decision >
    get() = _decisionsForThisTrick

  override val firstCard : Card?
    get() = if( _decisionsForThisTrick.isEmpty() ) null else _decisionsForThisTrick.first().card

  override val phase : Phase
    get() = _phase


  override fun dump( i : Int, appendable : Appendable ) {

    fun appendMap( name : String, map : Map< PlayerIdentity, Int > ) {
      appendable.indentMore( i ).append( "$name: " )
      map.entries.joinTo( appendable, transform = { e -> e.key.name + "=" + e.value } )
      appendable.eol()
    }

    val trumpAsString = if( _trump == null ) "" else ansiString(
        _trump)
    appendable
        .indent( i ).append( FullGame::class.simpleName ).append( '{' ).eol()
        .indentMore( i ).append( "Trump card: " ).append( trumpAsString ).eol()
        .indentMore( i ).append( "Turn: " ).append( trick.toString() ).eol()

    if( phase != Phase.NEW) {
      appendMap( "Bets", bids )
    }
    appendMap( "Wins", trickWins)
    appendMap( "Scores", scores )

    for( playerActor in _players ) {
      playerActor.dump( i + 1, appendable )
    }
    appendable
        .indent( i ).append( '}' ).eol()
  }

}

private fun ImmutableList< PlayerActor >.find( playerIdentity : PlayerIdentity ) : PlayerActor {
  return this.first{ playerActor -> playerActor.playerIdentity == playerIdentity }
}

