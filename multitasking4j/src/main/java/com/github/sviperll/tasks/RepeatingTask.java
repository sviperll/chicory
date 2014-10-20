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

/**
 * TaskDefinition that when perform repeatedly calles perform method of it's subtask
 * Given pause is performed between invokations of subtask
 * Stop method breaks repeating cycle
 */
public class RepeatingTask implements TaskDefinition {
    private volatile boolean doExit = false;
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
     * Calls signalKill method of currently running subtask
     * Stop repeating subtask
     */
    @Override
    public void signalKill() {
        doExit = true;
        task.signalKill();
    }

    /**
     * Runs an repeats subtask with given pause between invokations
     */
    @Override
    public void perform() {
        try {
            while (!doExit) {
                task.perform();
                try {
                    Thread.sleep(pause);
                } catch (InterruptedException ex) {
                }
            }
        } finally {
            doExit = false;
        }
    }

    /**
     * Performes subtask cleanup as is, i. e. calls subtask's #signalKill method
     */
    @Override
    public void cleanup() {
        task.cleanup();
    }
}
