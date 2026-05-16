package com.bananchiki.wakeup.data.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bananchiki.wakeup.data.ai.TextGenerator
import com.bananchiki.wakeup.data.preferences.GreetingCacheManager

class GreetingsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    val prompt: String = "Напиши одно мотивирующее предложение на правильном и понятном русском языке, чтобы разбудить человека утром. Текст должен быть дружеским и смешным, будто друг пытается тебя разбудить, но текст должен быть без странных или выдуманных слов. Выдай только саму фразу, без кавычек и заголовков."

    override suspend fun doWork(): Result {
        return try {
            Log.d("MyWorker", "Отправляем запрос к ИИ.")
            val textGenerator = TextGenerator()
            val response = textGenerator.generateText(prompt)

            if (response.isNullOrBlank() || response.startsWith("Ошибка") ){
                Log.e("MyWorker", "ИИ вернул: $response. Пробуем позже.")
                Result.retry()
            } else {
                Log.d("MyWorker", "ИИ сгенерировал фразу: $response")
                val greetingCacheManager = GreetingCacheManager(applicationContext)
                greetingCacheManager.addGreeting(response)
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("MyWorker", "Критическая ошибка: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}