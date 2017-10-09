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

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javafx.util.Duration;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class PooledResourceProviderTest {
    private static final Logger logger = Logger.getLogger(PooledResourceProviderTest.class.getName());

    public PooledResourceProviderTest() {
    }

    @Test
    public void testResourceLimit() {
        Duration runTimeMax = Duration.seconds(2);
        int nAllocatedMax = 3;
        AtomicInteger nAllocated = new AtomicInteger(0);
        ResourceProvider<Void> provider = PooledResourceProvider.createInstance(nAllocatedMax, 500, (consumer) -> {
            nAllocated.incrementAndGet();
            try {
                consumer.accept(null);
            } finally {
                nAllocated.decrementAndGet();
            }
        });
        ExecutorService threadPool = Executors.newCachedThreadPool();
        long start = System.currentTimeMillis();
        long duration = (long)runTimeMax.toMillis();
        for (int i = 0; i < 10; i++) {
            threadPool.execute(() -> {
                while (System.currentTimeMillis() - start < duration) {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextLong(100));
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    provider.provideResourceTo((value) -> {
                        try {
                            Thread.sleep(ThreadLocalRandom.current().nextLong(100));
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            });
        }
        while (System.currentTimeMillis() - start < duration) {
            Assert.assertTrue(nAllocated.get() <= nAllocatedMax);
            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(100));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        threadPool.shutdown();
    }

    @Test
    public void testResourceLimitInPresenceOfConsumerExceptions() {
        Duration runTimeMax = Duration.seconds(2);
        int nAllocatedMax = 3;
        AtomicInteger nAllocated = new AtomicInteger(0);
        ResourceProvider<Void> provider = PooledResourceProvider.createInstance(nAllocatedMax, 500, (consumer) -> {
            nAllocated.incrementAndGet();
            try {
                consumer.accept(null);
            } finally {
                nAllocated.decrementAndGet();
            }
        });
        ExecutorService threadPool = Executors.newCachedThreadPool();
        long start = System.currentTimeMillis();
        long duration = (long)runTimeMax.toMillis();
        for (int i = 0; i < 10; i++) {
            threadPool.execute(() -> {
                while (System.currentTimeMillis() - start < duration) {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextLong(100));
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    try {
                        provider.provideResourceTo((value) -> {
                            throw new IllegalStateException();
                        });
                    } catch (IllegalStateException ex) {
                        // ignore
                    }
                }
            });
        }
        while (System.currentTimeMillis() - start < duration) {
            Assert.assertTrue(nAllocated.get() <= nAllocatedMax);
            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(100));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        threadPool.shutdown();
    }

    @Test
    public void testConcurrencyLimit() {
        Duration runTimeMax = Duration.seconds(2);
        int nRunningMax = 3;
        AtomicInteger nRunning = new AtomicInteger(0);
        ResourceProvider<Void> provider =
                PooledResourceProvider.createInstance(nRunningMax, 500, (consumer) -> consumer.accept(null));
        ExecutorService threadPool = Executors.newCachedThreadPool();
        long start = System.currentTimeMillis();
        long duration = (long)runTimeMax.toMillis();
        for (int i = 0; i < 10; i++) {
            threadPool.execute(() -> {
                while (System.currentTimeMillis() - start < duration) {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextLong(100));
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    provider.provideResourceTo((value) -> {
                        nRunning.incrementAndGet();
                        try {
                            Thread.sleep(ThreadLocalRandom.current().nextLong(100));
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        } finally {
                            nRunning.decrementAndGet();
                        }
                    });
                }
            });
        }
        while (System.currentTimeMillis() - start < duration) {
            Assert.assertTrue(nRunning.get() <= nRunningMax);
            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(100));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        threadPool.shutdown();
    }

    @Test
    public void testIdleTime() throws InterruptedException {
        final long maxIdleTimeMillis = 337;
        final long maxIdleTimeMillisThreashold = 5;
        AtomicInteger nAllocated = new AtomicInteger(0);
        ResourceProvider<Void> provider = PooledResourceProvider.createInstance(10, maxIdleTimeMillis, (consumer) -> {
            nAllocated.incrementAndGet();
            try {
                consumer.accept(null);
            } finally {
                nAllocated.decrementAndGet();
            }
        });
        provider.provideResourceTo(value -> {});
        Assert.assertEquals(1, nAllocated.get());
        Thread.sleep(maxIdleTimeMillis - maxIdleTimeMillisThreashold);
        Assert.assertEquals(1, nAllocated.get());
        Thread.sleep(maxIdleTimeMillisThreashold + maxIdleTimeMillisThreashold);
        Assert.assertEquals(0, nAllocated.get());
    }

    @Test
    public void testIsAllocated() {
        AtomicInteger nAllocated = new AtomicInteger(0);
        ResourceProvider<Void> provider = PooledResourceProvider.createInstance(10, 500, (consumer) -> {
            nAllocated.incrementAndGet();
            try {
                consumer.accept(null);
            } finally {
                nAllocated.decrementAndGet();
            }
        });
        provider.provideResourceTo(value -> {
            Assert.assertThat(nAllocated.get(), CoreMatchers.equalTo(1));
        });
    }

    @Test(expected = NoSuchElementException.class)
    public void testAllocationExceptionIsThrown() {
        ResourceProvider<Void> provider = PooledResourceProvider.createInstance(10, 500, (consumer) -> {
            throw new NoSuchElementException("Should never allocate");
        });
        provider.provideResourceTo(value -> {
            Assert.fail("Should never be executed");
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConsumerExceptionIsThrown() {
        ResourceProvider<Void> provider = PooledResourceProvider.createInstance(10, 500, (consumer) -> {
            consumer.accept(null);
        });
        provider.provideResourceTo(value -> {
            throw new IllegalArgumentException();
        });
    }
}
