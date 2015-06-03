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
package com.github.sviperll.collection;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Pool of buffers.
 * Buffers are allocated on demand.
 * Created buffers are stored in the pool and reused.
 */
public class ByteBufferPool {
    /**
     * Create a pool of buffers.
     * Buffers are allocated on demand.
     * Created buffers are stored in the pool and reused.
     * With hard retain policy every allocated buffer is cached and is never freed.
     */
    public static ByteBufferAllocator createInstance(int size, RetainPolicy policy) {
        switch(policy) {
            case HARD:
                return new HardByteBufferPool(size);
            case SOFT:
                return new SoftByteBufferPool(size);
            case WEAK:
                return new WeakByteBufferPool(size);
            default:
                throw new IllegalArgumentException("Unsupported retain policy: " + policy);
        }
    }

    private ByteBufferPool() {
    }

    /**
      * HardByteBufferPool is a simple implementation of ByteBufferAllocator.
      * Every allocated buffer is cached and is never freed.
      */
    private static class HardByteBufferPool implements ByteBufferAllocator {
        private final Deque<HardCachedByteBuffer> cache = new ArrayDeque<HardCachedByteBuffer>();
        private final int size;
        HardByteBufferPool(int size) {
            this.size = size;
        }

        @Override
        public ByteBuffer allocateByteBuffer() {
            HardCachedByteBuffer result = pollCache();
            if (result != null)
                return result;
            else
                return new HardCachedByteBuffer(new byte[size]);
        }

        private HardCachedByteBuffer pollCache() {
            synchronized (cache) {
                return cache.pollFirst();
            }
        }

        private void offerCache(HardCachedByteBuffer buffer) {
            synchronized (cache) {
                cache.offerFirst(buffer);
            }
        }

        private class HardCachedByteBuffer implements ByteBuffer {
            private final byte[] array;
            HardCachedByteBuffer(byte[] array) {
                this.array = array;
            }

            @Override
            public byte[] array() {
                return array;
            }

            @Override
            public void free() {
                offerCache(this);
            }
        }
    }

    /**
     * SoftByteBufferPool caches allocated Byte buffers.
     * Byte buffers are freed using the "soft" policy of JVM.
     */
    private static class SoftByteBufferPool implements ByteBufferAllocator {
        private final Deque<SoftReference<SoftCachedByteBuffer>> cache = new ArrayDeque<SoftReference<SoftCachedByteBuffer>>();
        private final int size;
        SoftByteBufferPool(int size) {
            this.size = size;
        }

        @Override
        public ByteBuffer allocateByteBuffer() {
            SoftReference<SoftCachedByteBuffer> ref;
            while ((ref = pollCache()) != null) {
                SoftCachedByteBuffer result = ref.get();
                if (result != null)
                    return result;
            }
            return new SoftCachedByteBuffer(new byte[size]);
        }

        private SoftReference<SoftCachedByteBuffer> pollCache() {
            synchronized (cache) {
                return cache.pollFirst();
            }
        }

        private void offerCache(SoftReference<SoftCachedByteBuffer> ref) {
            synchronized (cache) {
                cache.offerFirst(ref);
            }
        }

        private class SoftCachedByteBuffer implements ByteBuffer {
            private final byte[] array;
            SoftCachedByteBuffer(byte[] array) {
                this.array = array;
            }

            @Override
            public byte[] array() {
                return array;
            }

            @Override
            public void free() {
                offerCache(new SoftReference<SoftCachedByteBuffer>(this));
            }
        }
    }

    /**
     * WeakByteBufferPool caches allocated Byte buffers.
     * Byte buffers are freed using the "weak" policy of JVM.
     */
    private static class WeakByteBufferPool implements ByteBufferAllocator {
        private final Deque<WeakReference<WeakCachedByteBuffer>> cache = new ArrayDeque<WeakReference<WeakCachedByteBuffer>>();
        private final int size;
        WeakByteBufferPool(int size) {
            this.size = size;
        }

        @Override
        public ByteBuffer allocateByteBuffer() {
            WeakReference<WeakCachedByteBuffer> ref;
            while ((ref = pollCache()) != null) {
                WeakCachedByteBuffer result = ref.get();
                if (result != null)
                    return result;
            }
            return new WeakCachedByteBuffer(new byte[size]);
        }

        private WeakReference<WeakCachedByteBuffer> pollCache() {
            synchronized (cache) {
                return cache.pollFirst();
            }
        }

        private void offerCache(WeakReference<WeakCachedByteBuffer> ref) {
            synchronized (cache) {
                cache.offerFirst(ref);
            }
        }

        private class WeakCachedByteBuffer implements ByteBuffer {
            private final byte[] array;
            WeakCachedByteBuffer(byte[] array) {
                this.array = array;
            }

            @Override
            public byte[] array() {
                return array;
            }

            @Override
            public void free() {
                offerCache(new WeakReference<WeakCachedByteBuffer>(this));
            }
        }
    }
}
