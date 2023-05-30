package com.wurmonline.shared.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class SynchedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
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
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public boolean containsKey(Object key) {
      this.readLock.lock();

      boolean var2;
      try {
         var2 = super.containsKey(key);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public boolean containsValue(Object value) {
      this.readLock.lock();

      boolean var2;
      try {
         var2 = super.containsValue(value);
      } finally {
         this.readLock.unlock();
      }

      return var2;
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      this.readLock.lock();

      Set var1;
      try {
         var1 = super.entrySet();
      } finally {
         this.readLock.unlock();
      }

      return var1;
   }

   @Override
   public V get(Object key) {
      this.readLock.lock();

      Object var2;
      try {
         var2 = super.get(key);
      } finally {
         this.readLock.unlock();
      }

      return (V)var2;
   }

   @Override
   public Set<K> keySet() {
      this.readLock.lock();

      Set var1;
      try {
         var1 = super.keySet();
      } finally {
         this.readLock.unlock();
      }

      return var1;
   }

   @Override
   public V put(K key, V value) {
      this.writeLock.lock();

      Object var3;
      try {
         var3 = super.put(key, value);
      } finally {
         this.writeLock.unlock();
      }

      return (V)var3;
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> map) {
      this.writeLock.lock();

      try {
         super.putAll(map);
      } finally {
         this.writeLock.unlock();
      }
   }

   @Override
   public V remove(Object key) {
      this.writeLock.lock();

      Object var2;
      try {
         var2 = super.remove(key);
      } finally {
         this.writeLock.unlock();
      }

      return (V)var2;
   }

   @Override
   public Collection<V> values() {
      this.readLock.lock();

      Collection var1;
      try {
         var1 = super.values();
      } finally {
         this.readLock.unlock();
      }

      return var1;
   }
}
