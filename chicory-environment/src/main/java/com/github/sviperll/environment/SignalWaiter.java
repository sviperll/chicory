/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.environment;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class SignalWaiter {
    private final Object lock = new Object();
    private final SignalWaiterHandler handler = new SignalWaiterHandler();
    private boolean doExit = false;

    public void waitForSignal() {
        synchronized (lock) {
            doExit = false;
            while (!doExit) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public void expect(String signal) {
        Signal.handle(new Signal(signal), handler);
    }

    private class SignalWaiterHandler implements SignalHandler {
        @Override
        public void handle(Signal sig) {
            synchronized (lock) {
                doExit = true;
                lock.notify();
            }
        }
    }
}
