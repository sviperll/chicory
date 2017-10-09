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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 */
public class PooledResourceProvider<T> implements ResourceProviderDefinition<T> {

    private static final Logger logger = Logger.getLogger(PooledResourceProvider.class.getName());

    public static <T> ResourceProvider<T> createInstance(
            ResourceProviderDefinition<T> provider,
            long maxIdleTimeMillis,
            int maxAllocated) {

        PooledResourceProvider<T> pooledResourceProvider = new PooledResourceProvider<>(provider, maxIdleTimeMillis, maxAllocated);
        return ResourceProvider.of(pooledResourceProvider);
    }

    private final ResourceProviderDefinition<T> provider;
    private final long maxIdleTimeMillis;
    private final int maxAllocated;
    private final WorkerCollection<Worker> workers = new WorkerCollection<>();

    private PooledResourceProvider(ResourceProviderDefinition<T> provider, long maxIdleTimeMillis, int maxAllocated) {
        this.provider = provider;
        this.maxIdleTimeMillis = maxIdleTimeMillis;
        this.maxAllocated = maxAllocated;
    }

    @Override
    public void provideResourceTo(Consumer<? super T> consumer) {
        Worker worker = null;
        synchronized (workers) {
            while (worker == null) {
                worker = workers.poll();
                if (worker == null) {
                    if (workers.size() >= maxAllocated) {
                        try {
                            workers.wait();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        worker = workers.put(workerID -> new Worker(workerID));
                        Thread thread = new Thread(worker);
                        thread.start();
                    }
                }
            }
        }
        worker.provideResourceTo(consumer);
    }

    private class Worker implements Runnable {
        private T value = null;
        private RuntimeException exception = null;
        private WorkerState state = WorkerState.UNALLOCATED;
        private final long workerID;

        Worker(long workerID) {
            this.workerID = workerID;
        }

        @Override
        public void run() {
            logger.log(Level.FINE, "[Worker {0}]: allocating resource", workerID);
            try {
                provider.provideResourceTo(this::withValue);
            } catch (RuntimeException ex) {
                switchToError(ex);
            }
            logger.log(Level.FINE, "[Worker {0}]: exiting", workerID);
        }

        private synchronized void withValue(T value) {
            logger.log(Level.FINE, "[Worker {0}]: resource allocating waiting for consumers", workerID);
            switchToInitialized(value);

            for (;;) {
                sleepDuringActualJob();
                waitMaximumIdleTime();
                if (state == WorkerState.IDLE) {
                    logger.log(Level.FINE, "[Worker {0}]: idle for too long: exiting", workerID);
                    switchToUninitialized();
                    workers.unregister(workerID);
                    return;
                }
            }
        }

        private synchronized void switchToError(RuntimeException ex) {
            logger.log(Level.FINE, "[Worker {0}]: switching to error state", workerID);
            exception = ex;
            switchToState(WorkerState.SHOULD_THROW);
        }

        private synchronized void switchToState(WorkerState state) {
            logger.log(Level.FINE, "[Worker {0}]: switching to {1} state", new Object[]{workerID, state});
            this.state = state;
            notifyAll();
        }
        
        private synchronized void switchToInitialized(T value) {
            logger.log(Level.FINE, "[Worker {0}]: switching to initialized state", workerID);
            this.value = value;
            switchToState(WorkerState.IDLE);
        }
        
        private synchronized void switchToUninitialized() {
            logger.log(Level.FINE, "[Worker {0}]: switching to uninitialized state", workerID);
            this.value = null;
            switchToState(WorkerState.UNALLOCATED);
        }

        private synchronized void sleepDuringActualJob() {
            logger.log(Level.FINE, "[Worker {0}]: waiting for ready (IDLE or ERROR) state", workerID);
            while (state != WorkerState.IDLE && state != WorkerState.SHOULD_THROW) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private synchronized void waitMaximumIdleTime() {
            logger.log(Level.FINE, "[Worker {0}]: sleeping while idle until maxIdleTimeMillis", workerID);
            long startTime = System.currentTimeMillis();
            long endTime = startTime + maxIdleTimeMillis;
            long now = System.currentTimeMillis();
            while (state == WorkerState.IDLE && now < endTime) {
                logger.log(Level.FINE, "[Worker {0}]: sleeping for {1} ms", new Object[]{workerID, endTime - now});
                try {
                    wait(endTime - now);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                now = System.currentTimeMillis();
            }
        }
        
        void provideResourceTo(Consumer<? super T> consumer) {
            logger.log(Level.FINE, "[Worker {0}]: consumer found: trying to provide resources", workerID);
            T initializedValue;
            synchronized(this) {
                sleepDuringActualJob();
                if (state == WorkerState.SHOULD_THROW) {
                    logger.log(Level.FINE, "[Worker {0}]: error was found: throwing exception to client", workerID);
                    workers.unregister(workerID);
                    throw exception;
                }
                initializedValue = value;
                switchToState(WorkerState.WORKING);
            }
            try {
                consumer.accept(initializedValue);
            } finally {
                logger.log(Level.FINE, "[Worker {0}]: consumer finished: making thyself available for future requests", workerID);
                synchronized(this) {
                    workers.enqueue(workerID);
                    switchToState(WorkerState.IDLE);
                }
            }
        }
    }

    private enum WorkerState {
        UNALLOCATED, IDLE, WORKING, SHOULD_THROW;
    }

    private static class WorkerCollection<W> {
        private final Map<Long, W> workerMap = new HashMap<>();
        private final Deque<Long> idleQueue = new ArrayDeque<>();
        private long nextWorkerID = 1;

        private synchronized W put(LongFunction<W> factory) {
            long id = nextWorkerID++;
            logger.log(Level.FINE, "[POOL]: allocating new worker: Worker {0}", id);
            W worker = factory.apply(id);
            workerMap.put(id, worker);
            return worker;
        }

        private synchronized void unregister(long workerID) {
            logger.log(Level.FINE, "[POOL]: removing worker: Worker {0}", workerID);
            workerMap.remove(workerID);
            notifyAll();
        }

        private synchronized void enqueue(long workerID) {
            logger.log(Level.FINE, "[POOL]: making worker available for future requests: Worker {0}", workerID);
            idleQueue.addLast(workerID);
            notifyAll();
        }

        private synchronized W poll() {
            Long workerID = idleQueue.pollLast();
            while (workerID != null) {
                W worker = workerMap.get(workerID);
                if (worker != null) {
                    logger.log(Level.FINE, "[POOL]: found idle worker: Worker {0}", workerID);
                    return worker;
                }
                workerID = idleQueue.pollLast();
            }
            return null;
        }

        private synchronized int size() {
            return workerMap.size();
        }
    }
}
