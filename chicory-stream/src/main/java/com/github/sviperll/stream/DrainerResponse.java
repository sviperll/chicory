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
package com.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
abstract class DrainerResponse<T> {
    @SuppressWarnings("rawtypes")
    private static final DrainerResponseFactory FACTORY = new DrainerResponseFactory();

    public static <T> DrainerResponse<T> fetched(T value) {
        return DrainerResponse.<T>factory().fetched(value);
    }

    public static <T> DrainerResponse<T> closed() {
        return DrainerResponse.<T>factory().closed();
    }

    public static <T> DrainerResponse<T> error(RuntimeException exception) {
        return DrainerResponse.<T>factory().error(exception);
    }

    @SuppressWarnings({"unchecked"})
    private static <T> DrainerResponseVisitor<T, DrainerResponse<T>> factory() {
        return FACTORY;
    }

    private DrainerResponse() {
    }

    public abstract <R> R accept(DrainerResponseVisitor<T, R> visitor);

    private static class DrainerResponseFactory<T> implements DrainerResponseVisitor<T, DrainerResponse<T>> {
        @SuppressWarnings("rawtypes")
        private static final DrainerResponse CLOSED = new ClosedDrainerResponse();

        @Override
        public DrainerResponse<T> fetched(final T value) {
            return new FetchedDrainerResponse<>(value);
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public DrainerResponse<T> closed() {
            return CLOSED;
        }

        @Override
        public DrainerResponse<T> error(final RuntimeException exception) {
            return new ErrorDrainerResponse<>(exception);
        }

        private static class FetchedDrainerResponse<T> extends DrainerResponse<T> {

            private final T value;

            FetchedDrainerResponse(T value) {
                this.value = value;
            }

            @Override
            public <R> R accept(DrainerResponseVisitor<T, R> visitor) {
                return visitor.fetched(value);
            }
        }

        private static class ClosedDrainerResponse<T> extends DrainerResponse<T> {

            ClosedDrainerResponse() {
            }

            @Override
            public <R> R accept(DrainerResponseVisitor<T, R> visitor) {
                return visitor.closed();
            }
        }

        private static class ErrorDrainerResponse<T> extends DrainerResponse<T> {

            private final RuntimeException exception;

            ErrorDrainerResponse(RuntimeException exception) {
                this.exception = exception;
            }

            @Override
            public <R> R accept(DrainerResponseVisitor<T, R> visitor) {
                return visitor.error(exception);
            }
        }
    }
}
