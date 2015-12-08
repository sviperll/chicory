chicory-stream: stream processing library
=====================================

Stream processing like Java 8 streams, but works with Java 6 and have slightly different desing.

  - Chicory-stream provides sequential-only streams, no parallel support.
  - Chicory-stream allows easy forking and reuse of streams, unlike Java 8 where stream can be used only once.
  - Chicory-stream automatically and transparently opens and closes resources each time stream is used,
    unlike Java 8 where stream should be manually closed.

Part of [chicory](https://github.com/sviperll/chicory).

Installation
------------

Use maven dependency:

```xml
    <dependency>
        <groupId>com.github.sviperll</groupId>
        <artifactId>chicory-stream</artifactId>
        <version>0.31</version>
    </dependency>
```


Changelog
---------

Usage
-----

```java
class App {

    public static void main(String[] args)
    {
        StreamableFile userFile = new StreamableFile(new File(args[0]));
        Function<String, User> deserialize = Function.of(line -> /* ... */);
        Stream<User> source = userFile.text(Charsets.UTF8).lines().map(deserialize);

        Predicate<User> isActive = Predicate.of(user -> /* ... */);
        Collector<User, TreeMap<Month, Integer>, RuntimeException> toCountingMapPerMonth = Collector.toTreeMap(user -> user.registrationTime().month(), Collector.counting());
        System.out.println("ACTIVE:");

        // source stream opens required file and closes it on complition, right after line below.
        Map<Month, Integer> result = source.filter(isActive).collect(toCountingMapPerMonth);

        for (Map.Entry<Month, Integer> entry: result.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("NOT ACTIVE:");

        // Use same stream again. File is opened and closed once again.
        result = source.filter(isActive.not()).collect(toCountingMapPerMonth);

        for (Map.Entry<Month, Integer> entry: result.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("TOTAL:");

        // And again:
        result = source.collect(toCountingMapPerMonth);

        for (Map.Entry<Month, Integer> entry: result.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
```

[Javadoc](http://sviperll.github.io/chicory/chicory-stream/apidocs/index.html)
