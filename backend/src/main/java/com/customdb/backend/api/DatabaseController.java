package com.customdb.backend.api;

import com.customdb.backend.engine.DatabaseEngine;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Allow Next.js frontend
public class DatabaseController {

    private final DatabaseEngine databaseEngine;

    public DatabaseController(DatabaseEngine databaseEngine) {
        this.databaseEngine = databaseEngine;
    }

    @PostMapping("/set")
    public String set(@RequestBody Map<String, String> payload) throws IOException {
        String key = payload.get("key");
        String value = payload.get("value");
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and Value are required");
        }
        databaseEngine.set(key, value);
        return "OK";
    }

    @GetMapping("/get")
    public String get(@RequestParam String key) {
        String value = databaseEngine.get(key);
        return value != null ? value : "NOT_FOUND";
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return databaseEngine.getStats();
    }
}
