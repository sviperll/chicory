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
