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

import com.github.sviperll.DateFormats;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Loggers is a facade-class. With methods to create loggers for common use-patterns.
 */
public class Loggers {

    /**
     * Creates new console logger. Each log-message occupies one line.
     *
     * @return new Logger instance, not null
     */
    public static Logger createConsoleLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        DateFormat dateFormat = DateFormats.ISO8601.createDateFormatInstance();
        java.util.logging.Formatter formatter = new LoggingFormatter(dateFormat);
        java.util.logging.StreamHandler handler = new java.util.logging.StreamHandler(System.out, formatter);
        Thread thread = new Thread(new HandlerFlusher(handler));
        thread.setDaemon(true);
        thread.start();
        handler.setLevel(java.util.logging.Level.ALL);
        logger.addHandler(handler);
        return logger;
    }

    public static Logger withLoggingLevel(Logger baseLogger, java.util.logging.Level level) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setParent(baseLogger);
        logger.setLevel(level);
        return logger;
    }

    public static Logger asynchronous(Logger baseLogger, int queueSize) {
        BlockingQueue<LogRecord> queue = new ArrayBlockingQueue<>(queueSize);
        Logger logger = Logger.getAnonymousLogger();
        logger.setParent(baseLogger);
        QueueWriter writer = new QueueWriter(baseLogger, queue);
        logger.addHandler(writer);
        logger.setUseParentHandlers(false);
        Thread thread = new Thread(writer);
        thread.setDaemon(true);
        thread.start();
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

    private static class LoggingFormatter extends java.util.logging.Formatter {
        private final DateFormat dateFormat;
        public LoggingFormatter(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public String format(java.util.logging.LogRecord record) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            printWriter.append(dateFormat.format(record.getMillis()));
            printWriter.append(" ");
            printWriter.append(record.getLevel().toString());
            if (record.getMessage() != null) {
                printWriter.append(" ");
                printWriter.append(formatMessage(record));
            }
            Throwable thrown = record.getThrown();
            if (thrown != null) {
                printWriter.append(":\n");
                thrown.printStackTrace(printWriter);
            }
            printWriter.append("\n");
            printWriter.flush();
            return stringWriter.toString();
        }
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

    private static class QueueWriter extends Handler implements Runnable {
        private volatile boolean doExit = false;
        private final Logger baseLogger;
        private final BlockingQueue<LogRecord> queue;
        public QueueWriter(Logger baseLogger, BlockingQueue<LogRecord> queue) {
            this.baseLogger = baseLogger;
            this.queue = queue;
        }

        @Override
        public void publish(LogRecord record) {
            for (;;) {
                try {
                    queue.put(record);
                    break;
                } catch (InterruptedException ex) {
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
            doExit = true;
        }

        @Override
        public void run() {
            try {
                while (!doExit) {
                    LogRecord record;
                    try {
                        record = queue.take();
                    } catch (InterruptedException ex) {
                        continue;
                    }
                    baseLogger.log(record);
                }
            } finally {
                LogRecord record;
                while ((record = queue.poll()) != null) {
                    baseLogger.log(record);
                }
            }
        }
    }

    private static class HandlerFlusher implements Runnable {
        private final java.util.logging.Handler handler;
        public HandlerFlusher(java.util.logging.Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                for(;;) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                    handler.flush();
                }
            } finally {
                handler.flush();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
                handler.close();
            }
        }
    }
}
