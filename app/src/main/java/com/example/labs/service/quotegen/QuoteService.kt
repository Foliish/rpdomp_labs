package com.example.labs.service.quotegen

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteService {

    private val openAI = OpenAI(
        OpenAIConfig(
            token = "pza_ul95cGPkLL7WcUhKtbclUZk4d_FBwnnx",
            host = OpenAIHost("https://polza.ai/api/v1/")
        )
    )

    data class QuoteResult(val title: String, val text: String)

    suspend fun generateQuote(author: String, seed: Int): QuoteResult = withContext(Dispatchers.IO) {
        val moods = listOf("абсурдный", "философский", "агрессивно-мотивирующий", "саркастичный", "меланхоличный", "пацанский", "высокомерный")
        val topics = listOf("смысл бытия", "пельмени", "криптовалюту", "котиков", "успешный успех", "одиночество", "баги в коде", "завтрак")
        val styles = listOf("кратко", "очень заумно", "используя сленг", "в стиле японских хокку", "как будто это секретный совет", "метафорично")
        val twists = listOf("неожиданная концовка", "упоминание носков", "драматическая пауза", "вопрос к читателю", "ирония судьбы")

        val mood = moods[seed % moods.size]
        val topic = topics[(seed / 2) % topics.size]
        val style = styles[(seed / 3) % styles.size]
        val twist = twists[(seed / 5) % twists.size]

        val prompt = """
            Представь, что ты — $author. 
            Напиши одну уникальную цитату в следующем стиле:
            - Настроение: $mood
            - Тема: $topic
            - Стиль изложения: $style
            - Особенность: $twist
            
            Важно: Цитата должна быть в духе персонажа $author, но с учетом заданных параметров. 
            
            ОТВЕТЬ ТОЛЬКО В ФОРМАТЕ JSON. НЕ ПИШИ НИКАКОГО ТЕКСТА ДО И ПОСЛЕ JSON. 
    НЕ ИСПОЛЬЗУЙ СИМВОЛЫ ```json.
    
    Формат:
    {"title": "Название", "quote": "Текст цитаты"}
        """.trimIndent()

        val request = ChatCompletionRequest(
            model = ModelId("openai/gpt-5.4-nano"),
            messages = listOf(
                ChatMessage(role = ChatRole.System, content = "Ты — креативный генератор юмористических и философских цитат."),
                ChatMessage(role = ChatRole.User, content = prompt)
            ),
            temperature = 0.9
        )

        try {
            val response = openAI.chatCompletion(request)
            val content = response.choices.firstOrNull()?.message?.content ?: ""

            parseJson(content)
        } catch (e: Exception) {
            QuoteResult("Ошибка", "Цитата потерялась в пространстве: ${e.message}")
        }
    }

    private fun parseJson(jsonStr: String): QuoteResult {
        return try {
            // Очищаем от возможных Markdown-тегов
            val cleanInput = jsonStr.removeSurrounding("```json", "```").trim()

            // Регулярки, которые игнорируют лишние пробелы и переносы внутри JSON
            val titleRegex = """"title"\s*:\s*"([^"]+)"""".toRegex()
            val quoteRegex = """"quote"\s*:\s*"([^"]+)"""".toRegex()

            val titleMatch = titleRegex.find(cleanInput)?.groupValues?.get(1)
            val quoteMatch = quoteRegex.find(cleanInput)?.groupValues?.get(1)

            // .replace("\\n", "\n") превращает текстовый символ в реальный перенос строки
            val finalTitle = titleMatch?.replace("\\n", "\n")?.trim() ?: "Мудрость"
            val finalQuote = quoteMatch?.replace("\\n", "\n")?.trim() ?: cleanInput

            QuoteResult(finalTitle, finalQuote)
        } catch (e: Exception) {
            QuoteResult("Ошибка", "Не удалось обработать текст")
        }
    }
}