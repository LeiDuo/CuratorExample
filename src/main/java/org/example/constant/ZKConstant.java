package org.example.constant;

/**
 * zk启动配置参数.
 */
public class ZKConstant {

    // curator client 连接zk的重试间隔时间
    public static final int RETRY_INTERVAL_MS = 500;

    // curator client 连接zk的最大重试次数
    public static final int MAX_RETRIES = 3;

    // curator client 会话超时时间
    public static final int SESSION_TIMEOUT_MS = 60 * 1000;

    // curator client 建立连接超时时间
    public static final int CONNECTION_TIMEOUT_MS = 5000;
}
