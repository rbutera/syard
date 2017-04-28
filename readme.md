# Panda 2 Scotland Yard Assignment
[![Floobits Status](https://floobits.com/Raigasm/syard.svg)](https://floobits.com/Raigasm/syard/redirect)

## Reference
- [CW-MODEL](https://www.ole.bris.ac.uk/bbcswebdav/courses/COMS10001_2016/students/model/index.html)
- [CW-AI](https://www.ole.bris.ac.uk/bbcswebdav/courses/COMS10001_2016/students/ai/index.html)
- [Scotland Yard Issues and Resolutions](https://www.ole.bris.ac.uk/bbcswebdav/courses/COMS10001_2016/students/issues.html)
- [Scotland Yard Rules on Wikipedia](https://en.wikipedia.org/wiki/Scotland_Yard_(board_game))
## CW-MODEL
### Compilation
```
 ./mvnw clean compile
```

### Starting the GUI
```
mvnw clean compile exec:java
```

### Testing

- [AssertJ quick start](http://joel-costigliola.github.io/assertj/assertj-core-quick-start.html)

```
 ./mvnw clean test
```

#### Running Individual Tests

```
./mvnw -Dtest=ModelCreationTest test
```


```
./mvnw -Dtest=ModelCreationTest#testNullMapShouldThrow* test
```
