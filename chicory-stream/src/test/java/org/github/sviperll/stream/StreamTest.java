/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import com.github.sviperll.Applicable;
import com.github.sviperll.Consumer;
import com.github.sviperll.Evaluatable;
import com.github.sviperll.Function;
import com.github.sviperll.Predicate;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class StreamTest {
    private boolean isOpened = false;

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
        Stream<Integer> test = Stream.valueOf(new Streamable<Integer>() {
            @Override
            public void forEach(SaturableConsuming<? super Integer> consumer) {
                isOpened = true;
                consumer.accept(1);
                consumer.accept(2);
                consumer.accept(3);
                isOpened = false;
            }
        });
        assert(!isOpened);
        CloseableIterator<Integer> iterator = test.openIterator();
        assert(isOpened);
        assertEquals(1, iterator.next().intValue());
        assertEquals(2, iterator.next().intValue());
        assertEquals(3, iterator.next().intValue());
        assert(!isOpened); // Should close stream as soon as all elements are read
        iterator.close();
    }

    @Test
    public void testDrainingPart() throws IOException {
        Stream<Integer> test = Stream.valueOf(new Streamable<Integer>() {
            @Override
            public void forEach(SaturableConsuming<? super Integer> consumer) {
                isOpened = true;
                consumer.accept(1);
                consumer.accept(2);
                consumer.accept(3);
                isOpened = false;
            }
        });
        assert(!isOpened);
        CloseableIterator<Integer> iterator = test.openIterator();
        assert(isOpened);
        assertEquals(1, iterator.next().intValue());
        assertEquals(2, iterator.next().intValue());
        iterator.close();
        assert(!isOpened); // Should close stream as soon as requested
    }

    @Test
    public void testDrainingWithException() throws IOException {
        Stream<Integer> test = Stream.valueOf(new Streamable<Integer>() {
            @Override
            public void forEach(SaturableConsuming<? super Integer> consumer) {
                isOpened = true;
                try {
                    consumer.accept(1);
                    consumer.accept(2);
                    throw new ExpectedException();
                    // consumer.accept(3);
                } finally {
                    isOpened = false;
                }
            }
        });
        assert(!isOpened);
        CloseableIterator<Integer> iterator = test.openIterator();
        assert(isOpened);
        assertEquals(1, iterator.next().intValue());
        assertEquals(2, iterator.next().intValue());

        try {
            iterator.next();
            assert(false); // Should never be reached
        } catch (ExpectedException ex) {
            assert(true); // Exception should be thrown
        }
        assert(!isOpened); // Should close stream as soon as exception is thrown
        iterator.close();
    }

    @Test
    public void testBasicPipeline() throws IOException {
        Predicate<Integer> mod2 = Predicate.valueOf(new Evaluatable<Integer>() {
            @Override
            public boolean evaluate(Integer t) {
                return t % 2 == 0;
            }
        });

        Predicate<Integer> mod3 = Predicate.valueOf(new Evaluatable<Integer>() {
            @Override
            public boolean evaluate(Integer t) {
                return t % 3 == 0;
            }
        });

        Function<Integer, Integer> doubleIt = Function.valueOf(new Applicable<Integer, Integer>() {
            @Override
            public Integer apply(Integer t) {
                return t * 2;
            }
        });

        Stream<Integer> stream = Arrays.asStream(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        int sum = stream.collect(Collector.summingInt()).intValue();
        assertEquals(45, sum);

        int sum1 = stream.filter(mod2).collect(Collector.summingInt()).intValue();
        assertEquals(20, sum1);

        int sum2 = stream.filter(mod2).filter(mod3).collect(Collector.summingInt()).intValue();
        assertEquals(6, sum2);

        int sum3 = stream.map(doubleIt).collect(Collector.summingInt()).intValue();
        assertEquals(90, sum3);

        int sum4 = stream.map(doubleIt).map(doubleIt).collect(Collector.summingInt()).intValue();
        assertEquals(180, sum4);
    }

    private static class ExpectedException extends RuntimeException {
    }
}