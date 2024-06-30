package cn.fnmain;

import cn.fnmain.tcp.Sentry;

public interface Provider {
    Sentry create();
    default void close() {

    }
}
