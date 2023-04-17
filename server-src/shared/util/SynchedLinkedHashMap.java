/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class SynchedLinkedHashMap<K, V>
extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 9173507152186508958L;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = this.rwl.readLock();
    private final Lock writeLock = this.rwl.writeLock();

    public SynchedLinkedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public SynchedLinkedHashMap() {
    }

    public SynchedLinkedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public SynchedLinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    @Override
    public void clear() {
        this.writeLock.lock();
        try {
            super.clear();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        this.readLock.lock();
        try {
            boolean bl = super.containsKey(key);
            return bl;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        this.readLock.lock();
        try {
            boolean bl = super.containsValue(value);
            return bl;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        this.readLock.lock();
        try {
            Set set = super.entrySet();
            return set;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public V get(Object key) {
        this.readLock.lock();
        try {
            Object v = super.get(key);
            return v;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        this.readLock.lock();
        try {
            Set set = super.keySet();
            return set;
        }
        finally {
            this.readLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public V put(K key, V value) {
        this.writeLock.lock();
        try {
            V v = super.put(key, value);
            return v;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        this.writeLock.lock();
        try {
            super.putAll(map);
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        this.writeLock.lock();
        try {
            Object v = super.remove(key);
            return v;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        this.readLock.lock();
        try {
            Collection collection = super.values();
            return collection;
        }
        finally {
            this.readLock.unlock();
        }
    }
}

