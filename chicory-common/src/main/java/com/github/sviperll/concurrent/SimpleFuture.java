/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class SimpleFuture<V> implements Future<V> {
    private final Object lock = new Object();
    private V value = null;
    private boolean isCancelled = false;
    private boolean isDone = false;

    public void set(V value) {
        synchronized(lock) {
            if (isDone || isCancelled)
                throw new IllegalStateException("SimpleFuture already set");
            this.value = value;
            isDone = true;
            lock.notifyAll();
        }
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean wasCancelled;
        synchronized(lock) {
            wasCancelled = isCancelled;
            isCancelled = true;
            lock.notifyAll();
        }
        return wasCancelled;
    }

    @Override
    public boolean isCancelled() {
        synchronized(lock) {
            return isCancelled;
        }
    }

    @Override
    public boolean isDone() {
        synchronized(lock) {
            return isDone;
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        synchronized(lock) {
            while (!isDone && !isCancelled)
                lock.wait();
            if (isCancelled)
                throw new CancellationException();
            return value;
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        synchronized(lock) {
            while (!isDone && !isCancelled)
                lock.wait(unit.toMillis(timeout));
            if (isCancelled)
                throw new CancellationException();
            if (!isDone)
                throw new TimeoutException();
            return value;
        }
    }

}
