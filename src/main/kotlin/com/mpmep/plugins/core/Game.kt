package com.mpmep.plugins.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class Game(val examples : List<Example>){

    val userAnswers : MutableList<Boolean> = mutableListOf()
    private val _currentExample : MutableStateFlow<Int> = MutableStateFlow(0)

    val currentExample : Flow<Example> = _currentExample.map {
        examples[it]
    }

    fun checkAnswer(example: Example, answer : Int):Boolean{
        val result = example.result() == answer
        if (result) _currentExample.value ++
        userAnswers.add(true)
        return result
    }

    fun skip(){
        userAnswers.add(false)
        _currentExample.value++
    }
}

fun generateExample(level : Int = 1): Example {
    val negativeN = -(level * 10 / 2)
    val positiveN = (level * 10 / 2)
    val range = negativeN..positiveN
    val first = range.random()
    val second = range.random()
    val operate = if (second != 0)
        Operate.values().random()
    else
        listOf(Operate.MINUS, Operate.PLUS, Operate.MULTI).random()
    return Example(first, second, operate)
}
