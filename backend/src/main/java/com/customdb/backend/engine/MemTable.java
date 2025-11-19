package com.customdb.backend.engine;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class MemTable {
    private final ConcurrentSkipListMap<String, String> map;
    private final AtomicLong sizeInBytes;

    public MemTable() {
        this.map = new ConcurrentSkipListMap<>();
        this.sizeInBytes = new AtomicLong(0);
    }

    public void put(String key, String value) {
        // Calculate the size of the new entry
        long keySize = key.getBytes(StandardCharsets.UTF_8).length;
        long valueSize = value.getBytes(StandardCharsets.UTF_8).length;
        long entrySize = keySize + valueSize;

        // If key exists, we need to subtract the old value's size
        // Note: This is a bit tricky in a concurrent map without locking.
        // For simplicity in this LSM implementation, we'll just add the new size.
        // In a production system, we'd need to handle updates more precisely or accept
        // slight inaccuracy.
        // However, since LSM trees are append-only conceptually, we can just track the
        // total size of data added.
        // But to be more accurate for the "flush" trigger, let's try to handle
        // replacement if possible,
        // or just accept that "size" means "approximate memory usage".

        // Let's do a simple put and add size.
        // If we overwrite, the map size doesn't grow by key size, only value
        // difference.
        // But checking containsKey is not atomic with put.
        // Let's use compute() for atomicity if we want strict size, but for high
        // throughput,
        // we often just approximate or use an append-only log approach.

        // For this project, let's stick to a simple approximation:
        // We add the size of the key + value. If we overwrite, we might overestimate
        // slightly until flush.
        // This is acceptable for a flush trigger.

        map.put(key, value);
        sizeInBytes.addAndGet(entrySize);
    }

    public String get(String key) {
        return map.get(key);
    }

    public long getSizeInBytes() {
        return sizeInBytes.get();
    }

    public void clear() {
        map.clear();
        sizeInBytes.set(0);
    }

    public ConcurrentSkipListMap<String, String> getMap() {
        return map;
    }
}
