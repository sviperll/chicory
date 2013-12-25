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
    public static <T> Predicate<T> and(List<Evaluatable<? super T>> predicates) {
        return new Predicate<>(new AndEvaluatable<>(predicates));
    }

    public static <T> Predicate<T> and(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
        return new Predicate<>(new AndEvaluatable2<>(predicate1, predicate2));
    }

    public static <T> Predicate<T> or(List<Evaluatable<? super T>> predicates) {
        return new Predicate<>(new OrEvaluatable<>(predicates));
    }

    public static <T> Predicate<T> or(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
        return new Predicate<>(new OrEvaluatable2<>(predicate1, predicate2));
    }

    public static <T> Predicate<T> valueOf(final Function<T, Boolean> function) {
        return valueOf(new Evaluatable<T>() {
            @Override
            public boolean evaluate(T t) {
                return function.apply(t);
            }
        });
    }

    public static <T> Predicate<T> valueOf(Evaluatable<T> evaluatable) {
        if (evaluatable instanceof Predicate)
            return (Predicate<T>)evaluatable;
        else
            return new Predicate<>(evaluatable);
    }

    private final Evaluatable<T> predicate;
    private Predicate(Evaluatable<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean evaluate(T t) {
        return predicate.evaluate(t);
    }

    public Function<T, Boolean> asFunction() {
        return Function.valueOf(new EvaluatableApplicable<>(predicate));
    }

    public Predicate<T> not() {
        return new Predicate<>(new NotEvaluatable<>(predicate));
    }

    public Predicate<T> and(Evaluatable<? super T> thatPredicate) {
        List<Evaluatable<? super T>> list = Arrays.<Evaluatable<? super T>>asList(predicate, thatPredicate);
        return new Predicate<>(new AndEvaluatable<>(list));
    }

    public Predicate<T> or(Evaluatable<? super T> thatPredicate) {
        List<Evaluatable<? super T>> list = Arrays.<Evaluatable<? super T>>asList(predicate, thatPredicate);
        return new Predicate<>(new OrEvaluatable<>(list));
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
        private final List<? extends Evaluatable<? super T>> predicates;
        public AndEvaluatable(List<? extends Evaluatable<? super T>> predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean evaluate(T t) {
            for (Evaluatable<? super T> predicate: predicates)
                if (!predicate.evaluate(t))
                    return false;
            return true;
        }
    }

    private static class AndEvaluatable2<T> implements Evaluatable<T> {
        private final Evaluatable<? super T> predicate1;
        private final Evaluatable<? super T> predicate2;
        public AndEvaluatable2(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
            this.predicate1 = predicate1;
            this.predicate2 = predicate2;
        }

        @Override
        public boolean evaluate(T t) {
            return predicate1.evaluate(t) && predicate2.evaluate(t);
        }
    }

    private static class OrEvaluatable<T> implements Evaluatable<T> {
        private final List<? extends Evaluatable<? super T>> predicates;
        public OrEvaluatable(List<? extends Evaluatable<? super T>> predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean evaluate(T t) {
            for (Evaluatable<? super T> predicate: predicates)
                if (predicate.evaluate(t))
                    return true;
            return false;
        }
    }

    private static class OrEvaluatable2<T> implements Evaluatable<T> {
        private final Evaluatable<? super T> predicate1;
        private final Evaluatable<? super T> predicate2;
        public OrEvaluatable2(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
            this.predicate1 = predicate1;
            this.predicate2 = predicate2;
        }

        @Override
        public boolean evaluate(T t) {
            return predicate1.evaluate(t) || predicate2.evaluate(t);
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
