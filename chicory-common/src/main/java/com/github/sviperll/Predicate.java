/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Predicate<T> implements Evaluatable<T> {
    public static <T> Predicate<T> and(List<Evaluatable<T>> predicates) {
        return new Predicate<>(new AndEvaluatable<>(predicates));
    }

    public static <T> Predicate<T> or(List<Evaluatable<T>> predicates) {
        return new Predicate<>(new OrEvaluatable<>(predicates));
    }

    private final Evaluatable<T> predicate;
    public Predicate(Evaluatable<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean evaluate(T t) {
        return predicate.evaluate(t);
    }

    public Function<T, Boolean> asFunction() {
        return new Function<>(new EvaluatableApplicable<>(predicate));
    }

    public Predicate<T> not() {
        return new Predicate<>(new NotEvaluatable<>(predicate));
    }

    public Predicate<T> and(Evaluatable<T> thatPredicate) {
        return new Predicate<>(new AndEvaluatable<>(Arrays.asList(predicate, thatPredicate)));
    }

    public Predicate<T> or(Evaluatable<T> thatPredicate) {
        return new Predicate<>(new OrEvaluatable<>(Arrays.asList(predicate, thatPredicate)));
    }

    private static class NotEvaluatable<T> implements Evaluatable<T> {
        private final Evaluatable<T> p;
        public NotEvaluatable(Evaluatable<T> p) {
            this.p = p;
        }

        @Override
        public boolean evaluate(T t) {
            return !p.evaluate(t);
        }
    }

    private static class AndEvaluatable<T> implements Evaluatable<T> {
        private final List<Evaluatable<T>> predicates;
        public AndEvaluatable(List<Evaluatable<T>> predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean evaluate(T t) {
            for (Evaluatable<T> predicate: predicates)
                if (!predicate.evaluate(t))
                    return false;
            return true;
        }
    }

    private static class OrEvaluatable<T> implements Evaluatable<T> {
        private final List<Evaluatable<T>> predicates;
        public OrEvaluatable(List<Evaluatable<T>> predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean evaluate(T t) {
            for (Evaluatable<T> predicate: predicates)
                if (predicate.evaluate(t))
                    return true;
            return false;
        }
    }

    private static class EvaluatableApplicable<T> implements Applicable<T, Boolean> {
        private final Evaluatable<T> predicate;
        public EvaluatableApplicable(Evaluatable<T> predicate) {
            this.predicate = predicate;
        }

        @Override
        public Boolean apply(T t) {
            return predicate.evaluate(t);
        }
    }
}
