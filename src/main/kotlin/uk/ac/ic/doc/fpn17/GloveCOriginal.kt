package uk.ac.ic.doc.fpn17

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import java.io.File
import java.nio.charset.Charset

class GloveCOriginal(
        val data: Collection<Collection<String>>,
        val memoryGB: Double = 4.0,
        val maxIter: Int = 50,
        val embeddingSize: Int = 50,
        val alpha: Double = 0.75,
        val windowSize: Int = 15,
        val vocabMinCount: Int = 5,
        val workingDirectory: String = "/mnt/harddrive1/cglove-test/"//System.getProperty("java.io.tmpdir") + "/working"
) {
    companion object {
        fun createInNewDirectory(data: Collection<Collection<String>>,
                                 memoryGB: Double = 4.0,
                                 maxIter: Int = 50,
                                 embeddingSize: Int = 50,
                                 alpha: Double = 0.75,
                                 windowSize: Int = 15,
                                 vocabMinCount: Int = 5): GloveCOriginal {
            val directoryAppendNameBase = "/mnt/harddrive1/cglove-original/"
            var reTryName = 0
            var candidateWorkingDirectory = File(directoryAppendNameBase, """run$reTryName""")
            while (candidateWorkingDirectory.exists()) {
                reTryName++
                candidateWorkingDirectory = File(directoryAppendNameBase, """run$reTryName""")
            }
            val workingDirectory = candidateWorkingDirectory
            workingDirectory.mkdirs()

            return GloveCOriginal(data,
                    memoryGB,
                    maxIter,
                    embeddingSize,
                    alpha,
                    windowSize,
                    vocabMinCount, workingDirectory.path)
        }
    }

    init {
        File(workingDirectory).mkdirs()
        extractToWorkingDirectory()
        val vocab = mutableMapOf<String, Int>()
        val dataFileContents = data.parallelStream().map {
            it.reduce { acc, s ->
                "$acc $s"
            }
        }.map { it + "\n" }.reduce { acc, s -> "$acc$s" }
        File(workingDirectory, "data.txt").writeText(dataFileContents.orElseThrow { IllegalArgumentException("Something was wrong with the data provided. Make sure there are no empty collections inputted.") })
    }

    fun runBlocking(): Word2Vec {
        val pb = ProcessBuilder(File(workingDirectory, "run.sh").path);
        pb.directory(File(workingDirectory))
        pb.redirectError(ProcessBuilder.Redirect.INHERIT)
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        val p = pb.start();
        p.waitFor()

        return WordVectorSerializer.readWord2VecModel(File(workingDirectory, "vector_output.txt"))
    }

    fun extractToWorkingDirectory() {
        extractResource("run.sh", mapOf(
                Pair("ALPHA=0.75", "ALPHA=$alpha"),
                Pair("MEMORY=4.0", "MEMORY=$memoryGB"),
                Pair("EMBEDDING_SIZE=50", "EMBEDDING_SIZE=$embeddingSize"),
                Pair("MAX_ITER=15", "MAX_ITER=$maxIter"),
                Pair("WINDOW_SIZE=15", "WINDOW_SIZE=$windowSize"),
                Pair("VOCAB_MIN_COUNT=5", "VOCAB_MIN_COUNT=$vocabMinCount")
        ))
        extractResource("GloVe/build/cooccur")
        extractResource("GloVe/build/glove")
        extractResource("GloVe/build/shuffle")
        extractResource("GloVe/build/vocab_count")
    }

    fun extractResource(resource: String, replaceRules: Map<String, String> = emptyMap()) {
        val `package` = "uk/ac/ic/doc/fpn17/"
        val resourceBytes = javaClass.classLoader.getResourceAsStream(`package` + resource).readBytes()
        val file = File(workingDirectory, resource)
        file.parentFile.mkdirs()
        if (replaceRules.isEmpty()) {
            file.writeBytes(resourceBytes)
            return
        }
        var resourceContents = resourceBytes.toString(Charset.defaultCharset())
        replaceRules.entries.forEach { resourceContents = resourceContents.replace(it.key, it.value) }
        file.writeText(resourceContents)
    }
}

fun main(args: Array<String>) {
    GloveCOriginal.createInNewDirectory(arrayListOf(
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
            , arrayListOf("a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c", "a", "a", "b", "c", "a"))).runBlocking()
}