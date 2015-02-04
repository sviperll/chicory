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
package org.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
abstract class DrainerRequest {
    private static DrainerRequestFactory FACTORY = new DrainerRequestFactory();

    public static DrainerRequestVisitor<DrainerRequest> factory() {
        return FACTORY;
    }
    public static DrainerRequest fetch() {
        return FACTORY.fetch();
    }
    public static DrainerRequest close() {
        return FACTORY.close();
    }

    private DrainerRequest() {
    }

    public abstract <R> R accept(DrainerRequestVisitor<R> visitor);

    private static class DrainerRequestFactory implements DrainerRequestVisitor<DrainerRequest> {
	private static final DrainerRequest FETCH = new FetchDrainerRequest();
	private static final DrainerRequest CLOSE = new CloseDrainerRequest();

        @Override
        public DrainerRequest fetch() {
            return FETCH;
        }

        @Override
        public DrainerRequest close() {
            return CLOSE;
        }

        private static class FetchDrainerRequest extends DrainerRequest {

            public FetchDrainerRequest() {
            }

            @Override
            public <R> R accept(DrainerRequestVisitor<R> visitor) {
                return visitor.fetch();
            }
        }

        private static class CloseDrainerRequest extends DrainerRequest {

            public CloseDrainerRequest() {
            }

            @Override
            public <R> R accept(DrainerRequestVisitor<R> visitor) {
                return visitor.close();
            }
        }
    }
}
