/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import com.github.sviperll.Applicable;
import com.github.sviperll.Evaluatable;
import com.github.sviperll.Function;
import com.github.sviperll.Predicate;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
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
        Stream<Integer> test = Stream.of(new Streamable<Integer>() {
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
        try {
            assert(isOpened);
            assertEquals(1, iterator.next().intValue());
            assertEquals(2, iterator.next().intValue());
            assertEquals(3, iterator.next().intValue());
            assert(!isOpened);
        } finally {
            iterator.close();
        }
    }

    @Test
    public void testDrainingPart() throws IOException {
        Stream<Integer> test = Stream.of(new Streamable<Integer>() {
            @Override
            public void forEach(SaturableConsuming<? super Integer> consumer) {
                isOpened = true;
                consumer.accept(1);
                consumer.accept(2);
                consumer.accept(3);
                consumer.accept(4);
                consumer.accept(5);
                isOpened = false;
            }
        });
        assert(!isOpened);
        CloseableIterator<Integer> iterator = test.openIterator();
        try {
            assert(isOpened);
            assertEquals(1, iterator.next().intValue());
            assertEquals(2, iterator.next().intValue());
            assert(isOpened);
        } finally {
            iterator.close();
        }
        assert(!isOpened); // Should close stream as soon as requested
    }

    @Test
    public void testDrainingWithException() throws IOException {
        Stream<Integer> test = Stream.of(new Streamable<Integer>() {
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
        try {
            assert(isOpened);
            assertEquals(1, iterator.next().intValue());
            assertEquals(2, iterator.next().intValue());

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
        Predicate<Integer> mod2 = Predicate.of(new IsEven());

        Predicate<Integer> mod3 = Predicate.of(new IsMultipleOf3());

        Function<Integer, Integer> doubleIt = Function.of(new DoubleIt());

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

    @SuppressWarnings("serial")
    private static class ExpectedException extends RuntimeException {
    }

    private static class IsEven implements Evaluatable<Integer> {

        public IsEven() {
        }

        @Override
        public boolean evaluate(Integer t) {
            return t % 2 == 0;
        }
    }

    private static class IsMultipleOf3 implements Evaluatable<Integer> {

        public IsMultipleOf3() {
        }

        @Override
        public boolean evaluate(Integer t) {
            return t % 3 == 0;
        }
    }

    private static class DoubleIt implements Applicable<Integer, Integer> {

        public DoubleIt() {
        }

        @Override
        public Integer apply(Integer t) {
            return t * 2;
        }
    }
}