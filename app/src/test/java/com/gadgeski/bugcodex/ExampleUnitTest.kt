package com.gadgeski.bugcodex

import org.junit.Assert.assertEquals
import org.junit.Test
// （そのまま利用/org.junit.Test）
// ★ Changed: ワイルドカードを個別 import に変更(org.junit.Assert.assertEquals)

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
}
