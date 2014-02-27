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
public abstract class Predicate<T> implements Evaluatable<T> {
    public static <T> Predicate<T> truePredicate() {
        return valueOf(new Evaluatable<T>() {
            @Override
            public boolean evaluate(T t) {
                return true;
            }
        });
    }

    public static <T> Predicate<T> falsePredicate() {
        return valueOf(new Evaluatable<T>() {
            @Override
            public boolean evaluate(T t) {
                return false;
            }
        });
    }

    public static <T> Predicate<T> and(List<Evaluatable<? super T>> predicates) {
        Predicate<T> result = truePredicate();
        for (Evaluatable<? super T> e: predicates) {
            result = result.and(e);
        }
        return result;
    }

    public static <T> Predicate<T> and(final Evaluatable<? super T> predicate1, final Evaluatable<? super T> predicate2) {
        return new Predicate<T>() {
            @Override
            public Function<T, Boolean> asFunction() {
                return Predicate.asFunction(this);
            }

            @Override
            public Predicate<T> not() {
                return Predicate.not(this);
            }

            @Override
            public Predicate<T> and(Evaluatable<? super T> predicate3) {
                Predicate<T> right = Predicate.and(predicate2, predicate3);
                return Predicate.and(predicate1, right);
            }

            @Override
            public Predicate<T> or(Evaluatable<? super T> predicate3) {
                return Predicate.or(this, predicate3);
            }

            @Override
            public boolean evaluate(T t) {
                return predicate1.evaluate(t) && predicate2.evaluate(t);
            }
        };
    }

    public static <T> Predicate<T> or(List<Evaluatable<? super T>> predicates) {
        Predicate<T> result = falsePredicate();
        for (Evaluatable<? super T> e: predicates) {
            result = result.or(e);
        }
        return result;
    }

    public static <T> Predicate<T> or(final Evaluatable<? super T> predicate1, final Evaluatable<? super T> predicate2) {
        return new Predicate<T>() {
            @Override
            public Function<T, Boolean> asFunction() {
                return Predicate.asFunction(this);
            }

            @Override
            public Predicate<T> not() {
                return Predicate.not(this);
            }

            @Override
            public Predicate<T> and(Evaluatable<? super T> predicate3) {
                return Predicate.and(this, predicate3);
            }

            @Override
            public Predicate<T> or(Evaluatable<? super T> predicate3) {
                Predicate<T> right = Predicate.or(predicate2, predicate3);
                return Predicate.or(predicate1, right);
            }

            @Override
            public boolean evaluate(T t) {
                return predicate1.evaluate(t) || predicate2.evaluate(t);
            }
        };
    }

    public static <T> Predicate<T> not(final Evaluatable<T> predicate1) {
        return new Predicate<T>() {
            @Override
            public Function<T, Boolean> asFunction() {
                return Predicate.asFunction(this);
            }

            @Override
            public Predicate<T> not() {
                return valueOf(predicate1);
            }

            @Override
            public Predicate<T> and(Evaluatable<? super T> predicate2) {
                return Predicate.and(this, predicate2);
            }

            @Override
            public Predicate<T> or(Evaluatable<? super T> predicate2) {
                return Predicate.or(this, predicate2);
            }

            @Override
            public boolean evaluate(T t) {
                return !predicate1.evaluate(t);
            }
        };
    }

    public static <T> Function<T, Boolean> asFunction(final Evaluatable<T> predicate) {
        return Function.valueOf(new Applicable<T, Boolean>() {
            @Override
            public Boolean apply(T t) {
                return predicate.evaluate(t);
            }
        });
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
            return new Predicate<T>() {
                @Override
                public Function<T, Boolean> asFunction() {
                    return Predicate.asFunction(this);
                }

                @Override
                public Predicate<T> not() {
                    return Predicate.not(evaluatable);
                }

                @Override
                public Predicate<T> and(Evaluatable<? super T> predicate2) {
                    return Predicate.and(evaluatable, predicate2);
                }

                @Override
                public Predicate<T> or(Evaluatable<? super T> predicate2) {
                    return Predicate.or(evaluatable, predicate2);
                }

                @Override
                public boolean evaluate(T t) {
                    return evaluatable.evaluate(t);
                }
            };
        }
    }

    private Predicate() {
    }

    public abstract Function<T, Boolean> asFunction();
    public abstract Predicate<T> not();
    public abstract Predicate<T> and(Evaluatable<? super T> thatPredicate);
    public abstract Predicate<T> or(Evaluatable<? super T> thatPredicate);
}
