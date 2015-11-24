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

import java.util.List;

/**
 * Objects used as logic predicates.
 *
 * @see Evaluatable
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public abstract class Predicate<T> implements Evaluatable<T> {
    @SuppressWarnings({"rawtypes"})
    private static final Predicate TRUE = new TruePredicate();

    @SuppressWarnings({"rawtypes"})
    private static final Predicate FALSE = new FalsePredicate();

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> truePredicate() {
        return TRUE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> falsePredicate() {
        return FALSE;
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

    public static <T> Predicate<T> from(final Function<? super T, Boolean> function) {
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
            return new SimplePredicate<T>(evaluatable);
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
        SimplePredicate(Evaluatable<? super T> evaluatable) {
            this.evaluatable = evaluatable;
        }

        @Override
        public Predicate<T> not() {
            return new NotPredicate<T>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<T>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
            return new OrPredicate<T>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return evaluatable.evaluate(t);
        }
    }

    private static class TruePredicate<T> extends Predicate<T> {
        @Override
        public Predicate<T> not() {
            return new NotPredicate<T>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<T>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
            return new OrPredicate<T>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return true;
        }
    }

    private static class FalsePredicate<T> extends Predicate<T> {
        @Override
        public Predicate<T> not() {
            return new NotPredicate<T>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<T>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
            return new OrPredicate<T>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return false;
        }
    }

    private static class NotPredicate<T> extends Predicate<T> {
        private final Evaluatable<? super T> original;
        NotPredicate(Evaluatable<? super T> original) {
            this.original = original;
        }

        @Override
        public Predicate<T> not() {
            return Predicate.of(original);
        }

        @Override
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<T>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
            return new OrPredicate<T>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return !original.evaluate(t);
        }
    }

    private static class AndPredicate<T> extends Predicate<T> {
        private final Evaluatable<? super T> predicate1;
        private final Evaluatable<? super T> predicate2;
        AndPredicate(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
            this.predicate1 = predicate1;
            this.predicate2 = predicate2;
        }

        @Override
        public Predicate<T> not() {
            return new NotPredicate<T>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<? super T> predicate3) {
            return new AndPredicate<T>(predicate1, of(predicate2).and(predicate3));
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> that) {
            return new OrPredicate<T>(this, that);
        }

        @Override
        public boolean evaluate(T t) {
            return predicate1.evaluate(t) && predicate2.evaluate(t);
        }
    }

    private static class OrPredicate<T> extends Predicate<T> {
        private final Evaluatable<? super T> predicate1;
        private final Evaluatable<? super T> predicate2;
        OrPredicate(Evaluatable<? super T> predicate1, Evaluatable<? super T> predicate2) {
            this.predicate1 = predicate1;
            this.predicate2 = predicate2;
        }

        @Override
        public Predicate<T> not() {
            return new NotPredicate<T>(this);
        }

        @Override
        public Predicate<T> and(Evaluatable<? super T> that) {
            return new AndPredicate<T>(this, that);
        }

        @Override
        public Predicate<T> or(Evaluatable<? super T> predicate3) {
            return new OrPredicate<T>(predicate1, of(predicate2).or(predicate3));
        }

        @Override
        public boolean evaluate(T t) {
            return predicate1.evaluate(t) || predicate2.evaluate(t);
        }
    }
}
