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

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 */
public class PooledResourceProvider<T> implements ResourceProviderDefinition<T> {
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
        private WorkerState state = WorkerState.UNINITIALIZED;
        private final long workerID;

        Worker(long workerID) {
            this.workerID = workerID;
        }

        @Override
        public void run() {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: allocating resource");
            try {
                provider.provideResourceTo(this::withValue);
            } catch (RuntimeException ex) {
                switchToError(ex);
            }
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: exiting");
        }

        private synchronized void withValue(T value) {
            System.out.println("[" + LocalDateTime.now() + "]" + Thread.currentThread().getId() + "][Worker " + workerID + "]: resource allocating waiting for consumers");
            switchToInitialized(value);

            for (;;) {
                sleepDuringActualJob();
                waitMaximumIdleTime();
                if (state == WorkerState.IDLE) {
                    System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: idle for too long: exiting");
                    switchToUninitialized();
                    workers.unregister(workerID);
                    return;
                }
            }
        }

        private synchronized void switchToError(RuntimeException ex) {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: switching to error state");
            exception = ex;
            switchToState(WorkerState.ERROR);
        }

        private synchronized void switchToState(WorkerState state) {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: switching to " + state + " state");
            this.state = state;
            notifyAll();
        }
        
        private synchronized void switchToInitialized(T value) {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: switching to initialized state");
            this.value = value;
            switchToState(WorkerState.IDLE);
        }
        
        private synchronized void switchToUninitialized() {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: switching to uninitialized state");
            this.value = null;
            switchToState(WorkerState.UNINITIALIZED);
        }

        private synchronized void sleepDuringActualJob() {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: waiting for ready (IDLE or ERROR) state");
            while (state != WorkerState.IDLE && state != WorkerState.ERROR) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private synchronized void waitMaximumIdleTime() {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: sleeping while idle until maxIdleTimeMillis");
            long startTime = System.currentTimeMillis();
            long endTime = startTime + maxIdleTimeMillis;
            long now = System.currentTimeMillis();
            while (state == WorkerState.IDLE && now < endTime) {
                System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: sleeping for " + (endTime - now) + " ms");
                try {
                    wait(endTime - now);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                now = System.currentTimeMillis();
            }
        }
        
        void provideResourceTo(Consumer<? super T> consumer) {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: consumer found: trying to provide resources");
            T initializedValue;
            synchronized(this) {
                sleepDuringActualJob();
                if (state == WorkerState.ERROR) {
                    System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: error was found: throwing exception to client");
                    workers.unregister(workerID);
                    throw exception;
                }
                initializedValue = value;
                switchToState(WorkerState.WORKING);
            }
            try {
                consumer.accept(initializedValue);
            } finally {
                System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][Worker " + workerID + "]: consumer finished: making thyself available for future requests");
                synchronized(this) {
                    workers.enqueue(workerID);
                    switchToState(WorkerState.IDLE);
                }
            }
        }
    }

    private enum WorkerState {
        UNINITIALIZED, IDLE, WORKING, ERROR;
    }

    private static class WorkerCollection<W> {
        private Map<Long, W> workerMap = new HashMap<>();
        private Deque<Long> idleQueue = new ArrayDeque<>();
        private long nextWorkerID = 1;

        private synchronized W put(LongFunction<W> factory) {
            long id = nextWorkerID++;
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][POOL]: allocating new worker: Worker " + id);
            W worker = factory.apply(id);
            workerMap.put(id, worker);
            return worker;
        }

        private synchronized void unregister(long workerID) {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][POOL]: removing worker: Worker " + workerID);
            workerMap.remove(workerID);
            notifyAll();
        }

        private synchronized void enqueue(long workerID) {
            System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][POOL]: making worker available for future requests: Worker " + workerID);
            idleQueue.addLast(workerID);
            notifyAll();
        }

        private synchronized W poll() {
            Long workerID = idleQueue.pollLast();
            while (workerID != null) {
                W worker = workerMap.get(workerID);
                if (worker != null) {
                    System.out.println("[" + LocalDateTime.now() + "][" + Thread.currentThread().getId() + "][POOL]: found idle worker: Worker " + workerID);
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
