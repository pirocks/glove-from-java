This project allows you to easily call the original, reference implementation of GloVe from a Java/JVM language. Word vectors will be returned in DL4J format. 

### Dependencies

You will need to have a ND4J backend in your classpath. 


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


### License

Both GloVe and this are licenced under the Apache 2.0 License