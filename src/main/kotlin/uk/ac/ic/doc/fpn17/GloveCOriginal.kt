package uk.ac.ic.doc.fpn17

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import java.io.File

class GloveCOriginal{
    fun runBlocking(): Word2Vec {

        return WordVectorSerializer.readWord2VecModel(File(workingDirectory.absolutePath, "vector_output.txt"))
    }
}