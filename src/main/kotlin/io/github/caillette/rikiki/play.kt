package io.github.caillette.rikiki

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

data class PlayerIdentity( val name : String )

/**
 * Describes what everybody can see. [PlayerActor] can safely access to every member.
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
  private val _cards : List< Card >

  private val _players : List< PlayerActor >

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

  var _bets : Map< PlayerIdentity, Int >? = null

  fun askPlayersToBet() {
    val betsBuilder : MutableMap< PlayerIdentity, Int > = mutableMapOf()
    for( playerActor in _players ) {
      betsBuilder[ playerActor.playerIdentity ] = playerActor.bet()
    }
    _bets = Collections.unmodifiableMap( betsBuilder )
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
}

class Bet( playerIdentity : PlayerIdentity, expectedWinCount : Int )

/**
 * Describes a [PlayerActor] playing one [Card]. This is seen by every other [Player] since
 * there is no 'hidden' state.
 * TODO: add some useful precomputed states.
 */
class Decision( playerIdentity : PlayerIdentity, card : Card )



/**
 * @param game gives a reference to other [PlayerIdentity]s.
 * @param playerIdentity must exist in given [PublicGame].
 * @param initialHand
 */
class PlayerActor(
    val game : PublicGame,
    val playerIdentity : PlayerIdentity,
    initialHand : Set< Card >
) : Dumpable {
  val _hand : MutableSet< Card >

  init {
    _hand = HashSet( initialHand )
  }

  /**
   * Must be called before first call to [decide] or [decision].
   */
  fun bet() : Int {
    return 0
  }

  fun decide() : Decision {
    TODO()
  }

  fun decision( decision : Decision ) {
    TODO()
  }

  override fun dump( i : Int, appendable : Appendable ) {
    appendable
        .indent( i ).append( PlayerActor::class.simpleName ).append( '{' ).eol()
        .indentMore( i ).append( "Player: " ).append( playerIdentity.name ).eol()
        .indentMore( i ).append( "Hand: " ).append( ansiString( _hand ) ).eol()
        .indent( i ).append( '}' ).eol()
  }
}