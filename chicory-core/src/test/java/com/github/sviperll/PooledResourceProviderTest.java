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

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public void testThreadLimit() {
        final int maxAllocatedResources = 3;
        final CountingResourceProvider providerDefinition = new CountingResourceProvider();
        final ResourceProvider<AtomicInteger> provider = PooledResourceProvider.createInstance(providerDefinition, 500, maxAllocatedResources);
        final Random random = new Random();
        ThreadLimitTester tester = new ThreadLimitTester(provider, providerDefinition, maxAllocatedResources, random, false);
        tester.test();
    }

    @Test
    public void testIsAllocated() {
        final int maxAllocatedResources = 3;
        final long maxIdleTimeMillis = 500;
        final CountingResourceProvider providerDefinition = new CountingResourceProvider();
        final ResourceProvider<AtomicInteger> provider = PooledResourceProvider.createInstance(providerDefinition, maxIdleTimeMillis, maxAllocatedResources);
        final Random random = new Random();
        IsAllocatedTester tester = new IsAllocatedTester(provider, providerDefinition, random, false);
        tester.test(maxIdleTimeMillis, maxAllocatedResources);
    }

    @Test
    public void testThreadLimitInPresenceOfExceptions() {
        final int maxAllocatedResources = 3;
        final CountingResourceProvider providerDefinition = new CountingResourceProvider();
        final ResourceProvider<AtomicInteger> provider = PooledResourceProvider.createInstance(providerDefinition, 500, maxAllocatedResources);
        final Random random = new Random();
        ThreadLimitTester tester = new ThreadLimitTester(provider, providerDefinition, maxAllocatedResources, random, true);
        tester.test();
    }

    @Test
    public void testIsAllocatedInPresenceOfExceptions() {
        final int maxAllocatedResources = 3;
        final long maxIdleTimeMillis = 500;
        final CountingResourceProvider providerDefinition = new CountingResourceProvider();
        final ResourceProvider<AtomicInteger> provider = PooledResourceProvider.createInstance(providerDefinition, maxIdleTimeMillis, maxAllocatedResources);
        final Random random = new Random();
        IsAllocatedTester tester = new IsAllocatedTester(provider, providerDefinition, random, true);
        tester.test(maxIdleTimeMillis, maxAllocatedResources);
    }

    @Test
    public void testNotAllocatable() {
        final int maxAllocatedResources = 3;
        final long maxIdleTimeMillis = 500;
        final ResourceProvider<Object> provider = PooledResourceProvider.createInstance(new NotAllocatableResourceProvider(), maxIdleTimeMillis, maxAllocatedResources);
        final Random random = new Random();
        NotAllocatableTester tester = new NotAllocatableTester(provider, random);
        tester.test(maxAllocatedResources);
    }

    private static class IsAllocatedTester implements Runnable {
        private final ResourceProvider<AtomicInteger> provider;
        private final CountingResourceProvider providerDefinition;
        private final Random random;
        private final boolean withExceptions;
        private volatile AssertionError error = null;
        private volatile boolean catchedException = false;

        public IsAllocatedTester(ResourceProvider<AtomicInteger> provider, CountingResourceProvider providerDefinition, Random random, boolean withExceptions) {
            this.provider = provider;
            this.providerDefinition = providerDefinition;
            this.random = random;
            this.withExceptions = withExceptions;
        }

        void test(long maxIdleTimeMillis, int maxAllocatedResources) {
            ExecutorService executor = Executors.newFixedThreadPool(maxAllocatedResources * 10);
            for (int i = 0; i < 100; i++) {
                executor.execute(this);
            }
            executor.shutdown();
            boolean isTerminated = false;
            while (!isTerminated) {
                try {
                    isTerminated = executor.awaitTermination(random.nextInt(200), TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    isTerminated = false;
                }
                if (!isTerminated) {
                    Assert.assertTrue(providerDefinition.count.get() >= 1);
                }
            }
            if (error != null)
                throw error;
            try {
                Thread.sleep(maxIdleTimeMillis + 100);
                Assert.assertTrue(providerDefinition.count.get() == 0);
            } catch (InterruptedException ex) {
                Assert.assertTrue("interrupted", false);
            }
            Assert.assertEquals(withExceptions, catchedException);
        }

        @Override
        public void run() {
            try {
                provider.provideResourceTo(new Consumer<AtomicInteger>() {
                    @Override
                    public void accept(AtomicInteger resource) {
                        try {
                            Assert.assertTrue(providerDefinition.count.get() >= 1);
                            resource.incrementAndGet();
                            Assert.assertTrue(resource.get() == 1);
                            Thread.sleep(random.nextInt(200));
                            Assert.assertTrue(resource.get() == 1);
                            resource.decrementAndGet();
                            Assert.assertTrue(providerDefinition.count.get() >= 1);
                            if (withExceptions)
                                throw new TestConsumerException();
                        } catch (InterruptedException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        } catch (AssertionError ex) {
                            if (error == null)
                                error = ex;
                        }
                    }
                });
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (AssertionError ex) {
                if (error == null)
                    error = ex;
            } catch (TestConsumerException ex) {
                catchedException = true;
            }
        }
    }

    private static class ThreadLimitTester implements Runnable {
        private final ResourceProvider<AtomicInteger> provider;
        private final CountingResourceProvider providerDefinition;
        private final int maxAllocatedResources;
        private final Random random;
        private final boolean withExceptions;
        private volatile AssertionError error;
        private volatile boolean catchedException = false;

        public ThreadLimitTester(ResourceProvider<AtomicInteger> provider, CountingResourceProvider providerDefinition, int maxAllocatedResources, Random random, boolean withExceptions) {
            this.provider = provider;
            this.providerDefinition = providerDefinition;
            this.maxAllocatedResources = maxAllocatedResources;
            this.random = random;
            this.withExceptions = withExceptions;
        }

        void test() {
            ExecutorService executor = Executors.newFixedThreadPool(maxAllocatedResources * 10);
            for (int i = 0; i < 100; i++) {
                executor.execute(this);
                Assert.assertTrue(providerDefinition.count.get() <= maxAllocatedResources);
            }
            executor.shutdown();
            boolean isTerminated = false;
            while (!isTerminated) {
                Assert.assertTrue(providerDefinition.count.get() <= maxAllocatedResources);
                try {
                    isTerminated = executor.awaitTermination(random.nextInt(200), TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    isTerminated = false;
                }
            }
            if (error != null)
                throw error;
            Assert.assertTrue(providerDefinition.count.get() <= maxAllocatedResources);
            Assert.assertEquals(withExceptions, catchedException);
        }

        @Override
        public void run() {
            try {
                provider.provideResourceTo(new Consumer<AtomicInteger>() {
                    @Override
                    public void accept(AtomicInteger resource) {
                        try {
                            Assert.assertTrue(providerDefinition.count.get() <= maxAllocatedResources);
                            resource.incrementAndGet();
                            Assert.assertTrue(resource.get() == 1);
                            Thread.sleep(random.nextInt(200));
                            Assert.assertTrue(resource.get() == 1);
                            resource.decrementAndGet();
                            Assert.assertTrue(providerDefinition.count.get() <= maxAllocatedResources);
                            if (withExceptions)
                                throw new TestConsumerException();
                        } catch (InterruptedException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        } catch (AssertionError ex) {
                            if (error == null)
                                error = ex;
                        }
                    }
                });
            } catch (TestConsumerException ex) {
                catchedException = true;
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (AssertionError ex) {
                if (error == null)
                    error = ex;
            }
        }
    }

    private static class NotAllocatableTester implements Runnable {
        private final ResourceProvider<Object> provider;
        private final Random random;
        private volatile boolean hasRunned = false;
        private volatile boolean hasException = false;

        public NotAllocatableTester(ResourceProvider<Object> provider, Random random) {
            this.provider = provider;
            this.random = random;
        }

        private void test(int maxAllocatedResources) {
            ExecutorService executor = Executors.newFixedThreadPool(maxAllocatedResources * 10);
            for (int i = 0; i < 100; i++) {
                executor.execute(this);
                Assert.assertFalse(hasRunned);
            }
            executor.shutdown();
            boolean isTerminated = false;
            while (!isTerminated) {
                Assert.assertFalse(hasRunned);
                try {
                    isTerminated = executor.awaitTermination(random.nextInt(200), TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    isTerminated = false;
                }
            }
            Assert.assertFalse(hasRunned);
            Assert.assertTrue(hasException);
        }

        @Override
        public void run() {
            try {
                provider.provideResourceTo(new Consumer<Object>() {
                    @Override
                    public void accept(Object value) {
                        hasRunned = true;
                        try {
                            Thread.sleep(random.nextInt(200));
                        } catch (InterruptedException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                });
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (NotAllocatableException ex) {
                hasException = true;
            }
        }
    }

    private static class NotAllocatableResourceProvider implements ResourceProviderDefinition<Object> {

        public NotAllocatableResourceProvider() {
        }

        @Override
        public void provideResourceTo(Consumer<Object> consumer) throws InterruptedException {
            throw new NotAllocatableException();
        }
    }

    private static class CountingResourceProvider implements ResourceProviderDefinition<AtomicInteger> {
        AtomicInteger count = new AtomicInteger(0);

        @Override
        public void provideResourceTo(Consumer<? super AtomicInteger> consumer) throws InterruptedException {
            count.incrementAndGet();
            try {
                consumer.accept(new AtomicInteger(0));
            } finally {
                count.decrementAndGet();
            }
        }
    }

    @SuppressWarnings("serial")
    private static class NotAllocatableException extends RuntimeException {
    }

    @SuppressWarnings("serial")
    private static class TestConsumerException extends RuntimeException {
    }
}
