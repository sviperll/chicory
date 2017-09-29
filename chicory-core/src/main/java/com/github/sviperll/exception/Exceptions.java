/*
 * Copyright (c) 2017, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.sviperll.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class Exceptions {

    public static <X extends Throwable> void loopHole(Class<X> exceptionKlass, Consumer<LoopHole<X>> withLoopHole) throws X {
        LoopHole<X> loopHole = new LoopHole<>(exceptionKlass);
        loopHole.run(withLoopHole);
    }

    public static String render(Throwable exception) {
        StringWriter message = new StringWriter();
        PrintWriter writer = new PrintWriter(message);
        exception.printStackTrace(writer);
        writer.flush();
        return message.toString();
    }

    private Exceptions() {
    }

    public static class LoopHole<X extends Throwable> {
        private final Class<X> exceptionKlass;

        LoopHole(Class<X> exceptionKlass) {
            this.exceptionKlass = exceptionKlass;
        }

        public <T> T throwUpThrough(X exception) {
            throw new LoopHoleException(this, exception);
        }

        public <T, Y extends X> Supplier<T> supplierWithExceptionsPassingUpThrough(ExceptionfulSupplier<T, Y> supplier) {
            return () -> {
                try {
                    return supplier.get();
                } catch (Throwable ex) {
                    if (exceptionKlass.isInstance(ex)) {
                        return throwUpThrough(exceptionKlass.cast(ex));
                    } else if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    } else if (ex instanceof Error) {
                        throw (Error) ex;
                    } else {
                        throw new IllegalStateException("Undeclared checked exception!", ex);
                    }
                }
            };
        }

        public <Y extends X> Runnable runnableWithExceptionsPassingUpThrough(ExceptionfulRunnable<Y> runnable) {
            ExceptionfulSupplier<?, Y> supplier = () -> {
                runnable.run();
                return null;
            };
            return () -> supplierWithExceptionsPassingUpThrough(supplier).get();
        }

        public <T, R, Y extends X> Function<T, R> functionWithExceptionsPassingUpThrough(ExceptionfulFunction<T, R, Y> function) {
            return argument -> {
                ExceptionfulSupplier<R, Y> supplier = () -> function.apply(argument);
                return supplierWithExceptionsPassingUpThrough(supplier).get();
            };
        }

        public <T, Y extends X> Consumer<T> consumerWithExceptionsPassingUpThrough(ExceptionfulConsumer<T, Y> consumer) {
            return (argument) -> {
                ExceptionfulRunnable<Y> runnable = () -> consumer.accept(argument);
                runnableWithExceptionsPassingUpThrough(runnable).run();
            };
        }

        private void run(Consumer<LoopHole<X>> withLoopHole) throws X {
            try {
                withLoopHole.accept(this);
            } catch (LoopHoleException ex) {
                Throwable cause = ex.getCause();
                if (ex.loopHole() == this && exceptionKlass.isInstance(cause)) {
                    throw exceptionKlass.cast(cause);
                } else {
                    throw ex;
                }
            }
        }

        @SuppressWarnings("serial")
        private static class LoopHoleException extends RuntimeException {

            private final LoopHole<?> loopHole;

            LoopHoleException(LoopHole<?> loopHole, Throwable exception) {
                super(exception);
                this.loopHole = loopHole;
            }

            LoopHole<?> loopHole() {
                return loopHole;
            }
        }
    }
}
