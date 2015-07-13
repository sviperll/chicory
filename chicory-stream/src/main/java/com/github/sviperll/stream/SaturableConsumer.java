/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.stream;

import com.github.sviperll.Applicable;
import com.github.sviperll.Evaluatable;
import com.github.sviperll.Function;
import com.github.sviperll.Predicate;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
abstract class SaturableConsumer<T> implements SaturableConsuming<T> {
    public static <T> SaturableConsumer<T> of(final SaturableConsuming<T> consuming) {
        if (consuming instanceof SaturableConsumer)
            return (SaturableConsumer<T>)consuming;
        else
            return new SimpleSaturableConsumer<T>(consuming);
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
            return new FilteringSaturableConsumer<T>(this, predicate);
        }

        @Override
        SaturableConsumer<T> limiting(int limit) {
            return new LimitingSaturableConsumer<T>(this, limit);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
            return new MappedSaturableConsumer<T, U>(this, function);
        }

        @Override
        SaturableConsumer<T> skipping(int offset) {
            return new SkippingSaturableConsumer<T>(this, offset);
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
            return new FilteringSaturableConsumer<T>(original, Predicate.and(predicate1, predicate2));
        }

        @Override
        SaturableConsumer<T> limiting(int limit) {
            return new LimitingSaturableConsumer<T>(this, limit);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
            return new MappedSaturableConsumer<T, U>(this, function);
        }

        @Override
        SaturableConsumer<T> skipping(int offset) {
            return new SkippingSaturableConsumer<T>(this, offset);
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
            return new FilteringSaturableConsumer<T>(this, predicate);
        }

        @Override
        SaturableConsumer<T> limiting(int limit2) {
            if (limit2 >= limit1)
                return this;
            else
                return new LimitingSaturableConsumer<T>(original, limit2);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
            return new MappedSaturableConsumer<T, U>(this, function);
        }

        @Override
        SaturableConsumer<T> skipping(int offset) {
            return new SkippingSaturableConsumer<T>(this, offset);
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
            return new FilteringSaturableConsumer<T>(this, predicate);
        }

        @Override
        SaturableConsumer<T> limiting(int limit) {
            return new LimitingSaturableConsumer<T>(this, limit);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function2) {
            return new MappedSaturableConsumer<V, U>(original, Function.of(function1).composeWith(function2));
        }

        @Override
        SaturableConsumer<T> skipping(int offset) {
            return new SkippingSaturableConsumer<T>(this, offset);
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
            return new FilteringSaturableConsumer<T>(this, predicate);
        }

        @Override
        SaturableConsumer<T> limiting(int limit) {
            return new LimitingSaturableConsumer<T>(this, limit);
        }

        @Override
        <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
            return new MappedSaturableConsumer<T, U>(this, function);
        }

        @Override
        SaturableConsumer<T> skipping(int offset2) {
            return new SkippingSaturableConsumer<T>(original, offset1 + offset2);
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
