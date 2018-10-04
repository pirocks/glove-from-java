package io.github.pirocks

import org.junit.Test

class VeryBasicTest {

    /**
     * Basic test which checks that glove is run without failure.
     */
    @Test
    fun basicTest() {
        val veryBasicDataSet = arrayListOf(
                arrayListOf("a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c", "a")
                , arrayListOf("a", "b", "a", "c", "a", "b", "a", "c", "a", "b", "a", "c", "a", "b", "a", "c")
                , arrayListOf("a", "c", "b", "a", "a", "c", "b", "a", "a", "c", "b", "a", "a", "c", "b", "a")
                , arrayListOf("a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c", "a")
                , arrayListOf("a", "b", "a", "c", "a", "b", "a", "c", "a", "b", "a", "c", "a", "b", "a", "c")
                , arrayListOf("a", "c", "b", "a", "a", "c", "b", "a", "a", "c", "b", "a", "a", "c", "b", "a")
                , arrayListOf("a", "b", "a", "c", "a", "b", "a", "c", "a", "b", "a", "c", "a", "b", "a", "c")
                , arrayListOf("a", "a", "c", "b", "a", "a", "c", "b", "a", "a", "c", "b", "a", "a", "c", "b")
                , arrayListOf("a", "c", "a", "b", "a", "c", "a", "b", "a", "c", "a", "b", "a", "c", "a", "b")
                , arrayListOf("a", "b", "a", "c", "a", "b", "a", "c", "a", "b", "a", "c", "a", "b", "a", "c")
                , arrayListOf("a", "a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c")
                , arrayListOf("a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c", "a"))
        GloveModel.createInNewDirectory(veryBasicDataSet).runBlocking()

    }


}
