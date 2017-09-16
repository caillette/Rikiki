package io.github.caillette.rikiki.toolkit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ToolkitTest {

  @Test
  fun checkUniqueFails() {
    val numbers = setOf< Number >( 1, 1f )
    assertThrows( IllegalArgumentException::class.java, {
      checkUnique( numbers, { n -> BigDecimal( if( n is Int ) "" + n + ".0" else "" + n ) } )
    } )
  }

  @Test
  fun checkUniqueSucceeds() {
    val numbers = setOf< Number >( 1, 1f )
    checkUnique( numbers, { n -> BigDecimal( "" + n ) } )
  }


  @Test
  fun rollFirst() {
    assertThrows( NoSuchElementException::class.java, { rollFirst( listOf(), 'A' ) } )
    assertEquals( rollFirst( listOf( 'A' ), 'A' ), listOf( 'A' ) )
    assertEquals( rollFirst( listOf( 'A', 'B', 'C' ), 'A' ), listOf( 'A', 'B', 'C' ) )
    assertEquals( rollFirst( listOf( 'A', 'B', 'C' ), 'B' ), listOf( 'B', 'C', 'A' ) )
    assertEquals( rollFirst( listOf( 'A', 'B', 'C' ), 'C' ), listOf( 'C', 'A', 'B' ) )
    assertEquals( rollFirst( listOf( 'A', 'B', 'A' ), 'A' ), listOf( 'A', 'B', 'A' ) )
    assertEquals( rollFirst( listOf( 'A', 'A', 'B' ), 'A' ), listOf( 'A', 'A', 'B' ) )
  }
}



