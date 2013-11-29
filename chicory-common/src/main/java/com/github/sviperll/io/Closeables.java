/*
 * Copyright (c) 2012, Victor Nazarov <asviraspossible@gmail.com>
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
package com.github.sviperll.io;

import java.io.Closeable;
import java.io.IOException;

public class Closeables {
    private static final Closeable EMPTY = new EmptyCloseable();

    public static Closeable runnable(final Runnable action) {
        return new Closeable() {
            @Override
            public void close() {
                action.run();
            }
        };
    }

    /**
     * Cobine two closeable resources into one. When resulting resource is closed
     * both original resources are closed.
     *
     * @param closeable1
     * @param closeable2
     * @return new closable resource
     */
    public static Closeable combine(final Closeable closeable1, final Closeable closeable2) {
        return new Closeable() {
            @Override
            public void close() throws IOException {
                try {
                    closeable1.close();
                } finally {
                    closeable2.close();
                }
            }
        };
    }

    /**
     * Cobine three closeable resources into one. When resulting resource is closed
     * both original resources are closed.
     *
     * @param closeable1
     * @param closeable2
     * @return new closable resource
     */
    public static Closeable combine(final Closeable closeable1, final Closeable closeable2, final Closeable closeable3) {
        return new Closeable() {
            @Override
            public void close() throws IOException {
                try {
                    closeable1.close();
                } finally {
                    try {
                        closeable2.close();
                    } finally {
                        closeable3.close();
                    }
                }
            }
        };
    }

    /**
     * Cobine collection of closeable resources into one. When resulting resource is closed
     * all closable resources from collection are closed.
     *
     * @param closeables collection of closeable resources
     * @return new closable resource
     */
    public static Closeable join(Iterable<? extends Closeable> closeables) {
        return new JoinedCloseable(closeables);
    }

    public static Closeable empty() {
        return EMPTY;
    }

    private Closeables() {}

    private static class EmptyCloseable implements Closeable {
        @Override
        public void close() {
        }
    }
}
