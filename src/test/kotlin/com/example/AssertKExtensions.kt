package com.example

import assertk.Assert
import assertk.assertions.support.expected

fun Assert<String?>.contains(expected: CharSequence) = given { actual ->
    if (null == actual) expected("$actual does not contain $expected")
    if (actual.contains(expected)) return

    expected("$actual does not contain $expected")
}
