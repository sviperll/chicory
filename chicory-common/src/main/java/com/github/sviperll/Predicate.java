/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

import java.util.List;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public abstract class Predicate<T> implements Evaluatable<T> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Predicate TRUE = new TruePredicate();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Predicate FALSE = new FalsePredicate();

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> truePredicate() {
        return (Predicate<T>)TRUE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> falsePredicate() {
        return (Predicate<T>)FALSE;
    }

    public static <T> Predicate<T> and(List<Evaluatable<? super T>> predicates) {
        Predicate<T> result = truePredicate();
        for (Evaluatable<? super T> e: predicates) {
            result = result.and(convert(e));
        }
        return result;
    }

    public static <T> Predicate<T> or(List<Evaluatable<? super T>> predicates) {
        Predicate<T> result = falsePredicate();
        for (Evaluatable<? super T> e: predicates) {
            result = result.or(convert(e));
        }
        return result;
    }

    public static <T> Predicate<T> valueOf(final Function<T, Boolean> function) {
        return valueOf(new Evaluatable<T>() {
            @Override
            public boolean evaluate(T t) {
                return function.apply(t);
            }
        });
    }

    public static <T> Predicate<T> valueOf(final Evaluatable<T> evaluatable) {
        if (evaluatable instanceof Predicate)
            return (Predicate<T>)evaluatable;
        else {
            return new SimplePredicate<>(evaluatable);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> convert(final Evaluatable<? super T> evaluatable) {
        if (evaluatable instanceof Predicate)
            return (Predicate<T>)evaluatable;
        else {
            return new SimplePredicate<>(evaluatable);
        }
    }

    private Predicate() {
    }

    public abstract Predicate<T> not();
    public abstract Predicate<T> and(Evaluatable<T> that);
    public abstract Predicate<T> or(Evaluatable<T> that);
    public Function<T, Boolean> asFunction() {
        return Function.valueOf(new Applicable<T, Boolean>() {
            @Override
            public Boolean apply(T t) {
                return Predicate.this.evaluate(t);
            }
        });
    }

    private static class SimplePredicate<T> extends Predicate<T> {
        private final Evaluatable<? super T> evaluatable;
        public SimplePredicate(Evaluatable<? super T> evaluatable) {
            this.evaluatable = evaluatable;
        }

        @Override
        public Predicate<T> not() {
            return new NotPredicate<>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<T> that) {
            return new OrPredicate<>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return evaluatable.evaluate(t);
        }
    }

    private static class TruePredicate<T> extends Predicate<T> {
        @Override
        public Predicate<T> not() {
            return new NotPredicate<>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<T> that) {
            return new OrPredicate<>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return true;
        }
    }

    private static class FalsePredicate<T> extends Predicate<T> {
        @Override
        public Predicate<T> not() {
            return new NotPredicate<>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<T> that) {
            return new OrPredicate<>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return false;
        }
    }

    private static class NotPredicate<T> extends Predicate<T> {
        private final Evaluatable<T> original;
        public NotPredicate(Evaluatable<T> original) {
            this.original = original;
        }

        @Override
        public Predicate<T> not() {
            return Predicate.valueOf(original);
        }

        @Override
        public Predicate<T> and(Evaluatable<T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<T> that) {
            return new OrPredicate<>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return !original.evaluate(t);
        }
    }

    private static class AndPredicate<T> extends Predicate<T> {
        private final Evaluatable<T> predicate1;
        private final Evaluatable<T> predicate2;
        public AndPredicate(Evaluatable<T> predicate1, Evaluatable<T> predicate2) {
            this.predicate1 = predicate1;
            this.predicate2 = predicate2;
        }

        @Override
        public Predicate<T> not() {
            return new NotPredicate<>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<T> predicate3) {
            return new AndPredicate<>(predicate1, valueOf(predicate2).and(predicate3));
        }

        @Override
        public Predicate<T> or(Evaluatable<T> that) {
            return new OrPredicate<>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return predicate1.evaluate(t) && predicate2.evaluate(t);
        }
    }

    private static class OrPredicate<T> extends Predicate<T> {
        private final Evaluatable<T> predicate1;
        private final Evaluatable<T> predicate2;
        public OrPredicate(Evaluatable<T> predicate1, Evaluatable<T> predicate2) {
            this.predicate1 = predicate1;
            this.predicate2 = predicate2;
        }

        @Override
        public Predicate<T> not() {
            return new NotPredicate<>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<T> predicate3) {
            return new OrPredicate<>(predicate1, valueOf(predicate2).or(predicate3));
        }

        @Override
        public boolean evaluate(T t) {
            return predicate1.evaluate(t) || predicate2.evaluate(t);
        }
    }
}
