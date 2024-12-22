package src.Cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Cache {
    private Map<String, byte[]> storedCache;
    private ArrayList<String> storedCacheName;
    private Map<String, Long> timeStored;
    private long maxCacheSize = 50;
    public long currentCacheSize;
    private int duration;
    private ScheduledExecutorService scheduler;
    private CacheManager cacheGUI;

    public void setCacheGUI(CacheManager cacheGUI) {
        this.cacheGUI = cacheGUI;
    }

    public Cache(int cacheDuration) {
        storedCache = new HashMap<>();
        storedCacheName = new ArrayList<String>();
        timeStored = new HashMap<>();
        duration = cacheDuration * 60;
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public Cache(long maxCacheSize, int cacheDuration) {
        storedCache = new LinkedHashMap<>();
        this.maxCacheSize = maxCacheSize;
        timeStored = new HashMap<>();

        storedCacheName = new ArrayList<String>();
        duration = cacheDuration * 60;
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public Cache(long maxCacheSize, int cacheDuration, CacheManager manager) {
        storedCache = new LinkedHashMap<>();
        storedCacheName = new ArrayList<String>();
        timeStored = new HashMap<>();
        this.maxCacheSize = maxCacheSize * 1024 * 1024;
        duration = cacheDuration * 60;
        scheduler = Executors.newScheduledThreadPool(1);
        this.cacheGUI = manager;
    }

    public  void addToCache(String name, byte[] content) {
        long dataSize = content.length;
        if (name != null)
            dataSize += name.length();

        String tmpName = name;
        if (tmpName == "" || tmpName == null)
            tmpName = "null";

        while (currentCacheSize + dataSize > maxCacheSize && dataSize < maxCacheSize)
            removeOldestEntry();

        if (!(dataSize >= maxCacheSize)) {
            storedCache.put(tmpName, content);
            storedCacheName.add(tmpName);
            timeStored.put(tmpName, System.currentTimeMillis());
            currentCacheSize += dataSize;
            final String tmpNameN = tmpName;
            scheduler.schedule(() -> removeFromCache(tmpNameN), (long) duration, TimeUnit.SECONDS);
            if (cacheGUI != null) {
                cacheGUI.reloadPanel();
            }
        }
    }

    private void removeOldestEntry() {
        if (!storedCache.isEmpty()) {
            // Supprimer la première entrée (la plus ancienne)

            String oldestKey = storedCacheName.get(0);

            currentCacheSize -= oldestKey.length() + storedCache.get(oldestKey).length;
            storedCache.remove(storedCacheName.get(0));
            storedCacheName.remove(0);
        }
    }

    public byte[] getInCache(String name) {
        if(name==null || name=="")name="null";
        if (storedCache.get(name) != null) {
            return storedCache.get(name);
        } else {
            return null;
        }
    }

    public  boolean isIncache(String element) {
        if (element == "")
            element = "null";

        return storedCache.containsKey(element);
    }

    public void removeFromCache(String name) {
        storedCache.remove(name);
        storedCacheName.remove(name);
        System.out.println(name + " removed from cache");
    }

    public long getCacheSize() {
        return currentCacheSize;
    }

    public long getCacheSize(String tmpName) {
        long dataSize = storedCache.get(tmpName).length;
        if (tmpName != null && tmpName != "") {
            dataSize += tmpName.length();
        }
        return dataSize;
    }

    public long getMaxCacheSize() {
        return maxCacheSize;
    }

    public ArrayList<String> getCaheContent() {
        return storedCacheName;
    }

}
