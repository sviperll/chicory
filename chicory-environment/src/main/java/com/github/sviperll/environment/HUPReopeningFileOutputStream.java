/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class HUPReopeningFileOutputStream extends OutputStream {
    private final static Logger logger = Logger.getLogger(HUPReopeningFileOutputStream.class.getName());
    private final Object lock = new Object();
    private OutputStream outputStream;
    public HUPReopeningFileOutputStream(final File file) throws FileNotFoundException {
        this.outputStream = new FileOutputStream(file, true);
        Signal.handle(new Signal("HUP"), new SignalHandler() {
            @Override
            public void handle(Signal sig) {
                synchronized(lock) {
                    try {
                        outputStream.close();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    try {
                        outputStream = new FileOutputStream(file, true);
                    } catch (FileNotFoundException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    @Override
    public void write(int b) throws IOException {
        synchronized(lock) {
            outputStream.write(b);
        }
    }
    @Override
    public void write(byte[] b) throws IOException {
        synchronized(lock) {
            outputStream.write(b);
        }
    }
    @Override
    public void write(byte[] b, int offset, int len) throws IOException {
        synchronized(lock) {
            outputStream.write(b, offset, len);
        }
    }
    @Override
    public void flush() throws IOException {
        synchronized(lock) {
            outputStream.flush();
        }
    }
    @Override
    public void close() throws IOException {
        synchronized(lock) {
            outputStream.close();
        }
    }
}
