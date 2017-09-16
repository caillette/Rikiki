package io.github.caillette.rikiki

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import io.github.caillette.rikiki.toolkit.append
import io.github.caillette.rikiki.toolkit.addTo
import io.github.caillette.rikiki.toolkit.newFilledMap
import mu.KotlinLogging

data class PlayerIdentity( val name : String )

/**
 * Describes what everybody can see. [PlayerActor] can safely access to every member.
 * Values are read-only and guaranteed to not change while [FullGame] calls methods of
 * [PlayerActor].
 *
 * Terminology based on <a href='https://www.pagat.com/exact/ohhell.html'>Oh Hell!</a> rules.
 *
 * @param trickCount how many times every [PlayerActor] gets to decide. Must be 1 or greater.
 */
abstract class PublicGame(
    val playerIdentities : Set< PlayerIdentity >,
    val trickCount : Int
) {

  init {
    check( trickCount > 0 )
  }

  abstract val trump : Card?

  /**
   * @return an immutable [List].
   */
  abstract val decisionsForThisTrick : List< Decision >

  abstract val trick : Int

  abstract val firstCard : Card?

  /**
   * @return an immutable [Map] reflecting last bets.
   * @throws IllegalStateException if [PlayerActor.bet] was not called beforehand for everybody.
   */
  abstract val bids : Map< PlayerIdentity, Int >

  abstract val scores : Map< PlayerIdentity, Int >

  /**
   * @return an immutable [Map] reflecting last wins.
   */
  abstract val tricksWon : Map< PlayerIdentity, Int >

  abstract val phase : Phase

  enum class Phase {
    NEW, BIDS_DONE, DECIDING, COMPLETE
  }

}

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
) : PublicGame( ImmutableSet.copyOf( playerIdentities ), decisionCount ),
    Dumpable
{

  private val logger = KotlinLogging.logger { }

  /**
   * All the [Card]s in the game, in distribution order. Immutable, no duplicates.
   */
  internal val _cards : ImmutableList< Card >

  internal val _players : ImmutableList< PlayerActor >

  private val _trump : Card?

  private var _tricksWon : ImmutableMap< PlayerIdentity, Int >

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
    _cards = ImmutableSet.copyOf( cards ).asList()

    val numberOfCardsPlayed = playerIdentities.size * decisionCount
    val initialHands : Array< MutableSet< Card > > =
        Array( playerIdentities.size, { _ -> LinkedHashSet< Card >() } )
    for( cardIndex in ( 0 until numberOfCardsPlayed ) ) {
      initialHands[ cardIndex % playerIdentities.size ].add( _cards[ cardIndex ] )
    }

    val playerActorsBuilder = ArrayList< PlayerActor >( playerIdentities.size )
    playerIdentities.mapIndexedTo( playerActorsBuilder ) {
      index, playerIdentity -> PlayerActor( this, playerIdentity, initialHands[ index ] )
    }
    _players = ImmutableList.copyOf( playerActorsBuilder )

    _trump = if( numberOfCardsPlayed < _cards.size ) _cards[ numberOfCardsPlayed ] else null
    _tricksWon = newFilledMap( playerIdentities, 0 )
    _scores = newFilledMap( playerIdentities, 0 )
  }

  fun askPlayersToBet() {
    val betsBuilder : MutableMap< PlayerIdentity, Int > = mutableMapOf()
    for( playerActor in _players ) {
      betsBuilder[ playerActor.playerIdentity ] = playerActor.bet()
    }
    _bid = ImmutableMap.copyOf( betsBuilder )
    _phase = Phase.BIDS_DONE
  }

  fun askPlayersToDecide() {
    check( trick < trickCount)
    check( _phase == Phase.BIDS_DONE || _phase == Phase.DECIDING )
    _phase = Phase.DECIDING

    _decisionsForThisTrick = ImmutableList.of()
    for( playerActor in _players ) {  // TODO: start with last winner if any.
      val cardPlayed = playerActor.decide()
      val decision = Decision(playerActor.playerIdentity, cardPlayed )
      _decisionsForThisTrick = _decisionsForThisTrick.append( decision )
    }
    val winningDecision = best(decisionsForThisTrick, trump )
    logger.info( "Winning decision: $winningDecision ")
    _tricksWon = _tricksWon.addTo( winningDecision.playerIdentity, 1 )
    _trick ++

    if( trick >= trickCount) {
      _phase = Phase.COMPLETE
      for( player in playerIdentities ) {
        _scores = _scores.addTo( player, score( bids[ player ]!!, tricksWon[ player ]!! ) )
      }
    }
  }

  override val trump : Card?
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

  override val tricksWon : Map< PlayerIdentity, Int >
    get() = _tricksWon

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

    val trumpAsString = if( _trump == null ) "" else ansiString( _trump )
    appendable
        .indent( i ).append( FullGame::class.simpleName ).append( '{' ).eol()
        .indentMore( i ).append( "Trump card: " ).append( trumpAsString ).eol()
        .indentMore( i ).append( "Turn: " ).append( trick.toString() ).eol()

    if( phase != Phase.NEW ) {
      appendMap( "Bets", bids )
    }
    appendMap( "Wins", tricksWon )
    appendMap( "Scores", scores )

    for( playerActor in _players ) {
      playerActor.dump( i + 1, appendable )
    }
    appendable
        .indent( i ).append( '}' ).eol()
  }

}

