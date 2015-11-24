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
package com.github.sviperll.stream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class StreamIterator<T> implements CloseableIterator<T> {
    @SuppressWarnings("rawtypes")
    private static final CloseableIterator EMPTY_INTERATOR = new EmptyIterator();

    public static <T> CloseableIterator<T> createInstance(Streamable<T> streamable) {
        final Drainer<T> drainer = new Drainer<T>(streamable);
        Thread thread = new Thread(drainer);
        thread.start();
        return drainer.fetch().accept(new DrainerResponseVisitor<T, CloseableIterator<T>>() {
            @Override
            public CloseableIterator<T> fetched(T value) {
                StreamIterator<T> iterator = new StreamIterator<T>(drainer);
                iterator.setHasNextValueState(value);
                return iterator;
            }

            @SuppressWarnings({"unchecked"})
            @Override
            public CloseableIterator<T> closed() {
                return EMPTY_INTERATOR;
            }

            @Override
            public CloseableIterator<T> error(RuntimeException exception) {
                throw exception;
            }
        });
    }
    private final Drainer<T> drainer;

    @SuppressWarnings({"unchecked"})
    private CloseableIterator<T> state = EMPTY_INTERATOR;

    public StreamIterator(Drainer<T> drainer) {
        this.drainer = drainer;
    }

    private void setHasNextValueState(T value) {
        this.state = new HasNextValueState(value);
    }

    @SuppressWarnings({"unchecked"})
    private void setClosedState() {
        this.state = EMPTY_INTERATOR;
    }

    private void setErrorState(RuntimeException exception) {
        this.state = new HasErrorState(exception);
    }

    @Override
    public void close() throws IOException {
        state.close();
    }

    @Override
    public boolean hasNext() {
        return state.hasNext();
    }

    @Override
    public T next() {
        return state.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private class HasNextValueState implements CloseableIterator<T> {
        private T value;

        public HasNextValueState(T value) {
            this.value = value;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        @SuppressFBWarnings("IT_NO_SUCH_ELEMENT")
        public T next() {
            T result = value;
            DrainerResponse<T> response = drainer.fetch();
            response.accept(new DrainerResponseVisitor<T, Void>() {
                @Override
                public Void fetched(T newValue) {
                    value = newValue;
                    return null;
                }

                @Override
                public Void closed() {
                    setClosedState();
                    return null;
                }

                @Override
                public Void error(RuntimeException ex) {
                    setErrorState(ex);
                    return null;
                }
            });
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            DrainerResponse<T> response = drainer.close();
            response.accept(new DrainerResponseVisitor<T, Void>() {
                @Override
                public Void fetched(T value) {
                    throw new IllegalStateException("Stream not closed in response to CLOSE request");
                }

                @Override
                public Void closed() {
                    setClosedState();
                    return null;
                }

                @Override
                public Void error(RuntimeException exception) {
                    setErrorState(exception);
                    throw exception;
                }
            });
        }
    }

    private class HasErrorState implements CloseableIterator<T> {
        private final RuntimeException exception;
        public HasErrorState(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        @SuppressFBWarnings("IT_NO_SUCH_ELEMENT")
        public T next() {
            setClosedState();
            throw exception;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            setClosedState();
        }
    }

    private static class EmptyIterator<T> implements CloseableIterator<T> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
        }
    }
}
