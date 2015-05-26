/*
 * Copyright (c) 2013, Victor Nazarov <asviraspossible@gmail.com>
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

package com.github.sviperll.logging;

import com.github.sviperll.Consumer;
import com.github.sviperll.ResourceProviderDefinition;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Loggers is a facade-class. With methods to create loggers for common use-patterns.
 */
public class Loggers {
    public static Logger getRootLogger() {
        return Logger.getLogger("");
    }

    public static void withRootHandler(Handler handler, Runnable action) {
        Logger logger = getRootLogger();
        Logger parentLogger = logger.getParent();
        while (parentLogger != null) {
            logger = parentLogger;
            parentLogger = logger.getParent();
        }
        Handler[] handlers = logger.getHandlers();
        handlers = Arrays.copyOf(handlers, handlers.length, Handler[].class);
        for (Handler registred: handlers) {
            logger.removeHandler(registred);
        }
        logger.addHandler(handler);
        action.run();
        logger.removeHandler(handler);
        for (Handler registred: handlers) {
            logger.addHandler(registred);
        }
    }

    public static void withRootHandler(ResourceProviderDefinition<? extends Handler> source, final Runnable action) throws InterruptedException {
        source.provideResourceTo(new Consumer<Handler>() {
            @Override
            public void accept(Handler handler) {
                withRootHandler(handler, action);
            }
        });
    }

    public static Logger createConsoleAnonymousLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(Handlers.createFlushingHandler(System.out));
        return logger;
    }

    public static Logger createAnonymousLogger(java.util.logging.Handler handler) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        return logger;
    }

    public static Logger withLoggingLevel(Logger baseLogger, java.util.logging.Level level) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setParent(baseLogger);
        logger.setLevel(level);
        return logger;
    }

    public static Logger prefixed(Logger baseLogger, String prefix) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setParent(baseLogger);
        java.util.logging.Handler handler = new PrefixingHandler(baseLogger, escapeMessage(prefix));
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        return logger;
    }

    private static String escapeMessage(String prefix) {
        return prefix.replace("'", "''").replace("{", "'{'").replace("}", "'}'");
    }

    private Loggers() {
    }

    private static class PrefixingHandler extends java.util.logging.Handler {
        private final Logger baseLogger;
        private final String escapedPrefix;

        private PrefixingHandler(Logger baseLogger, String escapedPrefix) {
            this.baseLogger = baseLogger;
            this.escapedPrefix = escapedPrefix;
        }

        @Override
        public void publish(LogRecord record) {
            String message = escapedPrefix + record.getMessage();
            LogRecord processedRecord = new LogRecord(record.getLevel(), message);
            processedRecord.setLoggerName(record.getLoggerName());
            processedRecord.setMillis(record.getMillis());
            processedRecord.setParameters(record.getParameters());
            processedRecord.setResourceBundle(record.getResourceBundle());
            processedRecord.setResourceBundleName(record.getResourceBundleName());
            processedRecord.setSequenceNumber(record.getSequenceNumber());
            processedRecord.setSourceClassName(record.getSourceClassName());
            processedRecord.setSourceMethodName(record.getSourceMethodName());
            processedRecord.setThreadID(record.getThreadID());
            processedRecord.setThrown(record.getThrown());

            baseLogger.log(processedRecord);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
