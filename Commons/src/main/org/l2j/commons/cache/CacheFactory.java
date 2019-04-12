package org.l2j.commons.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Objects.isNull;

public class CacheFactory {

    private static CacheFactory instance;
    private CacheManager manager;

    private CacheFactory() {

    }

    public static CacheFactory getInstance() {
        if(isNull(instance)) {
            instance = new CacheFactory();
        }
        return  instance;
    }

    public void initialize(String configurationFilePath) {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        manager = cachingProvider.getCacheManager(Path.of(configurationFilePath).toUri(), getClass().getClassLoader());
    }

    public <K, V> Cache<K, V> getCache(String alias) {
        checkInitilized();
        return manager.getCache(alias);
    }

    public <K, V> Cache<K, V> getCache(String alias, Class<K> keyClass, Class<V> valueClass) {
        checkInitilized();
        return manager.getCache(alias, keyClass, valueClass);
    }

    private void checkInitilized() {
        if (isNull(manager)) {
            throw new IllegalStateException("CacheFactory not initialized. Call method initialize before use it");
        }
    }

}
