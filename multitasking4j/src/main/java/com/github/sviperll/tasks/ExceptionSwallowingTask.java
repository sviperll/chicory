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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TaskDefinition that catches all exceptions of the original task, logges them and then just suppresses them
 */
class ExceptionSwallowingTask implements TaskDefinition {
    private final TaskDefinition task;
    private final Logger logger;
    private final long pause;

    /**
     * 
     * @param task subtask to perform actual work
     * @param logger logger used to log exceptions
     * @param pause pause after exception
     */
    public ExceptionSwallowingTask(TaskDefinition task, Logger logger, long pause) {
        this.task = task;
        this.logger = logger;
        this.pause = pause;
    }

    @Override
    public void signalKill() {
        try {
            task.signalKill();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException ex1) {
            }
        }
    }

    @Override
    public void perform() {
        try {
            task.perform();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException ex1) {
            }
        }
    }

    @Override
    public void cleanup() {
        try {
            task.cleanup();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException ex1) {
            }
        }
    }
}
