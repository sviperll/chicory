/*
 * Copyright (c) 2013, Victor Nazarov <asviraspossible@gmail.com>
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

import com.github.sviperll.ByteArray;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ByteArrayMap<V> implements Map<ByteArray, V> {
    private final Map<ByteArray, V> map;

    public ByteArrayMap(Map<ByteArray, V> map) {
        this.map = map;
    }

    @Override
    public Set<ByteArray> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<ByteArray, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean containsKey(Object key) {
        if (key instanceof byte[])
            return map.containsKey(new ByteArray((byte[])key));
        else
            return map.containsKey(key);
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public V get(Object key) {
        if (key instanceof byte[])
            return map.get(new ByteArray((byte[])key));
        else
            return map.get(key);
    }

    public V put(byte[] key, V value) {
        return map.put(new ByteArray(key), value);
    }

    @Override
    public V put(ByteArray key, V value) {
        return map.put(key, value);
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public V remove(Object key) {
        if (key instanceof byte[])
            return map.remove(new ByteArray((byte[])key));
        else
            return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends ByteArray, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }
}
