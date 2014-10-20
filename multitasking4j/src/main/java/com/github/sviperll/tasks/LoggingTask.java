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
 * TaskDefinition that logs when it's methods are called
 */
class LoggingTask implements TaskDefinition {
    private final String name;
    private final Logger logger;
    private final TaskDefinition task;
    
    /**
     * 
     * @param name name to use in log messages
     * @param logger logger to perform logging
     * @param task subtask that does actual work
     */
    public LoggingTask(String name, Logger logger, TaskDefinition task) {
        this.name = name;
        this.logger = logger;
        this.task = task;
    }

    @Override
    public void signalKill() {
        logger.log(Level.FINE, "{0}: exiting...", name);
        task.signalKill();
    }

    @Override
    public void perform() {
        logger.log(Level.FINE, "{0}: started", name);
        task.perform();
        logger.log(Level.FINE, "{0}: finished", name);
    }

    @Override
    public void cleanup() {
        logger.log(Level.FINE, "{0}: closing...", name);
        task.cleanup();
        logger.log(Level.FINE, "{0}: closed", name);
    }
}
