/*
 * Copyright (c) 2015, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class PooledResourceProvider<T> implements ResourceProviderDefinition<T> {
    public static <T> ResourceProvider<T> createInstance(ResourceProviderDefinition<T> provider, long maxIdleTimeMillis, int poolSize) {
        Executor executor = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(PooledResourceProvider.class.getName() + "-pool-thread");
                thread.setDaemon(true);
                return thread;
            }
        });
        Queue<T> queue = new Queue<T>(maxIdleTimeMillis);
        PooledResourceProvider<T> pooledResourceProvider = new PooledResourceProvider<T>(provider, queue);
        for (int i = 0; i < poolSize; i++) {
            executor.execute(pooledResourceProvider.createThreadRunnable());
        }
        return ResourceProvider.of(pooledResourceProvider);
    }

    private final ResourceProviderDefinition<T> provider;
    private final Queue<T> queue;

    private PooledResourceProvider(ResourceProviderDefinition<T> provider, Queue<T> queue) {
        this.provider = provider;
        this.queue = queue;
    }

    @Override
    public void provideResourceTo(Consumer<? super T> consumer) throws InterruptedException {
        queue.expect();
        queue.takeValueAndPassTo(consumer);
    }

    @Nonnull
    private Runnable createThreadRunnable() {
        return new ThreadRunnable();
    }

    private class ThreadRunnable implements Runnable, Consumer<T> {
        private final Semaphore semaphore = new Semaphore(1);
        boolean isExpected = true;

        @Override
        public void run() {
            for (;;) {
                queue.waitForExpectationIndefinitlyUninterruptibly();
                isExpected = true;
                while (isExpected) {
                    try {
                        provider.provideResourceTo(this);
                    } catch (InterruptedException ex) {
                    } catch (RuntimeException ex) {
                        try {
                            queue.putElement(new ExceptionElement(ex));
                            isExpected = false;
                        } catch (InterruptedException ex1) {
                        }
                    }
                }
            }
        }

        @Override
        public void accept(T value) {
            ValueElement element = new ValueElement(value);
            try {
                semaphore.acquire();
                try {
                    while (isExpected) {
                        queue.putElement(element);
                        isExpected = false;
                        semaphore.acquire();
                        isExpected = queue.waitForExpectationBoundedTime();
                    }
                } finally {
                    semaphore.release();
                }
            } catch (InterruptedException ex) {
            }
        }

        private class ValueElement extends Queue.Element<T> {
            private final T value;
            ValueElement(T value) {
                this.value = value;
            }

            @Nonnull
            @Override
            T value() {
                return value;
            }

            @Override
            void release() {
                semaphore.release();
            }
        }
        private class ExceptionElement extends Queue.Element<T> {
            private final RuntimeException exception;
            ExceptionElement(RuntimeException exception) {
                this.exception = exception;
            }

            @Nonnull
            @Override
            T value() {
                throw exception;
            }

            @Override
            void release() {
            }
        }
    }

    private static class Queue<T> {
        private final long idleTimeoutMillis;
        private final BlockingQueue<Element<T>> availableValues = new SynchronousQueue<Element<T>>();
        private final Semaphore currentValueExpectations = new Semaphore(0);
        Queue(long idleTimeoutMillis) {
            this.idleTimeoutMillis = idleTimeoutMillis;
        }
        void waitForExpectationIndefinitlyUninterruptibly() {
            currentValueExpectations.acquireUninterruptibly();
        }
        boolean waitForExpectationBoundedTime() throws InterruptedException {
            return currentValueExpectations.tryAcquire(idleTimeoutMillis, TimeUnit.MILLISECONDS);
        }
        void expect() {
            currentValueExpectations.release();
        }
        void putElement(Element<T> queueElement) throws InterruptedException {
            availableValues.put(queueElement);
        }
        void takeValueAndPassTo(Consumer<? super T> consumer) throws InterruptedException {
            Element<T> value = availableValues.take();
            try {
                consumer.accept(value.value());
            } finally {
                value.release();
            }
        }

        private static abstract class Element<T> {
            @Nonnull
            abstract T value();
            abstract void release();
        }
    }
}
