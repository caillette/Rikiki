package io.github.caillette.rikiki.game

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.card.Suite

/**
 * Describes what everybody can see. [PlayerActor] can safely access to every member.
 * Values are read-only and guaranteed to not change while [FullGame] calls methods of
 * [PlayerActor].
 *
 * @param turnCount how many times every [PlayerActor] gets to decide. Must be 1 or greater.
 */
abstract class PublicGame(
    val playerIdentities : Set< PlayerIdentity >,
    val turnCount : Int
) {

  init {
    check( turnCount > 0 )
  }

  abstract val trump : Suite?

  /**
   * @return an immutable [List].
   */
  abstract val decisionsInThisTurn : List< Decision >

  fun cardsPlayedInThisTurn() : List< Card > {
    return decisionsInThisTurn.map { it.card }
  }

  /**
   * Zero-based index of current turn.
   * A turn is a sequence of calls to [PlayerActor.bid] (one call per [PlayerActor])
   * then a sequence of calls to [PlayerActor.decisionForCurrentTurn] (one call per
   * [PlayerActor] again).
   * The value of [turnIndex] is capped by [turnCount].
   */
  abstract val turnIndex : Int

  abstract val firstCard : Card?

  abstract val cardCount : Int

  /**
   * @return an immutable [Map] reflecting last bets.
   * @throws IllegalStateException if [PlayerActor.bid] was not called beforehand for everybody.
   */
  abstract val bids : Map< PlayerIdentity, Int >

  abstract val scores : Map< PlayerIdentity, Int >

  /**
   * @return an immutable [Map] reflecting the sum of the wins for past turns.
   */
  abstract val turnWins : Map< PlayerIdentity, Int >

  abstract val phase : Phase

  enum class Phase {
    NEW, BIDS_DONE, DECIDING, COMPLETE
  }

}

fun Set< PlayerIdentity >.strategyAppearance() : Map< Strategy.Factory, Int > {
  return this.groupingBy { it.strategyFactory }.eachCountTo( HashMap() ).toMap()
}
