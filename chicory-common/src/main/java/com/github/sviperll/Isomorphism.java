/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Isomorphism<T, U> implements IsomorphismDefinition<T, U> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Isomorphism ID = new Isomorphism(new IdIsomorphismDefinition());

    @SuppressWarnings({"unchecked"})
    public static <T> Isomorphism<T, T> id() {
        return (Isomorphism<T, T>)ID;
    }

    public static <T, U> Isomorphism<T, U> of(final Applicable<T, U> forward, final Applicable<U, T> backward) {
        return valueOf(new IsomorphismDefinition<T, U>() {
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

    public static <T, U> Isomorphism<T, U> valueOf(IsomorphismDefinition<T, U> isomorphism) {
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
        return Function.valueOf(new Applicable<T, U>() {
            @Override
            public U apply(T t) {
                return isomorphism.forward(t);
            }
        });
    }

    public Function<U, T> backwardFunction() {
        return Function.valueOf(new Applicable<U, T>() {
            @Override
            public T apply(U t) {
                return isomorphism.backward(t);
            }
        });
    }

    private static class IdIsomorphismDefinition<T> implements IsomorphismDefinition<T, T> {
        @Override
        public T forward(T object) {
            return object;
        }

        @Override
        public T backward(T object) {
            return object;
        }
    }
}
