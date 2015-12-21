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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class PooledResourceProvider<T> implements ResourceProviderDefinition<T>, Runnable {
    private static final Logger logger = Logger.getLogger(PooledResourceProvider.class.getName());
    public static <T> ResourceProvider<T> createInstance(ResourceProviderDefinition<T> provider, long maxIdleTimeMillis, int poolSize) {
        Executor executor = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(PooledResourceProvider.class.getName() + " pool thread");
                thread.setDaemon(true);
                return thread;
            }
        });
        PooledResourceProvider<T> pooledResourceProvider = new PooledResourceProvider<T>(provider, maxIdleTimeMillis, new SynchronousQueue<SyncronousValue<T>>());
        for (int i = 0; i < poolSize; i++) {
            executor.execute(pooledResourceProvider);
        }
        return ResourceProvider.of(pooledResourceProvider);
    }

    private final ResourceProviderDefinition<T> provider;
    private final long idleTimeoutMillis;
    private final BlockingQueue<SyncronousValue<T>> availableValues;
    private final Semaphore currentValueExpectations = new Semaphore(0);

    private PooledResourceProvider(ResourceProviderDefinition<T> provider, long idleTimeoutMillis, BlockingQueue<SyncronousValue<T>> availableValues) {
        this.provider = provider;
        this.idleTimeoutMillis = idleTimeoutMillis;
        this.availableValues = availableValues;
    }

    @Override
    public void provideResourceTo(Consumer<? super T> consumer) throws InterruptedException {
        currentValueExpectations.release();
        SyncronousValue<T> value = availableValues.take();
        try {
            consumer.accept(value.value());
        } finally {
            value.release();
        }
    }

    @Override
    public void run() {
        for (;;) {
            currentValueExpectations.acquireUninterruptibly();
            for (;;) {
                try {
                    provider.provideResourceTo(new Consumer<T>() {
                        @Override
                        public void accept(T value) {
                            SyncronousValue<T> syncronousValue = new SyncronousValue<T>(value);
                            try {
                                syncronousValue.acquire();
                                for (;;) {
                                    availableValues.put(syncronousValue);
                                    syncronousValue.acquire();
                                    boolean isAcquired = currentValueExpectations.tryAcquire(idleTimeoutMillis, TimeUnit.MICROSECONDS);
                                    if (!isAcquired)
                                        break;
                                }
                            } catch (InterruptedException ex) {
                            }
                        }
                    });
                    break;
                } catch (InterruptedException ex) {
                } catch (RuntimeException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                    } catch (InterruptedException ex1) {
                    }
                }
            }
        }
    }

    private static class SyncronousValue<T> {
        private final Semaphore semaphore = new Semaphore(1);
        private final T value;
        SyncronousValue(T value) {
            this.value = value;
        }
        @Nonnull
        T value() throws InterruptedException {
            return value;
        }
        void acquire() throws InterruptedException {
            semaphore.acquire();
        }
        void release() {
            semaphore.release();
        }
    }

}
