/*
 * Copyright (c) 2016, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.sviperll;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 */
public class PooledResourceProvider<T> implements ResourceProviderDefinition<T> {
    public static <T> ResourceProvider<T> createInstance(ResourceProviderDefinition<T> provider, long maxIdleTimeMillis, int maxOpened) {
        PooledResourceProvider<T> pooledResourceProvider = new PooledResourceProvider<>(provider, maxIdleTimeMillis, maxOpened);
        return ResourceProvider.of(pooledResourceProvider);
    }

    private final ResourceProviderDefinition<T> provider;
    private final long maxIdleTimeMillis;
    private final BlockingQueue<Consumable<T>> queue = new SynchronousQueue<>();
    private final Semaphore threads;

    private PooledResourceProvider(ResourceProviderDefinition<T> provider, long maxIdleTimeMillis, int maxOpened) {
        this.provider = provider;
        this.maxIdleTimeMillis = maxIdleTimeMillis;
        this.threads = new Semaphore(maxOpened);
    }

    @Override
    public void provideResourceTo(Consumer<? super T> consumer) throws InterruptedException {
        try (Consumable<T> entry = openConsumable()) {
            consumer.accept(entry.value());
        }
    }

    private Consumable<T> openConsumable() throws InterruptedException {
        Consumable<T> entry = queue.poll();
        while (entry == null) {
            if (threads.tryAcquire()) {
                Thread thread = new Thread(new ConsumableCreator());
                thread.start();
            }
            entry = queue.poll(5, TimeUnit.SECONDS);
        }
        return entry;
    }

    private class ConsumableCreator implements Runnable {
        @Override
        public void run() {
            try {
                provider.provideResourceTo(this::processValue);
            } catch (RuntimeException ex) {
                NotAllocatedWithException notAllocated = new NotAllocatedWithException(ex);
                notAllocated.run();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                threads.release();
            }
        }

        private void processValue(T value) {
            Allocated entry = new Allocated(value);
            try {
                entry.run();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static abstract class Consumable<T> implements AutoCloseable {
        private Consumable() {
        }
        
        abstract T value();

        @Override
        abstract public void close();

        abstract void run() throws InterruptedException;
    }

    private class NotAllocatedWithException extends Consumable<T> {

        private final RuntimeException exception;

        NotAllocatedWithException(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        T value() {
            throw exception;
        }

        @Override
        public void close() {
        }

        @Override
        void run() {
            while (queue.offer(this))
                ;
        }
    }

    private class Allocated extends Consumable<T> {
        private final T value;
        private boolean isUsed = false;
        private final Object lock = new Object();
        Allocated(T value) {
            this.value = value;

        }

        @Override
        public void close() {
            synchronized(lock) {
                if (isUsed) {
                    isUsed = false;
                    lock.notifyAll();
                }
            }
        }

        @Override
        synchronized void run() throws InterruptedException {
            for (;;) {
                synchronized(lock) {
                    while (isUsed)
                        lock.wait();
                    isUsed = true;
                }
                boolean consumed = queue.offer(this, maxIdleTimeMillis, TimeUnit.MILLISECONDS);
                if (!consumed)
                    break;
            }
        }

        @Override
        T value() {
            return value;
        }
    }
}
