/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.daemon;

import com.github.sviperll.environment.JVM;
import com.github.sviperll.environment.SignalWaiter;
import com.github.sviperll.io.Charsets;
import com.github.sviperll.io.Files;
import com.github.sviperll.logging.Loggers;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Daemon {
    private static final SignalWaiter signalWaiter = new SignalWaiter();
    private static final Object lock = new Object();
    private static boolean isStarted = false;

    public static boolean expectsShutdownNow() {
        return signalWaiter.isReceived();
    }

    public static void waitForShutdown() {
        signalWaiter.waitForSignal();
    }

    public static void run(DaemonLog log, Runnable runnable) throws IOException {
        Daemon daemom = new Daemon(log, runnable);
        daemom.run();
    }

    public static void run(DaemonLog log, Runnable runnable, File pidFile) throws IOException {
        Daemon daemom = new Daemon(log, runnable, pidFile);
        daemom.run();
    }

    private File pidFile = null;
    private final DaemonLog log;
    private final Runnable runnable;
    private Daemon(DaemonLog log, Runnable runnable) {
        this.log = log;
        this.runnable = runnable;
    }

    private Daemon(DaemonLog log, Runnable runnable, File pidFile) {
        this.log = log;
        this.runnable = runnable;
        this.pidFile = pidFile;
    }

    private void run() throws IOException {
        processPidFile();
        Loggers.withRootHandler(log.handlerProvider(), new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized(lock) {
                        if (!isStarted) {
                            System.in.close();
                            System.out.close();
                            System.err.close();
                            signalWaiter.expect("INT");
                            signalWaiter.expect("TERM");
                            isStarted = true;
                        }
                    }
                    runnable.run();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private void processPidFile() throws IOException {
        if (pidFile != null) {
            if (pidFile.exists())
                throw new IllegalStateException("pid file exists: can't start: " + pidFile);
            else {
                Files.write(pidFile, Integer.toString(JVM.getPID()), Charsets.UTF8);
                pidFile.deleteOnExit();
            }
        }
    }

}
