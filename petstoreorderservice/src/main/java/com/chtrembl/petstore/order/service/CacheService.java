package com.chtrembl.petstore.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    public int getOrdersCacheSize() {
        try {
            org.springframework.cache.concurrent.ConcurrentMapCache mapCache =
                    (org.springframework.cache.concurrent.ConcurrentMapCache) cacheManager.getCache("orders");
            return mapCache != null ? mapCache.getNativeCache().size() : 0;
        } catch (Exception e) {
            log.warn("Could not get orders cache size: {}", e.getMessage());
            return 0;
        }
    }

    // Clear cache every 12 hours (43200000 ms)
    @Scheduled(fixedRate = 43200000)
    public void evictAllCaches() {
        log.info("Evicting all caches on scheduled interval");
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }
                });
    }
}