package io.github.caillette.rikiki.game

import com.google.common.collect.ImmutableSet
import io.github.caillette.rikiki.card.Card
import io.github.caillette.rikiki.card.Figure
import io.github.caillette.rikiki.card.Suite

data class PlayerIdentity( val name : String )

fun players( vararg names : String ) : Set< PlayerIdentity > {
  val builder : ImmutableSet.Builder< PlayerIdentity > = ImmutableSet.builder()
  for( name in names ) {
    builder.add( PlayerIdentity( name ) )
  }
  return builder.build()
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


fun best( decisions : List< Decision >, trump : Suite? ) : Decision {
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
    val trumpOnly = select( decisions, trump )
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


