package org.example.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.example.change.ZKNodeChangeHandler;
import org.example.ex.CuratorInitException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.example.constant.ZKConstant.CONNECTION_TIMEOUT_MS;
import static org.example.constant.ZKConstant.MAX_RETRIES;
import static org.example.constant.ZKConstant.RETRY_INTERVAL_MS;
import static org.example.constant.ZKConstant.SESSION_TIMEOUT_MS;

/**
 * Curator提供的zk client.
 */
public class CuratorClient {

    private final Map<String, CuratorCache> caches = new HashMap<>();

    private final CuratorFramework client;

    public final Map<String, HashSet<String>> cachePaths = new ConcurrentHashMap<>();

    public CuratorClient() {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder.connectString("127.0.0.1:2181")
                .retryPolicy(new ExponentialBackoffRetry(RETRY_INTERVAL_MS, MAX_RETRIES, RETRY_INTERVAL_MS * MAX_RETRIES))
                .namespace("governance_ds")
                .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                .connectionTimeoutMs(CONNECTION_TIMEOUT_MS);
        client = builder.build();
        initCuratorClient();
    }

    private void initCuratorClient() {
        client.start();
        // 等待连接建立
        try {
            if (!client.blockUntilConnected(RETRY_INTERVAL_MS * MAX_RETRIES, TimeUnit.MILLISECONDS)) {
                client.close();
                throw new KeeperException.OperationTimeoutException();
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CuratorInitException(ex);
        } catch (final KeeperException.OperationTimeoutException ex) {
            throw new CuratorInitException(ex);
        }
    }

    public void addCache(final String rawKey, final ZKNodeChangeHandler handler) {
        String key = rawKey.toLowerCase();
        if (!caches.containsKey(key)) {
            addCacheData(key);
            CuratorCache cache = caches.get(key);
            CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder()
                    .forTreeCache(client, (framework, treeCacheEvent) -> {
                        TreeCacheEvent.Type changedType = treeCacheEvent.getType();
                        switch (changedType) {
                            case NODE_ADDED:
                            case NODE_UPDATED:
                            case NODE_REMOVED: {
                                handler.onChanged(cache, cachePaths, treeCacheEvent);
                                break;
                            }
                            default:
                                break;
                        }
                    }).build();
            cache.listenable().addListener(curatorCacheListener);
            start(cache);
        }
    }

    private void addCacheData(final String cachePath) {
        CuratorCache cache = CuratorCache.build(client, cachePath);
        caches.put(cachePath, cache);
    }

    private void start(final CuratorCache cache) {
        try {
            cache.start();
        } catch (final Exception ex) {
            throw new CuratorInitException(ex);
        }
    }
}
