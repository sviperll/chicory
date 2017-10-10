/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.stream;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class StreamTest {
    private volatile boolean isOpened = false;

    public StreamTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        isOpened = false;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDrainingAll() throws IOException {
        Stream<Integer> test = Stream.of((SaturableConsuming<? super Integer> consumer) -> {
            isOpened = true;
            consumer.accept(1);
            consumer.accept(2);
            consumer.accept(3);
            isOpened = false;
        });
        assert(!isOpened);
        CloseableIterator<Integer> iterator = test.openIterator();
        try {
            assert(isOpened);
            assertEquals(1L, iterator.next().longValue());
            assertEquals(2L, iterator.next().longValue());
            assertEquals(3L, iterator.next().longValue());
            assert(!isOpened);
        } finally {
            iterator.close();
        }
    }

    @Test
    public void testDrainingPart() throws IOException {
        Stream<Integer> test = Stream.of((SaturableConsuming<? super Integer> consumer) -> {
            isOpened = true;
            consumer.accept(1);
            consumer.accept(2);
            consumer.accept(3);
            consumer.accept(4);
            consumer.accept(5);
            isOpened = false;
        });
        assert(!isOpened);
        CloseableIterator<Integer> iterator = test.openIterator();
        try {
            assert(isOpened);
            assertEquals(1, iterator.next().longValue());
            assertEquals(2, iterator.next().longValue());
            assert(isOpened);
        } finally {
            iterator.close();
        }
        assert(!isOpened); // Should close stream as soon as requested
    }

    @Test
    public void testDrainingWithException() throws IOException {
        Stream<Integer> test = Stream.of((SaturableConsuming<? super Integer> consumer) -> {
            isOpened = true;
            try {
                consumer.accept(1);
                consumer.accept(2);
                throw new ExpectedException();
                // consumer.accept(3);
            } finally {
                isOpened = false;
            }
        });
        assert(!isOpened);
        CloseableIterator<Integer> iterator = test.openIterator();
        try {
            assert(isOpened);
            assertEquals(1, iterator.next().longValue());
            assertEquals(2, iterator.next().longValue());

            try {
                iterator.next();
                assert(false); // Should never be reached
            } catch (ExpectedException ex) {
                assert(true); // Exception should be thrown
                assert(!isOpened); // Should close stream as soon as exception is thrown
            }
        } finally {
            iterator.close();
        }
    }

    @Test
    public void testBasicPipeline() throws IOException {
        Predicate<Integer> mod2 = i -> i % 2 == 0;

        Predicate<Integer> mod3 = i -> i % 3 == 0;

        Function<Integer, Integer> doubleIt = i -> i * 2;

        Stream<Integer> stream = Arrays.asStream(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        int sum = stream.collect(Collector.summingInt());
        assertEquals(45, sum);

        int sum1 = stream.filter(mod2).collect(Collector.summingInt());
        assertEquals(20, sum1);

        int sum2 = stream.filter(mod2).filter(mod3).collect(Collector.summingInt());
        assertEquals(6, sum2);

        int sum3 = stream.map(doubleIt).collect(Collector.summingInt());
        assertEquals(90, sum3);

        int sum4 = stream.map(doubleIt).map(doubleIt).collect(Collector.summingInt());
        assertEquals(180, sum4);
    }

    @Test
    public void testStringJoin() throws IOException {
        Stream<String> stream = Stream.ofElements("a", "b", "c");
        assertEquals("abc", stream.collect(Collector.joiningStrings()));
        assertEquals("", Stream.<String>empty().collect(Collector.joiningStrings()));

        Collector<String, String> collector =
                Collector.joiningStrings(", ").finallyTransforming(optional -> optional.orElse("<empty>"));
        assertEquals("a, b, c", stream.collect(collector));
        assertEquals("<empty>", Stream.<String>empty().collect(collector));
    }

    @SuppressWarnings("serial")
    private static class ExpectedException extends RuntimeException {
    }
}