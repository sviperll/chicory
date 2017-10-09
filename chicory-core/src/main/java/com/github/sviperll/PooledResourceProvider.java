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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 */
public class PooledResourceProvider<T> implements ResourceProviderDefinition<T> {

    private static final Logger logger = Logger.getLogger(PooledResourceProvider.class.getName());

    public static <T> ResourceProvider<T> createInstance(
            int maxAllocated,
            long maxIdleTimeMillis,
            ResourceProviderDefinition<T> provider) {

        PooledResourceProvider<T> pooledResourceProvider =
                new PooledResourceProvider<>(maxAllocated, () -> new Worker<>(provider, maxIdleTimeMillis));
        return ResourceProvider.of(pooledResourceProvider);
    }

    private final Deque<Worker<T>> allocatedWorkers = new ArrayDeque<>();
    private final Deque<Worker<T>> unallocatedWorkers = new ArrayDeque<>();
    private final Object lock = new Object();

    private PooledResourceProvider(int maxAllocated, Supplier<Worker<T>> workerFactory) {
        IntStream.range(0, maxAllocated).forEach(i -> unallocatedWorkers.push(workerFactory.get()));
    }

    @Override
    public void provideResourceTo(Consumer<? super T> consumer) {
        Worker<T> worker = null;
        synchronized (lock) {
            for (;;) {
                worker = allocatedWorkers.poll();
                if (worker != null && !worker.isAllocated()) {
                    unallocatedWorkers.push(worker);
                    lock.notifyAll();
                    continue;
                }
                if (worker == null)
                    worker = unallocatedWorkers.poll();
                if (worker != null)
                    break;
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (!worker.isAllocated()) {
            Thread thread = new Thread(worker);
            thread.start();
        }
        try {
            worker.provideResourceTo(consumer);
        } finally {
            synchronized (lock) {
                allocatedWorkers.add(worker);
                lock.notifyAll();
            }
        }
    }

    private static class Worker<T> implements Runnable {
        private final ResourceProviderDefinition<T> provider;
        private final long maxIdleTimeMillis;
        private WorkerState state = WorkerState.UNALLOCATED;
        private T value = null;
        private RuntimeException exception = null;

        private Worker(ResourceProviderDefinition<T> provider, long maxIdleTimeMillis) {
            this.provider = provider;
            this.maxIdleTimeMillis = maxIdleTimeMillis;
        }

        @Override
        public void run() {
            logger.log(Level.FINE, "[Worker {0}]: allocating resource", this);
            try {
                provider.provideResourceTo(this::withValue);
            } catch (RuntimeException ex) {
                synchronized (this) {
                    logger.log(Level.FINE, "[Worker {0}]: switching to error state", this);
                    exception = ex;
                    switchToState(WorkerState.UNCONSUMED_ALLOCATION_ERROR);
                }
            }
            logger.log(Level.FINE, "[Worker {0}]: exiting", this);
        }

        private synchronized void withValue(T value) {
            logger.log(Level.FINE, "[Worker {0}]: switching to allocated state: waiting for consumers", this);
            this.value = value;
            switchToState(WorkerState.UNCONSUMED);

            for (;;) {
                sleepWhileConsumed();
                waitMaximumUnconsumedTime();
                if (state == WorkerState.UNCONSUMED) {
                    logger.log(Level.FINE, "[Worker {0}]: idle for too long: exiting", this);
                    switchToUnallocated();
                    break;
                }
            }
        }


        private synchronized void switchToState(WorkerState state) {
            logger.log(Level.FINE, "[Worker {0}]: switching to {1} state", new Object[]{this, state});
            this.state = state;
            notifyAll();
        }
                
        private synchronized void switchToUnallocated() {
            logger.log(Level.FINE, "[Worker {0}]: switching to uninitialized state", this);
            this.exception = null;
            this.value = null;
            switchToState(WorkerState.UNALLOCATED);
        }

        private synchronized void sleepWhileConsumed() {
            logger.log(Level.FINE, "[Worker {0}]: waiting for ready (IDLE or ERROR) state", this);
            while (state != WorkerState.UNCONSUMED && state != WorkerState.UNCONSUMED_ALLOCATION_ERROR) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private synchronized void waitMaximumUnconsumedTime() {
            logger.log(Level.FINE, "[Worker {0}]: sleeping while idle until maxIdleTimeMillis", this);
            long startTime = System.currentTimeMillis();
            long endTime = startTime + maxIdleTimeMillis;
            long now = System.currentTimeMillis();
            while (state == WorkerState.UNCONSUMED && now < endTime) {
                logger.log(Level.FINE, "[Worker {0}]: sleeping for {1} ms", new Object[]{this, endTime - now});
                try {
                    wait(endTime - now);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                now = System.currentTimeMillis();
            }
        }
        
        void provideResourceTo(Consumer<? super T> consumer) {
            logger.log(Level.FINE, "[Worker {0}]: consumer found: trying to provide resources", this);
            T initializedValue;
            synchronized(this) {
                sleepWhileConsumed();
                if (state == WorkerState.UNCONSUMED_ALLOCATION_ERROR) {
                    logger.log(Level.FINE, "[Worker {0}]: error was found: throwing exception to client", this);
                    RuntimeException exception = this.exception;
                    switchToUnallocated();
                    throw exception;
                }
                initializedValue = value;
                switchToState(WorkerState.CONSUMED);
            }
            try {
                consumer.accept(initializedValue);
            } finally {
                logger.log(Level.FINE, "[Worker {0}]: consumer finished: making thyself available for future requests", this);
                synchronized(this) {
                    switchToState(WorkerState.UNCONSUMED);
                }
            }
        }

        private synchronized boolean isAllocated() {
            return state != WorkerState.UNALLOCATED;
        }
    }

    private enum WorkerState {
        UNALLOCATED, UNCONSUMED, CONSUMED, UNCONSUMED_ALLOCATION_ERROR;
    }
}
