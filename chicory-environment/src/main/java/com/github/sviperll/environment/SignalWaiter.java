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
    private boolean isReceived = false;

    public void waitForSignal() {
        synchronized (lock) {
            while (!isReceived) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public boolean isReceived() {
        synchronized (lock) {
            return isReceived;
        }
    }

    public void expect(String signal) {
        Signal.handle(new Signal(signal), handler);
    }

    private class SignalWaiterHandler implements SignalHandler {
        @Override
        public void handle(Signal sig) {
            synchronized (lock) {
                isReceived = true;
                lock.notify();
            }
        }
    }
}
