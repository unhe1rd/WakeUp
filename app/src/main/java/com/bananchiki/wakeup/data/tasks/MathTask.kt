package com.bananchiki.wakeup.data.tasks

import kotlin.random.Random

data class MathTask(val question: String, val answer: Int, val cards: List<Int>) {
}

fun generateMathTask(): MathTask {
    val operators = listOf("+","-","*",":")
    val currentOperator = operators.random()

    var a = 0
    var b = 0
    var answer = 0
    val cardsSet = mutableSetOf<Int>()

    when (currentOperator){
        "+" -> {
            a = (10..99).random()
            b = (10..99).random()
            answer = a + b
            cardsSet.add(answer)
            while(cardsSet.size < 6){
                if (Random.nextBoolean()){
                    cardsSet.add(answer + (1..10).random())
                } else
                    cardsSet.add(answer - (1..10).random())
            }
        }

        "-" -> {
            a = (10..99).random()
            b = (10..99).random()
            if (a < b)
                a = b.also { b = a }
            answer = a - b
            cardsSet.add(answer)
            while(cardsSet.size < 6){
                if (Random.nextBoolean()){
                    cardsSet.add(answer + (1..10).random())
                } else
                    cardsSet.add(answer - (1..10).random())
            }
        }

        "*" -> {
            a = (1..9).random()
            b = (2..9).random()
            answer = a * b
            cardsSet.add(answer)
            while(cardsSet.size < 6){
                if (Random.nextBoolean()){
                    cardsSet.add(answer + (1..10).random())
                } else
                    cardsSet.add(answer - (1..10).random())
            }
        }

        ":" -> {
            b = (2..9).random()
            answer = (2..81).random()
            a = b * answer
            cardsSet.add(answer)
            while(cardsSet.size < 6){
                if (Random.nextBoolean()){
                    cardsSet.add(answer + (1..10).random())
                } else
                    cardsSet.add(answer - (1..10).random())
            }
        }

    }

    val cardsList = cardsSet.toMutableList()
    cardsList.shuffle()

    val question = "Сколько будет $a $currentOperator $b?"



    return MathTask(question, answer, cardsList)
}