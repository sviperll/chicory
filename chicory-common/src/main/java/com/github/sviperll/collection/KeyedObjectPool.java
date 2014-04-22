/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
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
                return new HardKeyedObjectPool<>(factory);
            case SOFT:
                return new SoftKeyedObjectPool<>(factory);
            case WEAK:
                return new WeakKeyedObjectPool<>(factory);
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
        private final Map<K, V> map = new HashMap<>();
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
        private final ReferenceQueue<V> queue = new ReferenceQueue<>();
        private final Map<K, KeyedSoftReference<K, V>> map = new HashMap<>();
        private final KeyedObjectFactory<K, V> factory;
        private WeakReference<SoftKeyedObjectPoolGarbageCollector> garbageCollector = new WeakReference<>(new SoftKeyedObjectPoolGarbageCollector());
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
                    ref = new KeyedSoftReference<>(key, instance, queue);
                    map.put(key, ref);
                } else {
                    instance = ref.get();
                    if (instance == null) {
                        instance = factory.createKeyedInstance(key);
                        ref = new KeyedSoftReference<>(key, instance, queue);
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
                    garbageCollector = new WeakReference<>(new SoftKeyedObjectPoolGarbageCollector());
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
        private final ReferenceQueue<V> queue = new ReferenceQueue<>();
        private final Map<K, KeyedWeakReference<K, V>> map = new HashMap<>();
        private final KeyedObjectFactory<K, V> factory;
        private WeakReference<WeakKeyedObjectPoolGarbageCollector> garbageCollector = new WeakReference<>(new WeakKeyedObjectPoolGarbageCollector());
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
                    ref = new KeyedWeakReference<>(key, instance, queue);
                    map.put(key, ref);
                } else {
                    instance = ref.get();
                    if (instance == null) {
                        instance = factory.createKeyedInstance(key);
                        ref = new KeyedWeakReference<>(key, instance, queue);
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
                    garbageCollector = new WeakReference<>(new WeakKeyedObjectPoolGarbageCollector());
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