/**
 * Describes a [PlayerActor] playing one [Card]. Every [PlayerActor] can see a [Decision].
 * TODO: add some useful precomputed states.
 */
data class Decision( val playerIdentity : PlayerIdentity, val card : Card ) {
  companion object {
    val comparator : Comparator< Decision > = Comparator( { d1, d2 ->
      Figure.comparatorByStrength.compare( d1.card.figure, d2.card.figure ) } )
  }
}

/**
 * Calculate each [PlayerIdentity]'s score.
 * Formula from [Rikiki szabályok Wikipedián](https://hu.wikipedia.org/wiki/Rikiki) (in Hungarian).
 */
fun score( bid : Int, trick : Int ) : Int {
  check( bid >= 0 )
  check( trick >= 0 )
  return if( bid == trick ) {
    10 + 2 * trick
  } else {
    Math.abs( bid - trick ) * -2
  }
}


fun best( decisions : List< Decision >, trump : Card? ) : Decision {
  check( decisions.isNotEmpty() )

  fun select( decisions : List< Decision >, suite : Suite ) : MutableList< Decision > {
    return ArrayList( decisions.filter { it.card.suite == suite } )
  }

  fun sameSuiteAsFirst() : MutableList< Decision > {
    return select( decisions, decisions.first().card.suite )
  }

  val selectable : MutableList< Decision >
  selectable = if( trump == null ) {
    sameSuiteAsFirst()
  } else {
    val trumpOnly = select( decisions, trump.suite )
    if( trumpOnly.isEmpty() ) sameSuiteAsFirst() else trumpOnly
  }
  selectable.sortWith( Decision.comparator )
  return selectable.first()
}

fun chosable( cards : List< Card >, first : Card? ) : Set< Card > {
  return if( first == null ) {
    ImmutableSet.copyOf( cards )
  } else {
    val sameSuiteAsFirst = ImmutableSet.copyOf( cards.filter { it.suite == first.suite } )
    if( sameSuiteAsFirst.isEmpty() ) ImmutableSet.copyOf( cards ) else sameSuiteAsFirst
  }
}


/**
 * @param game gives a reference to other [PlayerIdentity]s.
 * @param playerIdentity must exist in given [PublicGame].
 * @param initialHand
 */
class PlayerActor(
    private val game : PublicGame,
    val playerIdentity : PlayerIdentity,
    initialHand : Set< Card >
) : Dumpable {

  private val logger = KotlinLogging.logger( PlayerActor::class.simpleName + "." +
      playerIdentity.name.replace( Regex( "[^a-zA-Z0-9]+" ), "" ) )

  val _hand : MutableList< Card >

  init {
    _hand = ArrayList( initialHand )
  }

  /**
   * Must be called before first call to [decide].
   */
  fun bet() : Int {
    val bet = 0
    logger.info( "Betting $bet." )
    return bet
  }

  fun decide() : Card {
    // Keep it simple for now.
    val chosen = chosable( _hand, game.firstCard ).first()
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