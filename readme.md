# Panda 2 Scotland Yard Assignment

- [https://www.ole.bris.ac.uk/bbcswebdav/courses/COMS10001_2016/students/model/index.html](https://www.ole.bris.ac.uk/bbcswebdav/courses/COMS10001_2016/students/model/index.html)
- https://www.ole.bris.ac.uk/bbcswebdav/courses/COMS10001_2016/students/issues.html

## Compilation
```
 ./mvnw clean compile
```

## Starting the GUI
```
mvnw clean compile exec:java
```

## Testing

- [AssertJ quick start](http://joel-costigliola.github.io/assertj/assertj-core-quick-start.html)

```
 ./mvnw clean test
```

### Running Individual Tests

```
./mvnw -Dtest=ModelCreationTest test
```

##
```
./mvnw -Dtest=ModelCreationTest#testNullMapShouldThrow* test
```
