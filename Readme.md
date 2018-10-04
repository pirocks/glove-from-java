This project allows you to easily call the original, reference implementation of [GloVe](https://github.com/stanfordnlp/GloVe) from a Java/JVM language. Word vectors will be returned in [DL4J](https://deeplearning4j.org/) format. Created because of bugs in DL4J, which made their implementation of GloVe unusable.

### Dependencies

You will need to have a [ND4J](https://deeplearning4j.org/docs/latest/nd4j-overview) backend in your classpath. 


### Usage

```kotlin
//call like so:
fun main(){
    val wordVectors = GloveModel.createInNewDirectory(dataset, 
                    memoryGB = 4.0, 
                    maxIter = 50,
                    embeddingSize = 50,
                    alpha = 0.75,
                    windowSize = 15,
                    vocabMinCount = 5,
                    learningRate = 0.05).runBlocking()
}

```

```java
//call like so:
class Demo{
    public static void main(String[] args){
        GloveModel.createInNewDirectory(dataset, 4.0, 50,50,0.75,15,5,0.05).runBlocking();
    }
}


```

### Contributing

Feel free to submit issues, feature requests and/or pull requests.


### License

Both GloVe and this project are licenced under the Apache 2.0 License