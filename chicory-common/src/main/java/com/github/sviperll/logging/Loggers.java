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
import com.github.sviperll.DateFormats;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Loggers is a facade-class. With methods to create loggers for common use-patterns.
 */
public class Loggers {

    public static java.util.logging.Formatter createDefaultFormatter() {
        DateFormat dateFormat = DateFormats.ISO8601.createDateFormatInstance();
        return new LoggingFormatter(dateFormat);
    }

    public static java.util.logging.Handler createFlushingHandler(OutputStream stream) {
        return createFlushingHandler(stream, createDefaultFormatter());
    }

    public static java.util.logging.Handler createFlushingHandler(OutputStream stream, java.util.logging.Formatter formatter) {
        FlushingHandler handler = new FlushingHandler(stream, formatter);
        return handler;
    }

    public static java.util.logging.Handler createPeriodicallyFlushingHandler(OutputStream stream, long time, TimeUnit unit) {
        return createPeriodicallyFlushingHandler(stream, time, unit, createDefaultFormatter());
    }

    public static java.util.logging.Handler createPeriodicallyFlushingHandler(OutputStream stream, long time, TimeUnit unit, java.util.logging.Formatter formatter) {
        PeriodicallyFlushingHandler handler = new PeriodicallyFlushingHandler(stream, time, unit, formatter);
        Thread thread = new Thread(handler);
        thread.setDaemon(true);
        thread.start();
        return handler;
    }

    public static java.util.logging.Handler createAsynchronousHandler(java.util.logging.Handler handler, int queueSize) {
        BlockingQueue<LogRecord> queue = new ArrayBlockingQueue<>(queueSize);
        AsynchronousHandler aynchrohous = new AsynchronousHandler(handler, queue);
        Thread thread = new Thread(aynchrohous);
        thread.setDaemon(true);
        thread.start();
        return aynchrohous;
    }

    public static void withRootHandler(Handler handler, Runnable action) {
        Logger logger = Logger.getLogger("");
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

    public static void withRootHandler(HandlerProvider handlerProvider, final Runnable action) {
        handlerProvider.provideHandler(new Consumer<Handler>() {
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
        logger.addHandler(createFlushingHandler(System.out));
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

    private static class FlushingHandler extends StreamHandler {
        public FlushingHandler(OutputStream stream, java.util.logging.Formatter formatter) {
            super(stream, formatter);
            setLevel(Level.ALL);
        }

        @Override
        public void publish(LogRecord record) {
            super.publish(record);
            super.flush();
        }
    }

    private static class PeriodicallyFlushingHandler extends StreamHandler implements Runnable {
        private volatile boolean doExit = false;
        private final long time;
        private final TimeUnit unit;
        public PeriodicallyFlushingHandler(OutputStream stream, long time, TimeUnit unit, Formatter formatter) {
            super(stream, formatter);
            setLevel(Level.ALL);
            this.time = time;
            this.unit = unit;
        }

        @Override
        public void run() {
            try {
                while (!doExit) {
                    try {
                        Thread.sleep(unit.toMillis(time));
                    } catch (InterruptedException ex) {
                    }
                    super.flush();
                }
            } finally {
                doExit = false;
            }
        }

        @Override
        public void close() {
            doExit = true;
            super.close();
        }
    }

    private static class AsynchronousHandler extends Handler implements Runnable {
        private static final LogRecord EXIT = new LogRecord(Level.OFF, null);
        private volatile boolean doExit = false;
        private volatile boolean isRunning = false;
        private final Handler handler;
        private final BlockingQueue<LogRecord> queue;
        private final BlockingQueue<Boolean> exitQueue = new SynchronousQueue<>();

        public AsynchronousHandler(Handler handler, BlockingQueue<LogRecord> queue) {
            this.handler = handler;
            this.queue = queue;
        }

        @Override
        public void publish(LogRecord record) {
            for (;;) {
                try {
                    queue.put(record);
                    break;
                } catch (InterruptedException ex) {
                    continue;
                }
            }
        }

        @Override
        public void run() {
            isRunning = true;
            try {
                for (;;) {
                    LogRecord record;
                    try {
                        record = queue.take();
                    } catch (InterruptedException ex) {
                        continue;
                    }
                    if (record == EXIT)
                        break;
                    handler.publish(record);
                }
            } finally {
                isRunning = false;
                for (;;) {
                    try {
                        exitQueue.put(true);
                        break;
                    } catch (InterruptedException ex) {
                        continue;
                    }
                }
            }
        }

        @Override
        public void flush() {
            handler.flush();
        }

        @Override
        public void close() throws SecurityException {
            if (!doExit && isRunning) {
                doExit = true;
                publish(EXIT);
                for (;;) {
                    try {
                        exitQueue.take();
                        break;
                    } catch (InterruptedException ex) {
                        continue;
                    }
                }
                doExit = false;
            }
            LogRecord record;
            while ((record = queue.poll()) != null) {
                handler.publish(record);
            }
            handler.close();
        }
    }
}
