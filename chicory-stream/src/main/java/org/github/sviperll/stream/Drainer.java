/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class Drainer<T> implements Runnable, SaturableConsuming<T> {
    private final Streamable<T> streamable;
    private final BlockingQueue<DrainerRequest> requestQueue = new SynchronousQueue<>();
    private final BlockingQueue<DrainerResponse<T>> responseQueue = new SynchronousQueue<>();
    private boolean isIteratorClosed = false;

    Drainer(Streamable<T> streamable) {
        this.streamable = streamable;
    }

    @Override
    public void run() {
        streamable.forEach(this);
        if (!isIteratorClosed) {
            takeRequest();
            putResponse(DrainerResponse.<T>closed());
        }
    }

    @Override
    public void accept(final T value) {
        if (!isIteratorClosed) {
            DrainerRequest request = takeRequest();
            try {
                request.accept(new DrainerRequestVisitor<Void>() {
                    @Override
                    public Void fetch() {
                        putResponse(DrainerResponse.<T>fetched(value));
                        return null;
                    }

                    @Override
                    public Void close() {
                        putResponse(DrainerResponse.<T>closed());
                        isIteratorClosed = true;
                        return null;
                    }
                });
            } catch (RuntimeException ex) {
                putResponse(DrainerResponse.<T>error(ex));
                isIteratorClosed = true;
            }
        }
    }

    @Override
    public boolean needsMore() {
        return !isIteratorClosed;
    }

    public DrainerResponse<T> fetch() {
        return request(DrainerRequest.fetch());
    }

    public DrainerResponse<T> close() {
        return request(DrainerRequest.close());
    }

    private DrainerResponse<T> request(DrainerRequest request) {
        for (;;) {
            try {
                requestQueue.put(request);
                break;
            } catch (InterruptedException ex) {
            }
        }
        for (;;) {
            try {
                return responseQueue.take();
            } catch (InterruptedException ex) {
            }
        }
    }

    private DrainerRequest takeRequest() {
        for (;;) {
            try {
                return requestQueue.take();
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
}
