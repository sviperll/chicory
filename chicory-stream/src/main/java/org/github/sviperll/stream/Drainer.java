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
    private DrainerState<T> state = new CommunicatingState();

    Drainer(Streamable<T> streamable) {
        this.streamable = streamable;
    }

    @Override
    public void run() {
        try {
            streamable.forEach(this);
        } catch (RuntimeException ex) {
            state.setException(ex);
        }
        state.finish();
    }

    @Override
    public void accept(T value) {
        state.accept(value);
    }

    @Override
    public boolean needsMore() {
        return state.needsMore();
    }

    private void setExceptionCaughtState(RuntimeException ex) {
        state = new ExceptionCaughtState(ex);
    }

    private void setClosedState() {
        state = new ClosedState();
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
                DrainerRequest request = requestQueue.take();
                return request;
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

    private interface DrainerState<T> extends SaturableConsuming<T> {
        void finish();
        void setException(RuntimeException ex);
    }

    private class CommunicatingState implements DrainerState<T> {
        @Override
        public void accept(final T value) {
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
                        setClosedState();
                        return null;
                    }
                });
            } catch (RuntimeException ex) {
                setExceptionCaughtState(ex);
                throw ex;
            }
        }

        @Override
        public boolean needsMore() {
            return true;
        }

        @Override
        public void finish() {
            takeRequest();
            putResponse(DrainerResponse.<T>closed());
        }

        @Override
        public void setException(RuntimeException ex) {
            takeRequest();
            setExceptionCaughtState(ex);
        }
    }

    private class ClosedState implements DrainerState<T> {
        @Override
        public void accept(T value) {
        }

        @Override
        public boolean needsMore() {
            return false;
        }

        @Override
        public void finish() {
            putResponse(DrainerResponse.<T>closed());
        }

        @Override
        public void setException(RuntimeException ex) {
            setExceptionCaughtState(ex);
        }
    }


    private class ExceptionCaughtState implements DrainerState<T> {
        private final RuntimeException exception;

        private ExceptionCaughtState(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public void accept(T value) {
        }

        @Override
        public boolean needsMore() {
            return false;
        }

        @Override
        public void finish() {
            putResponse(DrainerResponse.<T>error(exception));
        }

        @Override
        public void setException(RuntimeException ex) {
            setExceptionCaughtState(ex);
        }
    }
}
