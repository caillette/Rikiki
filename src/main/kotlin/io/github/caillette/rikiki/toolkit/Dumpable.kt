package io.github.caillette.rikiki.toolkit

interface Dumpable {

  fun dump( i : Int, appendable : Appendable )

  fun dumpToConsole() {
    val builder = StringBuilder()
    dump( 0, builder )
    println( builder.toString() )
  }
}