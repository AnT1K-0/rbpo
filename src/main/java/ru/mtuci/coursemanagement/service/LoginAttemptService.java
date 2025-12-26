package ru.mtuci.coursemanagement.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt a = attempts.get(key);
        if (a == null) return false;

        if (Instant.now().isAfter(a.windowStart.plus(WINDOW))) {
            attempts.remove(key);
            return false;
        }
        return a.count >= MAX_ATTEMPTS;
    }

    public void onFailure(String key) {
        attempts.compute(key, (k, old) -> {
            Instant now = Instant.now();
            if (old == null || now.isAfter(old.windowStart.plus(WINDOW))) {
                return new Attempt(1, now);
            }
            return new Attempt(old.count + 1, old.windowStart);
        });
    }

    public void onSuccess(String key) {
        attempts.remove(key);
    }

    public record Attempt(int count, Instant windowStart) {}
}
