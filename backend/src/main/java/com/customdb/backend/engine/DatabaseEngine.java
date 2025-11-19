package com.customdb.backend.engine;

import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class DatabaseEngine {
    private final MemTable memTable;
    private final WalManager walManager;
    private final SSTableManager ssTableManager;

    public DatabaseEngine() throws IOException {
        this.memTable = new MemTable();
        this.walManager = new WalManager("data"); // TODO: Configurable path
        this.ssTableManager = new SSTableManager("data");

        // Recover from WAL on startup
        this.walManager.loadFromWal(memTable);
    }

    public synchronized void set(String key, String value) throws IOException {
        // 1. Append to WAL
        walManager.append(key, value);
        // 2. Write to MemTable
        memTable.put(key, value);

        // 3. Check flush (Threshold: 1MB for demo purposes)
        if (memTable.getSizeInBytes() > 1024 * 1024) {
            flush();
        }
    }

    private void flush() throws IOException {
        System.out.println("Flushing MemTable to disk...");
        ssTableManager.flush(memTable.getMap());
        memTable.clear();
        walManager.clear();
        System.out.println("Flush complete.");
    }

    public String get(String key) {
        // 1. Check MemTable
        String value = memTable.get(key);
        if (value != null)
            return value;
        // 2. Check SSTables
        return ssTableManager.search(key);
    }

    public java.util.Map<String, Object> getStats() {
        return java.util.Map.of(
                "memTableSize", memTable.getSizeInBytes(),
                "sstableCount", ssTableManager.getSSTableCount());
    }
}
