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

import com.github.sviperll.ResourceProviderDefinition;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Wrapper that adds convinience methods to any task
 */
public class Task implements TaskDefinition {
    private static final Task DO_NOTHING_TASK = new Task(new DoNothingTask());

    /**
     * @return new task that does nothing
     */
    public static Task doNothing() {
        return DO_NOTHING_TASK;
    }

    /**
     * Creates task from Runnable object
     * <p>
     * When TaskDefinition#perform method of created task is called Runnable#perform method is called in response
     * <p>
     * TaskDefinition#cleanup and TaskDefinition#signalKill methods of created task does nothong
     *
     * @param runnable to base task on
     * @return new task
     */
    public static Task of(Runnable runnable) {
        return new Task(new RunnableTask(runnable));
    }

    /**
     * @param tasks array of subtasks
     * @return new task that performs each of it's subtasks in order
     */
    public static Task sequence(TaskDefinition... tasks) {
        return new Task(new SequenceTask(tasks));
    }

    /**
     * @param tasks list of subtasks
     * @return new task that performs each of it's subtasks in order
     */
    public static Task sequence(List<? extends TaskDefinition> tasks) {
        TaskDefinition[] taskArray = tasks.toArray(new TaskDefinition[tasks.size()]);
        return sequence(taskArray);
    }

    /**
     * @param tasks array of subtasks
     * @return new task that performs all it's subtasks in parallel
     */
    public static Task parallel(TaskDefinition... tasks) {
        return new Task(new ParallelTask(tasks));
    }

    /**
     * @param tasks list of subtasks
     * @return new task that performs all it's subtasks in parallel
     */
    public static Task parallel(List<? extends TaskDefinition> tasks) {
        TaskDefinition[] taskArray = tasks.toArray(new TaskDefinition[tasks.size()]);
        return parallel(taskArray);
    }

    public static Task of(ResourceProviderDefinition<? extends TaskDefinition> source) {
        return new Task(new SourceableResourceTask(source));
    }

    public static Task of(TaskDefinition task) {
        if (task instanceof Task)
            return (Task)task;
        else
            return new Task(task);
    }

    private final TaskDefinition task;
    private final Object lock = new Object();
    private Thread thread = null;
    /**
     * 
     * @param task task to inherit behaviour from
     */
    private Task(TaskDefinition task) {
        this.task = task;
    }

    public void start() {
        synchronized (lock) {
            if (thread == null) {
                thread = new Thread(this.asRunnable());
                thread.start();
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void signalKill() {
        task.signalKill();
    }

    public void join() throws InterruptedException {
        Thread currentThread;
        synchronized (lock) {
            currentThread = thread;
        }
        if (currentThread != null)
            currentThread.join();
    }

    public void interrupt() {
        Thread currentThread;
        synchronized (lock) {
            currentThread = thread;
        }
        if (currentThread != null)
            currentThread.interrupt();
    }

    public void stopTask() throws InterruptedException {
        signalKill();
        Thread currentThread;
        synchronized (lock) {
            currentThread = thread;
        }
        if (currentThread != null) {
            currentThread.interrupt();
            currentThread.join();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void perform() {
        try {
            task.perform();
        } finally {
            synchronized (lock) {
                thread = null;
            }
        }
    }


    /**
     * @inheritDoc
     */
    @Override
    public void cleanup() {
        task.cleanup();
    }

    /**
     * Returns new task. When perform new task calls #perform method of this task
     * repeatly with the pause passed as a parameter
     * 
     * @param pause pause between invocations of this task's #perform method
     * @param unit TimeUnit to use for pause
     * @return returns new task
     */
    public Task repeat(long pause, TimeUnit unit) {
        return new Task(new RepeatingTask(task, unit.toMillis(pause)));
    }

    /**
     * Returns new task. When perform new task calls #perform method of this task
     * repeatly without pauses
     * 
     * @return returns new task
     */
    public Task repeat() {
        return repeat(0, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns new task.
     * When methods of new task are called their invocations are logged by the logger passed as a parameter
     * 
     * @param logger logger that is used to log method invocations
     * @param name name of the task to use in log records
     * @return returns new task
     */
    public Task log(Logger logger, String name) {
        return new Task(new LoggingTask(name, logger, task));
    }

    /**
     * Returns new task.
     * When methods of new task it calls according methods of this task
     * When any exceptions are thrown by the invocation of this task's methods
     * exceptions are not propogated, instead they are logged by given logger
     * and suppressed
     * 
     * @param logger logger that is used to log exceptions
     * @return returns new task
     */
    public Task swallowExceptions(Logger logger, long pause, TimeUnit unit) {
        return new Task(new ExceptionSwallowingTask(task, logger, unit.toMillis(pause)));
    }

    /**
     * Returns new task.
     * When #perform method is called, at first the #perform method of this task is called
     * and then the #perform method of thatTask is called
     * 
     * @param thatTask task to perform after this task
     * @return returns new task
     */
    public Task andThen(TaskDefinition thatTask) {
        return sequence(this.task, thatTask);
    }

    /**
     * Returns new task.
     * When #perform method is called, at first the #perform method of this task is called
     * and then the #cleanup method of this task is called
     * <p>
     * Close method of resulting task does nothing
     * 
     * @return returns new task
     */
    public Task closeAfterEachRun() {
        return new Task(new CloseOnEachRunTask(task));
    }

    /**
     * Returns new task.
     * New task is unstoppable, i. e. #signalKill method of resulting task does nothing
     * 
     * @return returns new task
     */
    public Task unkillable() {
        return new Task(new UnkillableTask(task));
    }

    /**
     * Returns new task.
     * New task performs no cleanup, i. e. #cleanup method of resulting task does nothing
     * 
     * @return returns new task
     */
    public Task withoutCleanup() {
        return new Task(new WithoutCleanupTask(task));
    }

    /**
     * Returns new task.
     * New task performs cleanup of this task as it's main work, i. e.
     * when #perform method of resulting task is called #cleanup method of this task is called in response
     * 
     * @return returns new task
     */
    public Task cleaningUpTask() {
        return new Task(new CleaningUpTask(task));
    }

    /**
     * Returns new task.
     * New task performs cleanup action in additinal to this task original cleanup, i. e.
     * when #cleanup method of resulting task is called #cleanup method of this task is called in response
     * and than closingAction passed as a parameter is called
     * 
     * @param closingAction additinal closing action
     * @return returns new task
     */
    public Task withAdditinalCleanupAction(Runnable closingAction) {
        return new Task(new AdditionalCleanupActionTask(task, closingAction));
    }

    public Runnable asRunnable() {
        return new TaskRunnable(task);
    }
}
