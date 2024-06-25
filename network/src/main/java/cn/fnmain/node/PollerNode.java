package cn.fnmain.node;

import java.lang.foreign.MemorySegment;

public interface PollerNode {
    void onReadableEvent(MemorySegment ptr, int len);
    void onWriteableEvent();
    void exit();
    void doExit();
}
