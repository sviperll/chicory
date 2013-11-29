/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Isomorphism<T, U> implements IsomorphismDefinition<T, U> {
    public static <T, U> Isomorphism<T, U> createInstance(final Applicable<T, U> forward, final Applicable<U, T> backward) {
        return createInstance(new IsomorphismDefinition<T, U>() {
            @Override
            public U forward(T object) {
                return forward.apply(object);
            }

            @Override
            public T backward(U object) {
                return backward.apply(object);
            }
        });
    }

    public static <T, U> Isomorphism<T, U> createInstance(IsomorphismDefinition<T, U> isomorphism) {
        return new Isomorphism<>(isomorphism);
    }

    private final IsomorphismDefinition<T, U> isomorphism;
    private Isomorphism(IsomorphismDefinition<T, U> isomorphism) {
        this.isomorphism = isomorphism;
    }

    @Override
    public U forward(T object) {
        return isomorphism.forward(object);
    }

    @Override
    public T backward(U object) {
        return isomorphism.backward(object);
    }

    public <V> Isomorphism<V, U> composeWith(final IsomorphismDefinition<V, T> thatIsomorphism) {
        return new Isomorphism<>(new IsomorphismDefinition<V, U>() {
            @Override
            public U forward(V object) {
                return isomorphism.forward(thatIsomorphism.forward(object));
            }

            @Override
            public V backward(U object) {
                return thatIsomorphism.backward(isomorphism.backward(object));
            }
        });
    }

    public <V> Isomorphism<T, V> andThen(final IsomorphismDefinition<U, V> thatIsomorphism) {
        return new Isomorphism<>(new IsomorphismDefinition<T, V>() {
            @Override
            public V forward(T object) {
                return thatIsomorphism.forward(isomorphism.forward(object));
            }

            @Override
            public T backward(V object) {
                return isomorphism.backward(thatIsomorphism.backward(object));
            }
        });
    }

    public Isomorphism<U, T> reverse() {
        return new Isomorphism<>(new IsomorphismDefinition<U, T>() {
            @Override
            public T forward(U object) {
                return isomorphism.backward(object);
            }

            @Override
            public U backward(T object) {
                return isomorphism.forward(object);
            }
        });
    }

    public Isomorphism<T, U> passNullThrough() {
        return new Isomorphism<>(new IsomorphismDefinition<T, U>() {
            @Override
            public U forward(T object) {
                if (object == null)
                    return null;
                else
                    return isomorphism.forward(object);
            }

            @Override
            public T backward(U object) {
                if (object == null)
                    return null;
                else
                    return isomorphism.backward(object);
            }
        });
    }

    public Isomorphism<T, U> throwOnNull() {
        return new Isomorphism<>(new IsomorphismDefinition<T, U>() {
            @Override
            public U forward(T object) {
                if (object == null)
                    throw new NullPointerException();
                else
                    return isomorphism.forward(object);
            }

            @Override
            public T backward(U object) {
                if (object == null)
                    throw new NullPointerException();
                else
                    return isomorphism.backward(object);
            }
        });
    }

    public Function<T, U> forwardFunction() {
        return new Function<>(new Applicable<T, U>() {
            @Override
            public U apply(T t) {
                return isomorphism.forward(t);
            }
        });
    }

    public Function<U, T> backwardFunction() {
        return new Function<>(new Applicable<U, T>() {
            @Override
            public T apply(U t) {
                return isomorphism.backward(t);
            }
        });
    }
}
