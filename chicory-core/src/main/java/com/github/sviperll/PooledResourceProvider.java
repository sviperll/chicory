/*
 * Copyright (c) 2015, vir
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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author vir
 */
public class PooledResourceProvider<T> implements ResourceProviderDefinition<T> {
    public static <T> ResourceProvider<T> createInstance(ResourceProviderDefinition<T> provider, long maxIdleTimeMillis, int maxPoolSize) {
        PooledResourceProvider<T> pooledResourceProvider = new PooledResourceProvider<T>();
        for (int i = 0; i < maxPoolSize; i++) {
            pooledResourceProvider.add(provider, maxIdleTimeMillis);
        }
        return ResourceProvider.of(pooledResourceProvider);
    }

    // availableQueue must be synchronous queue,
    // to allow EmptyPoolElement to block until
    // it's actually taken out of the queue.
    // EmptyPoolElement should block as long as possible to
    // avoid premature resource allocation
    // See EmptyPoolElement.run implementation
    private final BlockingQueue<ResourceProviderDefinition<T>> availableQueue = new SynchronousQueue<ResourceProviderDefinition<T>>();;

    private PooledResourceProvider() {
    }

    @Override
    public void provideResourceTo(Consumer<? super T> consumer) throws InterruptedException {
        ResourceProviderDefinition<T> provider = availableQueue.take();
        provider.provideResourceTo(consumer);
    }

    void add(ResourceProviderDefinition<T> provider, long maxIdleTimeMillis) {
        EmptyPoolElement element = new EmptyPoolElement(provider, maxIdleTimeMillis);
        Thread thread = new Thread(element);

        // Will automatically shut down on application shut down.
        thread.setDaemon(true);
        thread.start();
    }

    private class EmptyPoolElement implements Runnable, ResourceProviderDefinition<T> {
        private final ResourceProviderDefinition<T> provider;
        private final long maxIdleTimeMillis;
        private final BlockingQueue<PoolElement> responseQueue = new SynchronousQueue<PoolElement>();

        EmptyPoolElement(ResourceProviderDefinition<T> provider, long maxIdleTimeMillis) {
            this.provider = provider;
            this.maxIdleTimeMillis = maxIdleTimeMillis;
        }

        @Override
        public void run() {
            for (;;) {
                for (;;) {
                    try {
                        // We should block as long as possible to
                        // avoid premature resource allocation
                        availableQueue.put(this);
                        break;
                    } catch (InterruptedException ex) {
                    }
                }
                for (;;) {
                    try {
                        provider.provideResourceTo(new Consumer<T>() {
                            @Override
                            public void accept(T value) {
                                PoolElement element = new PoolElement(value, maxIdleTimeMillis);
                                for (;;) {
                                    try {
                                        responseQueue.put(element);
                                        break;
                                    } catch (InterruptedException ex) {
                                    }
                                }
                                element.run();
                            }
                        });
                        break;
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }

        @Override
        public void provideResourceTo(Consumer<? super T> consumer) throws InterruptedException {
            PoolElement element = responseQueue.take();
            element.provideResourceTo(consumer);
        }
    }

    private class PoolElement implements Runnable, ResourceProviderDefinition<T> {
        private final BlockingQueue<T> consumedQueue = new SynchronousQueue<T>();
        private final T value;
        private final long idleTimeMillis;

        PoolElement(T value, long idleTimeMillis) {
            this.value = value;
            this.idleTimeMillis = idleTimeMillis;
        }

        @Override
        public void provideResourceTo(Consumer<? super T> consumer) {
            try {
                consumer.accept(value);
            } finally {
                // InterruptedException shouldn't be thrown out here
                // sinse we can't retry potentially not side-effect free
                // consumer action.
                for (;;) {
                    try {
                        consumedQueue.put(value);
                        break;
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }

        @Override
        public void run() {
            for (;;) {
                for (;;) {
                    try {
                        consumedQueue.take();
                        break;
                    } catch (InterruptedException ex) {
                    }
                }
                boolean used;
                for (;;) {
                    try {
                        used = availableQueue.offer(this, idleTimeMillis, TimeUnit.MILLISECONDS);
                        break;
                    } catch (InterruptedException ex) {
                    }
                }
                if (!used)
                    break;
            }
        }
    }
}
