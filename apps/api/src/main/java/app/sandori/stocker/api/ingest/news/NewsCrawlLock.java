package app.sandori.stocker.api.ingest.news;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NewsCrawlLock {

    private final Set<String> running = ConcurrentHashMap.newKeySet();

    public boolean tryLock(String providerId) {
        return running.add(providerId);
    }

    public void unlock(String providerId) {
        running.remove(providerId);
    }

    public boolean isRunning(String providerId) {
        return running.contains(providerId);
    }

    public Map<String, String> allStatus(java.util.List<String> providerIds) {
        Map<String, String> status = new java.util.LinkedHashMap<>();
        for (String id : providerIds) {
            status.put(id, running.contains(id) ? "running" : "idle");
        }
        return status;
    }
}
