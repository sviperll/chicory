/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import com.github.sviperll.Applicable;
import com.github.sviperll.Evaluatable;
import com.github.sviperll.Function;
import com.github.sviperll.Predicate;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
abstract class SaturableConsumer<T> implements SaturableConsuming<T> {
    public static <T> SaturableConsumer<T> valueOf(final SaturableConsuming<T> consuming) {
        if (consuming instanceof SaturableConsumer)
            return (SaturableConsumer<T>)consuming;
        else
            return new SimpleSaturableConsumer<>(consuming);
    }

    abstract SaturableConsumer<T> filtering(Evaluatable<? super T> predicate);
    abstract SaturableConsumer<T> limiting(int limit);
    abstract <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function);
    abstract SaturableConsumer<T> skipping(int offset);

    private SaturableConsumer() {
    }

    private static class SimpleSaturableConsumer<T> extends SaturableConsumer<T> {
        private final SaturableConsuming<T> consuming;
        public SimpleSaturableConsumer(SaturableConsuming<T> consuming) {
            this.consuming = consuming;
        }

        @Override
        SaturableConsumer<T> filtering(Evaluatable<? super T> predicate) {
            return new FilteringSaturableConsumer<>(this, predicate);
        }

        @Override
        SaturableConsumer<T> limiting(int limit) {
            return new LimitingSaturableConsumer<>(this, limit);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
            return new MappedSaturableConsumer<>(this, function);
        }

        @Override
        SaturableConsumer<T> skipping(int offset) {
            return new SkippingSaturableConsumer<>(this, offset);
        }

        @Override
        public void accept(T value) {
            consuming.accept(value);
        }

        @Override
        public boolean needsMore() {
            return consuming.needsMore();
        }
    }

    private static class FilteringSaturableConsumer<T> extends SaturableConsumer<T> {
        private final SaturableConsuming<T> original;
        private final Evaluatable<? super T> predicate1;
        public FilteringSaturableConsumer(SaturableConsuming<T> original, Evaluatable<? super T> predicate1) {
            this.original = original;
            this.predicate1 = predicate1;
        }

        @Override
        SaturableConsumer<T> filtering(Evaluatable<? super T> predicate2) {
            return new FilteringSaturableConsumer<>(original, Predicate.and(predicate1, predicate2));
        }

        @Override
        SaturableConsumer<T> limiting(int limit) {
            return new LimitingSaturableConsumer<>(this, limit);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
            return new MappedSaturableConsumer<>(this, function);
        }

        @Override
        SaturableConsumer<T> skipping(int offset) {
            return new SkippingSaturableConsumer<>(this, offset);
        }

        @Override
        public void accept(T value) {
            if (predicate1.evaluate(value))
                original.accept(value);
        }

        @Override
        public boolean needsMore() {
            return original.needsMore();
        }
    }

    private static class LimitingSaturableConsumer<T> extends SaturableConsumer<T> {
        private final SaturableConsuming<T> original;
        private final int limit1;
        private int count = 0;

        public LimitingSaturableConsumer(SaturableConsuming<T> original, int limit1) {
            this.original = original;
            this.limit1 = limit1;
        }

        @Override
        SaturableConsumer<T> filtering(Evaluatable<? super T> predicate) {
            return new FilteringSaturableConsumer<>(this, predicate);
        }

        @Override
        SaturableConsumer<T> limiting(int limit2) {
            if (limit2 >= limit1)
                return this;
            else
                return new LimitingSaturableConsumer<>(original, limit2);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
            return new MappedSaturableConsumer<>(this, function);
        }

        @Override
        SaturableConsumer<T> skipping(int offset) {
            return new SkippingSaturableConsumer<>(this, offset);
        }

        @Override
        public void accept(T value) {
            if (count < limit1)
                original.accept(value);
            count++;
        }

        @Override
        public boolean needsMore() {
            return count < limit1 && original.needsMore();
        }
    }

    private static class MappedSaturableConsumer<V, T> extends SaturableConsumer<T> {
        private final SaturableConsuming<V> original;
        private final Applicable<T, ? extends V> function1;
        public MappedSaturableConsumer(SaturableConsuming<V> original, Applicable<T, ? extends V> function1) {
            this.original = original;
            this.function1 = function1;
        }

        @Override
        SaturableConsumer<T> filtering(Evaluatable<? super T> predicate) {
            return new FilteringSaturableConsumer<>(this, predicate);
        }

        @Override
        SaturableConsumer<T> limiting(int limit) {
            return new LimitingSaturableConsumer<>(this, limit);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function2) {
            return new MappedSaturableConsumer<>(original, Function.valueOf(function1).composeWith(function2));
        }

        @Override
        SaturableConsumer<T> skipping(int offset) {
            return new SkippingSaturableConsumer<>(this, offset);
        }

        @Override
        public void accept(T value) {
            original.accept(function1.apply(value));
        }

        @Override
        public boolean needsMore() {
            return original.needsMore();
        }
    }

    private static class SkippingSaturableConsumer<T> extends SaturableConsumer<T> {
        private final SaturableConsuming<T> original;
        private final int offset1;
        private int count = 0;
        public SkippingSaturableConsumer(SaturableConsuming<T> orignal, int offset1) {
            this.original = orignal;
            this.offset1 = offset1;
        }

        @Override
        SaturableConsumer<T> filtering(Evaluatable<? super T> predicate) {
            return new FilteringSaturableConsumer<>(this, predicate);
        }

        @Override
        SaturableConsumer<T> limiting(int limit) {
            return new LimitingSaturableConsumer<>(this, limit);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
            return new MappedSaturableConsumer<>(this, function);
        }

        @Override
        SaturableConsumer<T> skipping(int offset2) {
            return new SkippingSaturableConsumer<>(original, offset1 + offset2);
        }

        @Override
        public void accept(T value) {
            if (count >= offset1)
                original.accept(value);
            count++;
        }

        @Override
        public boolean needsMore() {
            return count < offset1 || original.needsMore();
        }
    }
}
