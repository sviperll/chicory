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
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Function<T, R> implements Applicable<T, R> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Function ID = new Function(new IdApplicable());

    public static <T> Function<T, T> identity() {
        return typedIdentity();
    }

    @SuppressWarnings({"unchecked"})
    public static <R, T extends R> Function<T, R> typedIdentity() {
        return (Function<T, R>)ID;
    }

    @SuppressWarnings({"unchecked"})
    public static <T, R> Function<T, R> of(Applicable<? super T, R> function) {
        if (function instanceof Function) {
            // Applicable is covariant in it's first argument
            // so this cast is safe
            return (Function<T, R>)function;
        } else
            return new Function<>(function);
    }

    private final Applicable<? super T, R> function;
    private Function(Applicable<? super T, R> function) {
        this.function = function;
    }

    @Override
    public R apply(T t) {
        return function.apply(t);
    }

    public <U> Function<U, R> composeWith(Applicable<U, ? extends T> thatFunction) {
        return new Function<>(new ComposedApplicable<>(this.function, thatFunction));
    }

    public <U> Function<T, U> andThen(Applicable<? super R, U> thatFunction) {
        return new Function<>(new ComposedApplicable<>(thatFunction, this.function));
    }

    public Function<T, R> passNullThrough() {
        return new Function<>(new PassingNullThroughApplicable<>(function));
    }

    public Function<T, R> throwOnNull() {
        return new Function<>(new ThrowingOnNullApplicable<>(function));
    }

    private static class IdApplicable<R, T extends R> implements Applicable<T, R> {
        @Override
        public R apply(T t) {
            return t;
        }
    }

    private static class ComposedApplicable<T, Q, R> implements Applicable<T, R> {
        private final Applicable<? super Q, R> f;
        private final Applicable<? super T, Q> g;
        public ComposedApplicable(Applicable<? super Q, R> f, Applicable<? super T, Q> g) {
            this.f = f;
            this.g = g;
        }

        @Override
        public R apply(T t) {
            return f.apply(g.apply(t));
        }
    }

    private static class PassingNullThroughApplicable<T, R> implements Applicable<T, R> {
        private final Applicable<? super T, R> function;
        public PassingNullThroughApplicable(Applicable<? super T, R> function) {
            this.function = function;
        }

        @Override
        public R apply(T t) {
            if (t == null)
                return null;
            else
                return function.apply(t);
        }
    }

    private static class ThrowingOnNullApplicable<T, R> implements Applicable<T, R> {
        private final Applicable<? super T, R> function;
        public ThrowingOnNullApplicable(Applicable<? super T, R> function) {
            this.function = function;
        }

        @Override
        public R apply(T t) {
            if (t == null)
                throw new NullPointerException();
            else
                return function.apply(t);
        }
    }
}
