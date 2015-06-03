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
public class Isomorphism<T, U> implements IsomorphismDefinition<T, U> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Isomorphism ID = new Isomorphism(new IdIsomorphismDefinition());

    @SuppressWarnings({"unchecked"})
    public static <T> Isomorphism<T, T> identity() {
        return ID;
    }

    public static <T, U> Isomorphism<T, U> of(final Applicable<? super T, U> forward, final Applicable<? super U, T> backward) {
        return of(new IsomorphismDefinition<T, U>() {
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

    public static <T, U> Isomorphism<T, U> of(IsomorphismDefinition<T, U> isomorphism) {
        if (isomorphism instanceof Isomorphism)
            return (Isomorphism<T, U>)isomorphism;
        else
            return new Isomorphism<T, U>(isomorphism);
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
        return new Isomorphism<V, U>(new IsomorphismDefinition<V, U>() {
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
        return new Isomorphism<T, V>(new IsomorphismDefinition<T, V>() {
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
        return new Isomorphism<U, T>(new IsomorphismDefinition<U, T>() {
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
        return new Isomorphism<T, U>(new IsomorphismDefinition<T, U>() {
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
        return new Isomorphism<T, U>(new IsomorphismDefinition<T, U>() {
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
        return Function.of(new Applicable<T, U>() {
            @Override
            public U apply(T t) {
                return isomorphism.forward(t);
            }
        });
    }

    public Function<U, T> backwardFunction() {
        return Function.of(new Applicable<U, T>() {
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
