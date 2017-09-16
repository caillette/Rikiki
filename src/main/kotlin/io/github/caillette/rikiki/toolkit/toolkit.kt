package io.github.caillette.rikiki.toolkit

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap

fun < T > ImmutableList< T >.append( element : T ) : ImmutableList< T > {
  return ImmutableList.builder< T >().addAll( this ).add( element ).build()
}

fun< K, V > newFilledMap( keys : Set< K >, value : V ) : ImmutableMap< K, V > {
  val builder : ImmutableMap.Builder< K, V > = ImmutableMap.builder()
  for( key in keys ) builder.put( key, value )
  return builder.build()
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
