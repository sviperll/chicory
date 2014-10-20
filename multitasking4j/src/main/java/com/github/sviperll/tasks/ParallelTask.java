/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TaskDefinition that runs it's subtasks in parallel
 * Close methods are perform sequentially
 */
public class ParallelTask implements TaskDefinition {
    private final TaskDefinition[] tasks;
    private volatile ThreadManager threadManager = new ThreadManager(Collections.<Thread>emptyList());
    
    /**
     * 
     * @param tasks array of subtasks
     */
    public ParallelTask(TaskDefinition[] tasks) {
        this.tasks = tasks;
    }

    @Override
    public void perform() {
        List<Thread> threads = new ArrayList<Thread>(tasks.length);
        try {
            for (final TaskDefinition task: tasks) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Task.of(task).perform();
                    }
                });
                threads.add(thread);
                thread.start();
            }
        } finally {
            threadManager = new ThreadManager(threads);
            threadManager.join();
        }
    }

    @Override
    public void signalKill() {
        try {
            RuntimeException exception = null;
            for (TaskDefinition task: tasks) {
                try {
                    task.signalKill();
                } catch (RuntimeException ex) {
                    exception = ex;
                }
            }
            if (exception != null)
                throw exception;
        } finally {
            threadManager.interrupt();
        }
    }

    @Override
    public void cleanup() {
        RuntimeException exception = null;
        for (TaskDefinition task: tasks) {
            try {
                task.cleanup();
            } catch (RuntimeException ex) {
                exception = ex;
            }
        }
        if (exception != null)
            throw exception;
    }

    private static class ThreadManager {
        private final List<Thread> threads;

        private ThreadManager(List<Thread> threads) {
            this.threads = threads;
        }

        private void join() {
            for (Thread thread: threads) {
                for (;;) {
                    try {
                        thread.join();
                        break;
                    } catch (InterruptedException ex) {
                        continue;
                    }
                }
            }
        }

        private void interrupt() {
            for (Thread thread: threads) {
                thread.interrupt();
            }
        }
    }
}
