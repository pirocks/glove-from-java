package io.github.pirocks

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.util.HashSet
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock


/**
 * Not recommend that you use this constructor directly. @see <code> io.github.pirocks.GloveModel.Companion.createInNewDirectory </code>
 */
class GloveModel(
        val walksData: Collection<Collection<String>>,
        val memoryGB: Double = 4.0,
        val maxIter: Int = 50,
        val embeddingSize: Int = 50,
        val alpha: Double = 0.75,
        val windowSize: Int = 15,
        val vocabMinCount: Int = 5,
        val learningRate: Double = 0.05,
        val workingDirectory: String = File(".","/c-glove-from-java/single-run").absolutePath,
        val deleteOnCompletion: Boolean = false
) {

    companion object {
        val logger = LoggerFactory.getLogger(GloveModel::class.java)
        val fileSystemLock = ReentrantLock()

        /**
         * Extracts glove executables and writes data to disk in a new working directory.
         * @param data The sentence corpus to train with, represented as a collection of sentences.
         * @param memoryGB Maximum memory to use. Keep in mind that this memory is on top of any memory allocated to the jvm
         * @param maxIter Maximum iterations of GloVe to carry out
         * @param embeddingSize Size of desired embeddings
         * @param alpha Alpha in GloVe model, passed directly to glove executable
         * @param windowSize Window size to use
         * @param vocabMinCount Minimum occurences of a word needed for that word to be included in the model
         * @param learningRate Initial learning rate for model
         * @param baseDirectory The base directory to use for training. With every training run of a model, a new subdirectory of `baseDirectory` will be created. Data and executables will be copied into the subdirectory, and all glove output will be stored there.
         * @param deleteOnCompletion Delete the new working directory after the model has been trained via runBlocking.
         */
        @JvmStatic
        public fun createInNewDirectory(data: Collection<Collection<String>>,
                                        memoryGB: Double = 4.0,
                                        maxIter: Int = 50,
                                        embeddingSize: Int = 50,
                                        alpha: Double = 0.75,
                                        windowSize: Int = 15,
                                        vocabMinCount: Int = 5,
                                        learningRate: Double = 0.05,
                                        baseDirectory: String = File(/*System.getProperty("java.io.tmpdir")*/".","c-glove-from-java").absolutePath,
                                        deleteOnCompletion: Boolean = false ): GloveModel {
            var reTryName = 0
            var candidateWorkingDirectory = File(baseDirectory, """run$reTryName""")
            while (candidateWorkingDirectory.exists()) {
                reTryName++
                candidateWorkingDirectory = File(baseDirectory, """run$reTryName""")
            }
            val workingDirectory = candidateWorkingDirectory
            workingDirectory.mkdirs()
            logger.info("New working difrectory created:${workingDirectory.absolutePath}")

            return GloveModel(data,
                    memoryGB,
                    maxIter,
                    embeddingSize,
                    alpha,
                    windowSize,
                    vocabMinCount,
                    learningRate,
                    workingDirectory.path,
                    deleteOnCompletion)
        }
    }

    init {
        //Lock prevents two different model runs from using the same run directory, and interfering with each other. Does not protect against multiple processes.
        fileSystemLock.lock()
        logger.info("Initializing GloVe model with settings: memoryGB:$memoryGB maxIter:$maxIter embeddingSize:$embeddingSize alpha:$alpha windowSize:$windowSize vocabMinCount:$vocabMinCount learningRate:$learningRate workingDirectory:$workingDirectory")
        File(workingDirectory).mkdirs()
        extractToWorkingDirectory()
        val dataFile = File(workingDirectory, "GloVe/data.txt")
        logger.info("Writing data to ${dataFile.absolutePath}")
        dataFile.writeText("")// clear/create file
        fileSystemLock.unlock()
        val dataFileContents = walksData.stream().map {
            it.joinToString(separator = " " ,transform = {s -> s})
        }.forEach {
            dataFile.appendText(it + "\n")
        }//.toList().joinToString( separator = "\n", transform = {s -> s} )
//        dataFile.writeText(dataFileContents)
    }

    /**
     * Runs the model fitting process
     * @return A dl4j Word2Vec model, containing the vectors gennerated by GloVe.
     */
    public fun runBlocking(): Word2Vec {
        val pb = ProcessBuilder(File(workingDirectory, "GloVe/run.sh").path);
        pb.directory(File(workingDirectory,"GloVe"))
        pb.redirectError(ProcessBuilder.Redirect.INHERIT)
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        val p = pb.start()
        logger.info("Running GloVe...")
        p.waitFor()

        logger.info("Glove run complete, loading vectors into memory")
        val res = WordVectorSerializer.readWord2VecModel(File(workingDirectory, "GloVe/vector_output.txt"))
        if(deleteOnCompletion){
            File(workingDirectory).deleteRecursively()
        }
        return res
    }

    /**
     * Same as runBlocking, but asynchronous.
     */
    public fun runAsync(): Future<Word2Vec> {
        return Executors.newSingleThreadScheduledExecutor().submit(Callable { runBlocking() })
    }

    private fun extractToWorkingDirectory() {
        extractResource("GloVe/run.sh", mapOf(
                Pair("ALPHA=0.75", "ALPHA=$alpha"),
                Pair("MEMORY=4.0", "MEMORY=$memoryGB"),
                Pair("EMBEDDING_SIZE=50", "EMBEDDING_SIZE=$embeddingSize"),
                Pair("MAX_ITER=15", "MAX_ITER=$maxIter"),
                Pair("WINDOW_SIZE=15", "WINDOW_SIZE=$windowSize"),
                Pair("VOCAB_MIN_COUNT=5", "VOCAB_MIN_COUNT=$vocabMinCount"),
                Pair("LEARNING_RATE=0.05", "LEARNING_RATE=$learningRate")
        ))
        extractResource("GloVe/build/cooccur")
        extractResource("GloVe/build/glove")
        extractResource("GloVe/build/shuffle")
        extractResource("GloVe/build/vocab_count")
    }

    private fun extractResource(resource: String, replaceRules: Map<String, String> = emptyMap()) {
        logger.info("Extracting resource: $resource")
        val perms = HashSet<PosixFilePermission>()
        perms.add(PosixFilePermission.OWNER_READ)
        perms.add(PosixFilePermission.OWNER_WRITE)
        perms.add(PosixFilePermission.OWNER_EXECUTE)
        val `package` = "io/github/pirocks/"
        val resourceBytes = javaClass.classLoader.getResourceAsStream(`package` + resource).readBytes()
        val file = File(workingDirectory, resource)
        file.parentFile.mkdirs()
        if (replaceRules.isEmpty()) {
            file.writeBytes(resourceBytes)
        }else{
            var resourceContents = resourceBytes.toString(Charset.defaultCharset())
            replaceRules.entries.forEach { resourceContents = resourceContents.replace(it.key, it.value) }
            file.writeText(resourceContents)
        }
        Files.setPosixFilePermissions(file.toPath(), perms)
    }
}