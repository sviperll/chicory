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

    public static <T> Predicate<T> and(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
        Predicate<T> predicate = of(predicate1);
        return predicate.and(predicate2);
    }

    public static <T> Predicate<T> or(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
        Predicate<T> predicate = of(predicate1);
        return predicate.or(predicate2);
    }

    public static <T> Predicate<T> and(List<? extends Evaluatable<? super T>> predicates) {
        Predicate<T> result = truePredicate();
        for (Evaluatable<? super T> e: predicates) {
            result = Predicate.and(result, e);
        }
        return result;
    }

    public static <T> Predicate<T> or(List<? extends Evaluatable<? super T>> predicates) {
        Predicate<T> result = falsePredicate();
        for (Evaluatable<? super T> e: predicates) {
            result = Predicate.or(result, e);
        }
        return result;
    }

    public static <T> Predicate<T> of(final Function<? super T, Boolean> function) {
        return of(new Evaluatable<T>() {
            @Override
            public boolean evaluate(T t) {
                return function.apply(t);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> of(final Evaluatable<? super T> evaluatable) {
        if (evaluatable instanceof Predicate)
            // Predicate is covariant in it's type argument,
            // so this cast is safe!
            return (Predicate<T>)evaluatable;
        else {
            return new SimplePredicate<>(evaluatable);
        }
    }

    private Predicate() {
    }

    public abstract Predicate<T> not();
    public abstract Predicate<T> and(Evaluatable<? super T> that);
    public abstract Predicate<T> or(Evaluatable<? super T> that);
    public Function<T, Boolean> asFunction() {
        return Function.of(new Applicable<T, Boolean>() {
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
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
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
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
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
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
            return new OrPredicate<>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return false;
        }
    }

    private static class NotPredicate<T> extends Predicate<T> {
        private final Evaluatable<? super T> original;
        public NotPredicate(Evaluatable<? super T> original) {
            this.original = original;
        }

        @Override
        public Predicate<T> not() {
            return Predicate.of(original);
        }

        @Override
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
            return new OrPredicate<>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return !original.evaluate(t);
        }
    }

    private static class AndPredicate<T> extends Predicate<T> {
        private final Evaluatable<? super T> predicate1;
        private final Evaluatable<? super T> predicate2;
        public AndPredicate(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
            this.predicate1 = predicate1;
            this.predicate2 = predicate2;
        }

        @Override
        public Predicate<T> not() {
            return new NotPredicate<>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<? super T> predicate3) {
            return new AndPredicate<>(predicate1, of(predicate2).and(predicate3));
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
            return new OrPredicate<>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return predicate1.evaluate(t) && predicate2.evaluate(t);
        }
    }

    private static class OrPredicate<T> extends Predicate<T> {
        private final Evaluatable<? super T> predicate1;
        private final Evaluatable<? super T> predicate2;
        public OrPredicate(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
            this.predicate1 = predicate1;
            this.predicate2 = predicate2;
        }

        @Override
        public Predicate<T> not() {
            return new NotPredicate<>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> predicate3) {
            return new OrPredicate<>(predicate1, of(predicate2).or(predicate3));
        }

        @Override
        public boolean evaluate(T t) {
            return predicate1.evaluate(t) || predicate2.evaluate(t);
        }
    }
}
