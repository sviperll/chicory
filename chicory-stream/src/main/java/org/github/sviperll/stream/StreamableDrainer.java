/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class StreamableDrainer<T> {
    public static <T> CloseableIterator<T> createIterator(Streamable<T> streamable) {
        StreamableDrainer<T> factory = new StreamableDrainer<>(streamable);
        return factory.createIterator();
    }

    private final Streamable<T> streamable;
    private final BlockingQueue<DrainerRequest> requestQueue = new SynchronousQueue<>();
    private final BlockingQueue<DrainerResponse<T>> responseQueue = new SynchronousQueue<>();
    private boolean needsMore = true;

    private StreamableDrainer(Streamable<T> streamable) {
        this.streamable = streamable;
    }

    private CloseableIterator<T> createIterator() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                drain();
            }
        });
        thread.start();
        putRequest(DrainerRequest.FETCH_VALUE);
        DrainerResponse<T> response = takeResponse();
        if (!response.isClosedResponse()) {
            return new DrainerIterator(response.value());
        } else {
            return new CloseableIterator<T>() {
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
            };
        }
    }

    private void drain() {
        streamable.forEach(new SaturableConsuming<T>() {
            @Override
            public void accept(T value) {
                if (needsMore) {
                    DrainerRequest request = takeRequest();
                    if (request.isCloseRequest()) {
                        needsMore = false;
                    } else {
                        putResponse(DrainerResponse.fetchedValue(value));
                    }
                }
            }

            @Override
            public boolean needsMore() {
                return needsMore;
            }
        });
        if (needsMore)
            takeRequest();
        putResponse(DrainerResponse.<T>isClosed());
    }

    private DrainerRequest takeRequest() {
        for (;;) {
            try {
                return requestQueue.take();
            } catch (InterruptedException ex) {
            }
        }
    }

    private void putRequest(DrainerRequest request) {
        for (;;) {
            try {
                requestQueue.put(request);
                return;
            } catch (InterruptedException ex) {
            }
        }
    }

    private void putResponse(DrainerResponse<T> response) {
        for (;;) {
            try {
                responseQueue.put(response);
                return;
            } catch (InterruptedException ex) {
            }
        }
    }

    private DrainerResponse<T> takeResponse() {
        for (;;) {
            try {
                return responseQueue.take();
            } catch (InterruptedException ex) {
            }
        }
    }

    private class DrainerIterator implements CloseableIterator<T> {
        private boolean isClosed = false;
        private T nextValue;

        public DrainerIterator(T nextValue) {
            this.nextValue = nextValue;
        }

        @Override
        public void close() {
            if (!isClosed) {
                putRequest(DrainerRequest.CLOSE);
                takeResponse();
            }
        }

        @Override
        public boolean hasNext() {
            return !isClosed;
        }

        @Override
        public T next() {
            T result = nextValue;
            putRequest(DrainerRequest.FETCH_VALUE);
            DrainerResponse<T> response = takeResponse();
            if (!response.isClosedResponse()) {
                nextValue = response.value();
            } else {
                nextValue = null;
                isClosed = true;
            }
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static enum DrainerRequest {
        CLOSE {
            @Override
            public boolean isCloseRequest() {
                return true;
            }
            @Override
            public boolean isFetchRequest() {
                return false;
            }
        }, FETCH_VALUE {
            @Override
            public boolean isCloseRequest() {
                return false;
            }
            @Override
            public boolean isFetchRequest() {
                return true;
            }
        };

        public abstract boolean isCloseRequest();
        public abstract boolean isFetchRequest();
    }

    private static class DrainerResponse<T> {
        @SuppressWarnings({"rawtypes", "unchecked"})
        private static final DrainerResponse IS_CLOSED = new DrainerResponse(null);
        public static <T> DrainerResponse<T> fetchedValue(T value) {
            return new DrainerResponse<>(value);
        }

        @SuppressWarnings("unchecked")
        private static <T> DrainerResponse<T> isClosed() {
            return IS_CLOSED;
        }
        private final T value;

        public DrainerResponse(T value) {
            this.value = value;
        }
        public boolean isClosedResponse() {
            return this == IS_CLOSED;
        }
        public boolean isFetchedValueResponse() {
            return this != IS_CLOSED;
        }

        public T value() {
            if (!isFetchedValueResponse())
                throw new IllegalStateException();
            return value;
        }
    }
}
