package io.github.caillette.rikiki.toolkit

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
}



