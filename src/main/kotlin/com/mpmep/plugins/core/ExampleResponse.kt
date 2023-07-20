package com.mpmep.plugins.core

import kotlinx.serialization.Serializable

@Serializable
class ExampleResponse(
    val answer: Int = Int.MAX_VALUE,
    val isSkip : Boolean = false
)