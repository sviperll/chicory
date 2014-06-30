/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
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
     * When TaskDefinition#run method of created task is called Runnable#run method is called in response
     * <p>
     * TaskDefinition#close and TaskDefinition#stop methods of created task does nothong
     *
     * @param runnable runnable to base task on
     * @return new task
     */
    public static Task runnable(Runnable runnable) {
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
    public void stop() {
        task.stop();
    }

    public void join() throws InterruptedException {
        Thread currentThread = null;
        synchronized (lock) {
            currentThread = thread;
        }
        if (currentThread != null)
            currentThread.join();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void run() {
        try {
            task.run();
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
    public void close() {
        task.close();
    }

    /**
     * Returns new task. When run new task calls #run method of this task
     * repeatly with the pause passed as a parameter
     * 
     * @param pause pause between invocations of this task's #run method
     * @param unit TimeUnit to use for pause
     * @return returns new task
     */
    public Task repeat(long pause, TimeUnit unit) {
        return new Task(new RepeatingTask(task, unit.toMillis(pause)));
    }

    /**
     * Returns new task. When run new task calls #run method of this task
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
     * When #run method is called, at first the #run method of this task is called
     * and then the #run method of thatTask is called
     * 
     * @param thatTask task to run after this task
     * @return returns new task
     */
    public Task andThen(TaskDefinition thatTask) {
        return sequence(this.task, thatTask);
    }

    /**
     * Returns new task.
     * When #run method is called, at first the #run method of this task is called
     * and then the #close method of this task is called
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
     * New task is unstoppable, i. e. #stop method of resulting task does nothing
     * 
     * @return returns new task
     */
    public Task unstoppable() {
        return new Task(new UnstoppableTask(task));
    }

    /**
     * Returns new task.
     * New task performs no cleanup, i. e. #close method of resulting task does nothing
     * 
     * @return returns new task
     */
    public Task withoutClose() {
        return new Task(new WithoutCloseTask(task));
    }

    /**
     * Returns new task.
     * New task performs cleanup of this task as it's main work, i. e.
     * when #run method of resulting task is called #close method of this task is called in response
     * 
     * @return returns new task
     */
    public Task closingTask() {
        return new Task(new ClosingTask(task));
    }

    /**
     * Returns new task.
     * New task performs cleanup action in additinal to this task original cleanup, i. e.
     * when #close method of resulting task is called #close method of this task is called in response
     * and than closingAction passed as a parameter is called
     * 
     * @param closingAction additinal closing action
     * @return returns new task
     */
    public Task withAdditinalClosingAction(Runnable closingAction) {
        return new Task(new AdditionalClosingActionTask(task, closingAction));
    }

    public Runnable asRunnable() {
        return new TaskRunnable(task);
    }
}
