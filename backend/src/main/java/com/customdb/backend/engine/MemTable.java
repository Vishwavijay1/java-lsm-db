package com.customdb.backend.engine;

import java.util.concurrent.ConcurrentSkipListMap;

public class MemTable {
    private final ConcurrentSkipListMap<String, String> map;
    private long sizeInBytes;

    public MemTable() {
        this.map = new ConcurrentSkipListMap<>();
        this.sizeInBytes = 0;
    }

    public void put(String key, String value) {
        map.put(key, value);
        // TODO: Update sizeInBytes
    }

    public String get(String key) {
        return map.get(key);
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void clear() {
        map.clear();
        sizeInBytes = 0;
    }
    
    public ConcurrentSkipListMap<String, String> getMap() {
        return map;
    }
}
