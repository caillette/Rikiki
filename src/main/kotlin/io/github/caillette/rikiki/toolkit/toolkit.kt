package io.github.caillette.rikiki.toolkit

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap


// ===========
// Collections
// ===========

fun< K, V > newFilledMap( keys : Set< K >, value : V ) : ImmutableMap< K, V > {
  val builder : ImmutableMap.Builder< K, V > = ImmutableMap.builder()
  for( key in keys ) builder.put( key, value )
  return builder.build()
}

fun < T > ImmutableList< T >.append( element : T ) : ImmutableList< T > {
  return ImmutableList.builder< T >().addAll( this ).add( element ).build()
}

fun< K > ImmutableMap< K, Int >.addTo( key : K, increment : Int ) : ImmutableMap< K, Int > {
  val builder : ImmutableMap.Builder< K, Int > = ImmutableMap.builder()
  var found = false
  for( entry in entries ) {
    if( entry!!.key == key ) {
      found = true
      builder.put( entry.key, entry.value + increment )
    } else {
      builder.put( entry.key, entry.value )
    }
  }
  check( found, { "Key '$key' not found in $this" } )
  return builder.build()
}

fun< T, U > checkUnique( collection : Collection< T >, extractor : ( T ) -> U ) {
  val extractedValues : MutableSet< U > = HashSet()
  for( item in collection ) {
    val extracted = extractor.invoke( item )
    if( extractedValues.contains( extracted ) ) {
      throw IllegalArgumentException(
          "Unicity check failed: $item and $extracted considered equivalent" )
    } else {
      extractedValues.add( extracted )
    }
  }
}


// ====
// Roll
// ====

fun< T > rollFirst( iterable : Iterable< T >, element : T ) : Iterable< T > {
  return rollFirst( iterable, { e -> e == element } )
}

/**
 * Rearranges the sequence of elements produced by an [Iterator], preserving element ordering
 * (considering that first element is next to last) but making first element (in initial
 * [Iterable] ordering) that matched given predicate appear first in resulting [Iterable].
 * ````
 *  rollFirst( [ A B C D E ], { e -> e == B } ) => [ B C D E A ]
 * ````
 *
 * @param iterable iterated entierely but only once.
 * @return an [Iterable] backed by an immutable collection.
 * @throws NoSuchElementException if there was not matched element.
 */
fun< T > rollFirst( iterable : Iterable< T >, matcher : ( T ) -> Boolean ) : Iterable< T > {
  val firstSegment : MutableList< T > = ArrayList()
  val lastSegment : MutableList< T > = ArrayList()
  var foundMatch = false
  iterable.iterator().forEach {
    if( ! foundMatch && matcher.invoke( it ) ) {
      foundMatch = true
    }
    if( foundMatch ) {
      firstSegment.add( it )
    } else {
      lastSegment.add( it )
    }
  }
  if( ! foundMatch ) {
    throw NoSuchElementException( "No match for $matcher in $iterable" )
  }

  // We could save a copy but this would get messy.
  return ImmutableList.builder< T >().addAll( firstSegment ).addAll( lastSegment ).build()
}


// ==========
// Appendable
// ==========

fun Appendable.eol() : Appendable {
  append( "\n" )
  return this
}

fun Appendable.indent( indentCount : Int ) : Appendable {
  for( i in 0 until indentCount ) {
    append( "  " )
  }
  return this
}

fun Appendable.indentMore( indentCount : Int ) : Appendable {
  return indent( indentCount + 1 )
}


