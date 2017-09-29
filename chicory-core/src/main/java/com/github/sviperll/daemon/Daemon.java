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
package com.github.sviperll.daemon;

import com.github.sviperll.environment.JVM;
import com.github.sviperll.environment.SignalWaiter;
import com.github.sviperll.io.Charsets;
import com.github.sviperll.io.Files;
import com.github.sviperll.logging.Loggers;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
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

    public static void run(Runnable runnable, DaemonLog log) throws IOException {
        Daemon daemom = new Daemon(runnable, log);
        daemom.run();
    }

    public static void run(Runnable runnable, DaemonLog log, File pidFile) throws IOException {
        Daemon daemom = new Daemon(runnable, log, pidFile);
        daemom.run();
    }

    private final File pidFile;
    private final DaemonLog log;
    private final Runnable runnable;
    private Daemon(Runnable runnable, DaemonLog log) {
        this.log = log;
        this.runnable = runnable;
        this.pidFile = null;
    }

    private Daemon(Runnable runnable, DaemonLog log, File pidFile) {
        this.log = log;
        this.runnable = runnable;
        this.pidFile = pidFile;
    }

    private void run() throws IOException {
        processPidFile();
        Runnable startingRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized(lock) {
                        if (!isStarted) {
                            System.in.close();
                            if (log.closesStandardOut())
                                System.out.close();
                            if (log.closesStandardErr())
                                System.err.close();
                            signalWaiter.expect("INT");
                            signalWaiter.expect("TERM");
                            isStarted = true;
                        }
                    }
                    runnable.run();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
        try {
            Loggers.withRootHandler(log.handlerProvider(), startingRunnable);
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
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
