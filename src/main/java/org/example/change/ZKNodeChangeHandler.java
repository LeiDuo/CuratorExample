package org.example.change;

import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

import java.util.HashSet;
import java.util.Map;

/**
 * 监听触发,回调对应处理方法.
 */
public interface ZKNodeChangeHandler {

    void onChanged(CuratorCache cache, Map<String, HashSet<String>> cachePaths, TreeCacheEvent event);
}
