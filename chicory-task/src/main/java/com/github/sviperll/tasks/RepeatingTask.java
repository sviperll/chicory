/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

/**
 * TaskDefinition that when run repeatedly calles run method of it's subtask
 * Given pause is performed between invokations of subtask
 * Stop method breaks repeating cycle
 */
public class RepeatingTask implements TaskDefinition {
    private final Object lock = new Object();
    private boolean doExit = false;
    private final TaskDefinition task;
    private final long pause;

    /**
     * 
     * @param task subtask
     * @param pause pause between subtask invokations in milliseconds
     */
    public RepeatingTask(TaskDefinition task, long pause) {
        this.task = task;
        this.pause = pause;
    }

    /**
     * Calls stop method of currently running subtask
     * Stop repeating subtask
     */
    @Override
    public void stop() {
        synchronized (lock) {
            doExit = true;
        }
        task.stop();
    }

    /**
     * Runs an repeats subtask with given pause between invokations
     */
    @Override
    public void run() {
        for (;;) {
            synchronized (lock) {
                if (doExit) {
                    doExit = false;
                    break;
                }
            }
            task.run();
            try {
                Thread.sleep(pause);
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * Performes subtask cleanup as is, i. e. calls subtask's #stop method
     */
    @Override
    public void close() {
        task.close();
    }
}
