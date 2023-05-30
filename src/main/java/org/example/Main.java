package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.example.client.CuratorClient;

import java.util.HashSet;
import java.util.Scanner;

/**
 * main.
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        CuratorClient curatorClient = new CuratorClient();
        // 这里直接监听根节点，实际上几个分布式任务的节点应该移出监听范围
        curatorClient.addCache("/", (cache, cachePaths, event) -> {
            log.info("基于{}节点的监听触发", "/");
            log.info("节点发生更改: {}\n更改类型: {}", event.getData().getPath(), event.getType());
            HashSet<String> paths = cachePaths.get("/");
            // cache刚建立时，内部数据为空，curator从zk读取数据，并触发监听，监听事件为add类型.
            if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                if (null == paths) {
                    paths = new HashSet<>();
                    paths.add(event.getData().getPath());
                    cachePaths.put("/", paths);
                } else {
                    if (!paths.contains(event.getData().getPath())) {
                        paths.add(event.getData().getPath());
                    } else {
                        log.info("删除{}节点后又新增了该节点", event.getData().getPath());
                    }
                }
            }
        });
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            if ("exit".equalsIgnoreCase(sc.next())) {
                return;
            }
        }
    }
}
