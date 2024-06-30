package cn.fnmain.node;

import cn.fnmain.thread.PollerTask;

import java.lang.foreign.MemorySegment;
import java.time.Duration;

public interface PollerNode {
    void onReadableEvent(MemorySegment ptr, int len);
    //这个函数的作用是让poller节点向writer节点转变
    void onWriteableEvent();

    void onClose(PollerTask pollerTask);
    //退出当前的poller节点
    void exit(Duration duration);
}
