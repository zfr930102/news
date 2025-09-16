package com.fr.news

import com.fr.news.model.service.RetrofitClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testToutiaoApi()= runBlocking {
        val api = RetrofitClient()
        val response = api.touTiaoApiService.getHotBoard()
        println(response.body())
        assertEquals(200, response.code())
    }
}