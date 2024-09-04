package cn.fnmain;

import cn.fnmain.protocol.Sentry;

public interface Provider {
    Sentry create();
    default void close() {
    }
}
