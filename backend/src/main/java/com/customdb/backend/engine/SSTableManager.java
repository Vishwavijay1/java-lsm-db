package com.customdb.backend.engine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SSTableManager {
    private final String dataDir;
    private final AtomicInteger fileCounter;
    private final List<Integer> ssTableIds;

    public SSTableManager(String dataDir) {
        this.dataDir = dataDir;
        this.ssTableIds = Collections.synchronizedList(new ArrayList<>());
        this.fileCounter = new AtomicInteger(0);

        // Scan directory for existing SSTables
        File dir = new File(dataDir);
        if (dir.exists()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".sst"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    try {
                        int id = Integer.parseInt(name.replace("data-", "").replace(".sst", ""));
                        ssTableIds.add(id);
                        if (id > fileCounter.get()) {
                            fileCounter.set(id);
                        }
                    } catch (NumberFormatException e) {
                        // Ignore malformed files
                    }
                }
                // Sort IDs descending (newest first) for search
                ssTableIds.sort(Collections.reverseOrder());
            }
        }
    }

    public synchronized void flush(ConcurrentSkipListMap<String, String> memTable) throws IOException {
        int nextId = fileCounter.incrementAndGet();
        File file = new File(dataDir, "data-" + nextId + ".sst");

        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            for (Map.Entry<String, String> entry : memTable.entrySet()) {
                byte[] keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
                byte[] valueBytes = entry.getValue().getBytes(StandardCharsets.UTF_8);

                dos.writeInt(keyBytes.length);
                dos.write(keyBytes);
                dos.writeInt(valueBytes.length);
                dos.write(valueBytes);
            }
        }

        ssTableIds.add(0, nextId); // Add to front (newest)
    }

    public String search(String key) {
        // Search all SSTables from newest to oldest
        // Note: This is slow (O(N) files). Real LSM trees use Bloom Filters and Sparse
        // Indexes.
        // We will add those in Phase 4.

        // Create a snapshot of the list to avoid concurrent modification issues
        List<Integer> snapshotIds;
        synchronized (ssTableIds) {
            snapshotIds = new ArrayList<>(ssTableIds);
        }

        for (Integer id : snapshotIds) {
            try {
                String value = searchInFile(id, key);
                if (value != null) {
                    return value;
                }
            } catch (IOException e) {
                // Log error but continue searching other files
                e.printStackTrace();
            }
        }
        return null;
    }

    private String searchInFile(int id, String key) throws IOException {
        File file = new File(dataDir, "data-" + id + ".sst");
        if (!file.exists())
            return null;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            while (raf.getFilePointer() < raf.length()) {
                int keyLen = raf.readInt();
                byte[] keyBytes = new byte[keyLen];
                raf.readFully(keyBytes);
                String currentKey = new String(keyBytes, StandardCharsets.UTF_8);

                int valueLen = raf.readInt();

                if (currentKey.equals(key)) {
                    byte[] valueBytes = new byte[valueLen];
                    raf.readFully(valueBytes);
                    return new String(valueBytes, StandardCharsets.UTF_8);
                } else {
                    // Skip value
                    raf.skipBytes(valueLen);
                }
            }
        }
        return null;
    }

    public int getSSTableCount() {
        return ssTableIds.size();
    }
}
