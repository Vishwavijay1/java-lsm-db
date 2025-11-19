package com.customdb.backend.engine;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListMap;

public class SSTableManager {
    private final String dataDir;

    public SSTableManager(String dataDir) {
        this.dataDir = dataDir;
    }

    public void flush(ConcurrentSkipListMap<String, String> memTable) throws IOException {
        // TODO: Write MemTable to disk as SSTable
    }

    public String search(String key) {
        // TODO: Search in SSTables
        return null;
    }
}
