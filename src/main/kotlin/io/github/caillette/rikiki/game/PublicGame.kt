package io.github.caillette.rikiki.game

import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.card.Suite

/**
 * Describes what everybody can see. [PlayerActor] can safely access to every member.
 * Values are read-only and guaranteed to not change while [FullGame] calls methods of
 * [PlayerActor].
 *
 * Terminology based on [Oh Hell!](https://www.pagat.com/exact/ohhell.html) rules.
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

  abstract val trump : Suite?

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
  abstract val trickWins : Map< PlayerIdentity, Int >

  abstract val phase : Phase

  enum class Phase {
    NEW, BIDS_DONE, DECIDING, COMPLETE
  }

}