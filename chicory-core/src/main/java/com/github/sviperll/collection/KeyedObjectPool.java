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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Pool of object instances.
 * Instances are created on demand.
 * Created instances are stored in the pool and reused.
 */
public class KeyedObjectPool {
    /**
     * Creates new pool of object instances.
     * Instances are created on demand.
     * Created instances are stored in the pool and reused.
     * Hard retain policy guarantees that the same instance is always return for the same key.
     * Soft and weak policies can return different instances for the same key.
     * With soft and weak policies only one instance for given key can be referenced at any point of time.
     */
    public static <K, V> KeyedObjectFactory<K, V> createInstance(KeyedObjectFactory<K, V> factory, RetainPolicy policy) {
        switch(policy) {
            case HARD:
                return new HardKeyedObjectPool<K, V>(factory);
            case SOFT:
                return new SoftKeyedObjectPool<K, V>(factory);
            case WEAK:
                return new WeakKeyedObjectPool<K, V>(factory);
            default:
                throw new IllegalArgumentException("Unsupported retain policy: " + policy);
        }
    }

    private KeyedObjectPool() {
    }

    /**
      * HardKeyedObjectPool is a simple implementation.
      * HardKeyedObjectPool provides mapping between key and value.
      * HardKeyedObjectPool guarantees that the same instance is always return for the same key.
      * Every keyed oject instance is cached and is never freed.
      */
    private static class HardKeyedObjectPool<K, V> implements KeyedObjectFactory<K, V> {
        private final Map<K, V> map = new HashMap<K, V>();
        private final KeyedObjectFactory<K, V> factory;
        public HardKeyedObjectPool(KeyedObjectFactory<K, V> factory) {
            this.factory = factory;
        }

        @Override
        public V createKeyedInstance(K key) {
            V instance = map.get(key);
            if (instance == null) {
                instance = factory.createKeyedInstance(key);
                map.put(key, instance);
            }
            return instance;
        }
    }

    /**
     * SoftKeyedObjectPool can return different instances for the same key.
     * SoftKeyedObjectPool allowes instances to be garbage collected.
     * Only one instance for given key can be referenced at any point of time.
     * Instances are freed using the "soft" policy of JVM.
     */
    private static class SoftKeyedObjectPool<K, V> implements KeyedObjectFactory<K, V> {
        private final Object lock = new Object();
        private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
        private final Map<K, KeyedSoftReference<K, V>> map = new HashMap<K, KeyedSoftReference<K, V>>();
        private final KeyedObjectFactory<K, V> factory;
        private WeakReference<SoftKeyedObjectPoolGarbageCollector> garbageCollector = new WeakReference<SoftKeyedObjectPoolGarbageCollector>(new SoftKeyedObjectPoolGarbageCollector());
        public SoftKeyedObjectPool(KeyedObjectFactory<K, V> factory) {
            this.factory = factory;
        }

        @Override
        public V createKeyedInstance(K key) {
            garbageCollect();
            synchronized (lock) {
                KeyedSoftReference<K, V> ref = map.get(key);
                V instance;
                if (ref == null) {
                    instance = factory.createKeyedInstance(key);
                    ref = new KeyedSoftReference<K, V>(key, instance, queue);
                    map.put(key, ref);
                } else {
                    instance = ref.get();
                    if (instance == null) {
                        instance = factory.createKeyedInstance(key);
                        ref = new KeyedSoftReference<K, V>(key, instance, queue);
                        map.put(key, ref);
                    }
                }
                return instance;
            }
        }

        @SuppressWarnings("unchecked")
        private void garbageCollect() {
            synchronized (lock) {
                KeyedSoftReference<K, V> ref;
                while ((ref = (KeyedSoftReference<K, V>)queue.poll()) != null) {
                    map.remove(ref.key);
                }
                if (garbageCollector.get() == null)
                    garbageCollector = new WeakReference<SoftKeyedObjectPoolGarbageCollector>(new SoftKeyedObjectPoolGarbageCollector());
            }
        }

        private class SoftKeyedObjectPoolGarbageCollector {
            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                garbageCollect();
            }
        }

        private static class KeyedSoftReference<K, V> extends SoftReference<V> {
            private final K key;

            public KeyedSoftReference(K key, V instance, ReferenceQueue<V> queue) {
                super(instance, queue);
                this.key = key;
            }
        }
    }

    /**
     * WeakKeyedObjectPool can return different instances for the same key.
     * WeakKeyedObjectPool allowes instances to be garbage collected.
     * Only one instance for given key can be referenced at any point of time.
     * Instances are freed using the "weak" policy of JVM.
     */
    private static class WeakKeyedObjectPool<K, V> implements KeyedObjectFactory<K, V> {
        private final Object lock = new Object();
        private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
        private final Map<K, KeyedWeakReference<K, V>> map = new HashMap<K, KeyedWeakReference<K, V>>();
        private final KeyedObjectFactory<K, V> factory;
        private WeakReference<WeakKeyedObjectPoolGarbageCollector> garbageCollector = new WeakReference<WeakKeyedObjectPoolGarbageCollector>(new WeakKeyedObjectPoolGarbageCollector());
        public WeakKeyedObjectPool(KeyedObjectFactory<K, V> factory) {
            this.factory = factory;
        }

        @Override
        public V createKeyedInstance(K key) {
            garbageCollect();
            synchronized (lock) {
                KeyedWeakReference<K, V> ref = map.get(key);
                V instance;
                if (ref == null) {
                    instance = factory.createKeyedInstance(key);
                    ref = new KeyedWeakReference<K, V>(key, instance, queue);
                    map.put(key, ref);
                } else {
                    instance = ref.get();
                    if (instance == null) {
                        instance = factory.createKeyedInstance(key);
                        ref = new KeyedWeakReference<K, V>(key, instance, queue);
                        map.put(key, ref);
                    }
                }
                return instance;
            }
        }

        @SuppressWarnings("unchecked")
        private void garbageCollect() {
            synchronized (lock) {
                KeyedWeakReference<K, V> ref;
                while ((ref = (KeyedWeakReference<K, V>)queue.poll()) != null) {
                    map.remove(ref.key);
                }
                if (garbageCollector.get() == null)
                    garbageCollector = new WeakReference<WeakKeyedObjectPoolGarbageCollector>(new WeakKeyedObjectPoolGarbageCollector());
            }
        }

        private class WeakKeyedObjectPoolGarbageCollector {
            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                garbageCollect();
            }
        }

        private static class KeyedWeakReference<K, V> extends WeakReference<V> {
            private final K key;

            public KeyedWeakReference(K key, V instance, ReferenceQueue<V> queue) {
                super(instance, queue);
                this.key = key;
            }
        }
    }
}
