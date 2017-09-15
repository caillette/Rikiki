package io.github.caillette.rikiki

import mu.KotlinLogging
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

data class PlayerIdentity( val name : String )

/**
 * Describes what everybody can see. [PlayerActor] can safely access to every member.
 * Values are read-only and guaranteed to not change while [FullGame] calls methods of
 * [PlayerActor].
 *
 * @param decisionCount must be 1 or greater.
 */
abstract class PublicGame(
    val playerIdentities : Set< PlayerIdentity >,
    val decisionCount : Int
) {

  init {
    check( decisionCount > 0 )
  }

  abstract val trump : Card?

  abstract val decisionsForThisTurn : List< Decision >

  abstract val turn : Int

  abstract val firstCard : Card?

  /**
   * @throws IllegalStateException if [PlayerActor.bet] was not called beforehand for everybody.
   */
  abstract val bets : Map< PlayerIdentity, Int >

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
) : PublicGame( Collections.unmodifiableSet( playerIdentities ), decisionCount ),
    Dumpable
{

  /**
   * All the [Card]s in the game, in distribution order. Immutable, no duplicates.
   */
  internal val _cards : List< Card >

  internal val _players : List< PlayerActor >

  private val _trump : Card?

  override val trump : Card?
    get() = _trump

  init {
    check( cards.size >= playerIdentities.size )
    check( playerIdentities.size > 1 )
    _cards = Collections.unmodifiableList( ArrayList( cards ) )

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
    _players = Collections.unmodifiableList( playerActorsBuilder )

    _trump = if( numberOfCardsPlayed < _cards.size ) _cards[ numberOfCardsPlayed ] else null
  }

  private var _bets : Map< PlayerIdentity, Int >? = null

  private val _decisionsForThisTurn = ArrayList< Decision >()

  private var _turn = 0

  fun askPlayersToBet() {
    _decisionsForThisTurn.clear()
    val betsBuilder : MutableMap< PlayerIdentity, Int > = mutableMapOf()
    for( playerActor in _players ) {
      betsBuilder[ playerActor.playerIdentity ] = playerActor.bet()
    }
    _bets = Collections.unmodifiableMap( betsBuilder )
  }

  fun askPlayersToDecide() {
    for( playerActor in _players ) {
      val cardPlayed = playerActor.decide()
      val decision = Decision( _turn, playerActor.playerIdentity, cardPlayed )
      _decisionsForThisTurn.add( decision )
    }
    _turn ++
  }

  override val bets : Map< PlayerIdentity, Int >
    get() {
      val current = _bets
      if( current == null ) {
        throw IllegalStateException( "Bets must occur first" )
      } else {
        return current
      }
    }

  override val turn : Int
    get() = _turn

  override val decisionsForThisTurn : List<Decision>
    get() = Collections.unmodifiableList( ArrayList( _decisionsForThisTurn ) )

  override val firstCard : Card?
    get() = if( _decisionsForThisTurn.isEmpty() ) null else _decisionsForThisTurn.first().card

  override fun dump( i : Int, appendable : Appendable ) {
    val trumpAsString = if( _trump == null ) "" else ansiString( _trump )
    appendable
        .indent( i ).append( FullGame::class.simpleName ).append( '{' ).eol()
        .indentMore( i ).append( "Trump card: " ).append( trumpAsString ).eol()

    for( playerActor in _players ) {
      playerActor.dump( i + 1, appendable )
    }
    appendable
        .indent( i ).append( '}' ).eol()
  }


}

/**
 * Describes a [PlayerActor] playing one [Card]. This is seen by every other [Player] since
 * there is no 'hidden' state.
 * TODO: add some useful precomputed states.
 */
data class Decision( val turnIndex : Int, val playerIdentity : PlayerIdentity, val card : Card )

fun chosable( cards : List< Card >, first : Card? ) : Set< Card > {
  return if( first == null ) {
    LinkedHashSet( cards )
  } else {
    val sameSuiteAsFirst = LinkedHashSet( cards.filter { it.suite == first.suite } )
    if( sameSuiteAsFirst.isEmpty() ) LinkedHashSet( cards ) else sameSuiteAsFirst
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

  private val logger = KotlinLogging.logger(
      PlayerActor::class.simpleName + "." +
          playerIdentity.name.replace( Regex( "[^a-zA-Z0-9]+" ), "" ) )

  val _hand : MutableList< Card >

  init {
    _hand = ArrayList( initialHand )
  }

  /**
   * Must be called before first call to [decide] or [decision].
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