package com.customdb.backend.engine;

import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class DatabaseEngine {
    private final MemTable memTable;
    private final WalManager walManager;
    private final SSTableManager ssTableManager;

    public DatabaseEngine() {
        this.memTable = new MemTable();
        this.walManager = new WalManager("data"); // TODO: Configurable path
        this.ssTableManager = new SSTableManager("data");
    }

    public synchronized void set(String key, String value) throws IOException {
        // 1. Append to WAL
        walManager.append(key, value);
        // 2. Write to MemTable
        memTable.put(key, value);
        // 3. Check flush
        // if (memTable.getSizeInBytes() > THRESHOLD) { ... }
    }

    public String get(String key) {
        // 1. Check MemTable
        String value = memTable.get(key);
        if (value != null)
            return value;

        // 2. Check SSTables
        return ssTableManager.search(key);
    }
}
