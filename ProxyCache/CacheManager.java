import java.util.*;

public class CacheManager {
    private final int cacheSize;
    private final Map<String, LinkedHashMap<String, CacheEntry>> clientCaches;
    private final long ttlMillis;

    private static class CacheEntry {
        String value;
        long expiryTime;

        CacheEntry(String value, long ttlMillis) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        long getTimestamp() {
            return expiryTime;
        }
    }

    public CacheManager(int cacheSize, long ttlMillis) {
        this.cacheSize = cacheSize;
        this.ttlMillis = ttlMillis;
        this.clientCaches = new HashMap<>();
    }

    private LinkedHashMap<String, CacheEntry> getClientCache(String clientId) {
        return clientCaches.computeIfAbsent(clientId, k -> new LinkedHashMap<>(cacheSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > cacheSize;
            }
        });
    }

    public synchronized void put(String clientId, String key, String value) {
        System.out.println("Mise en cache de : " + key);
        getClientCache(clientId).put(key, new CacheEntry(value, ttlMillis));
    }

    public synchronized String get(String clientId, String key) {
        CacheEntry entry = getClientCache(clientId).get(key);

        if (entry != null && !entry.isExpired()) {
            System.out.println("Cache trouvé pour : " + key);
            return entry.value;
        } else {
            if (entry != null) {
                System.out.println("Cache expiré pour : " + key);
                getClientCache(clientId).remove(key);
            }
            System.out.println("Cache manquant pour : " + key);
            return null;
        }
    }

    public synchronized boolean remove(String clientId, String key) {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        return getClientCache(clientId).remove(key) != null;
    }

    public synchronized void clearAll(String clientId) {
        getClientCache(clientId).clear();
    }

    public String listCache(String clientId) {
        StringBuilder responseBuilder = new StringBuilder();
        try {
            LinkedHashMap<String, CacheEntry> clientCache = getClientCache(clientId);

            if (clientCache.isEmpty()) {
                responseBuilder.append("Aucun cache n'est actuellement stocké pour le client : ").append(clientId).append("\n");
            } else {
                responseBuilder.append("Caches stockés pour le client ").append(clientId).append(" : \n");
                for (Map.Entry<String, CacheEntry> entry : clientCache.entrySet()) {
                    String path = entry.getKey();
                    CacheEntry cacheEntry = entry.getValue();
                    long timestamp = cacheEntry.getTimestamp();
                    responseBuilder.append("Chemin : ").append(path).append(", Timestamp : ").append(timestamp).append("\n");
                }
            }
        } catch (Exception e) {
            responseBuilder.append("Erreur lors de la récupération des caches : ").append(e.getMessage());
        }

        return responseBuilder.toString();
    }


}
