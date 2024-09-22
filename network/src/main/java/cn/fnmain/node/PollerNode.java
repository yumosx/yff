package cn.fnmain.node;

import cn.fnmain.thread.PollerTask;

import java.lang.foreign.MemorySegment;
import java.time.Duration;

public interface PollerNode {
    void onReadableEvent(MemorySegment segment, int len);

    void onWriteableEvent();

    void onRegisterTaggedMsg(PollerTask pollerTask);

    void onUnRegisterTaggedMsg(PollerTask pollerTask);

    void onClose(PollerTask pollerTask);

    void exit(Duration duration);
}
