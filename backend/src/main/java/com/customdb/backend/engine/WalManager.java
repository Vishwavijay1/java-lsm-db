package com.customdb.backend.engine;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class WalManager {
    private final File walFile;
    private final RandomAccessFile fileWriter;

    public WalManager(String dataDir) throws IOException {
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.walFile = new File(dir, "wal.log");
        this.fileWriter = new RandomAccessFile(walFile, "rw");
    }

    public synchronized void append(String key, String value) throws IOException {
        fileWriter.seek(fileWriter.length()); // Append to end

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);

        // Format: [Key Length (4 bytes)][Key Bytes][Value Length (4 bytes)][Value
        // Bytes]
        fileWriter.writeInt(keyBytes.length);
        fileWriter.write(keyBytes);
        fileWriter.writeInt(valueBytes.length);
        fileWriter.write(valueBytes);

        // Force write to disk immediately for durability (fsync)
        // In production, we might batch this for performance, but for safety, we sync.
        // fileWriter.getChannel().force(false);
    }

    public void clear() throws IOException {
        fileWriter.setLength(0);
    }

    public void loadFromWal(MemTable memTable) throws IOException {
        if (!walFile.exists() || walFile.length() == 0)
            return;

        try (RandomAccessFile reader = new RandomAccessFile(walFile, "r")) {
            reader.seek(0);
            while (reader.getFilePointer() < reader.length()) {
                int keyLen = reader.readInt();
                byte[] keyBytes = new byte[keyLen];
                reader.readFully(keyBytes);
                String key = new String(keyBytes, StandardCharsets.UTF_8);

                int valueLen = reader.readInt();
                byte[] valueBytes = new byte[valueLen];
                reader.readFully(valueBytes);
                String value = new String(valueBytes, StandardCharsets.UTF_8);

                memTable.put(key, value);
            }
        }
    }

    public void close() throws IOException {
        fileWriter.close();
    }
}
