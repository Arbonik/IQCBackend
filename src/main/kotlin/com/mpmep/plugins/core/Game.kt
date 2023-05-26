package com.mpmep.plugins.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Game(
    val examples : List<Example>,
    val coroutineScope: CoroutineScope
){

    private val userAnswers : MutableList<Boolean> = mutableListOf()

    private val _currentExample : MutableStateFlow<Int> = MutableStateFlow(0)

    val currentExample : StateFlow<Example> = _currentExample.map {
        examples[it]
    }.stateIn(coroutineScope, SharingStarted.Lazily, examples.first())

    val userMisstake : MutableSharedFlow<String> = MutableSharedFlow()
    fun checkAnswer(answer : Int){
        val result = examples[_currentExample.value].result() == answer
        if (result)
            _currentExample.value ++
        else
            coroutineScope.launch {
                userMisstake.emit("Неверно")
            }
        userAnswers.add(result)
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
