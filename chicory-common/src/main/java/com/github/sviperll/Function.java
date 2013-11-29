/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Function<T, R> implements Applicable<T, R> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Function ID = new Function(new IdApplicable());

    public static <T> Function<T, T> id() {
        return typedIdentity();
    }

    @SuppressWarnings({"unchecked"})
    public static <R, T extends R> Function<T, R> typedIdentity() {
        return (Function<T, R>)ID;
    }

    private final Applicable<T, R> function;
    public Function(Applicable<T, R> function) {
        this.function = function;
    }

    @Override
    public R apply(T t) {
        return function.apply(t);
    }

    public <U> Function<U, R> composeWith(Applicable<U, T> thatFunction) {
        return new Function<>(new ComposedApplicable<>(this.function, thatFunction));
    }

    public <U> Function<T, U> andThen(Applicable<R, U> thatFunction) {
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
        private final Applicable<T, R> function;
        public PassingNullThroughApplicable(Applicable<T, R> function) {
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
        private final Applicable<T, R> function;
        public ThrowingOnNullApplicable(Applicable<T, R> function) {
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
