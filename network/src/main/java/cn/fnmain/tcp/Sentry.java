package cn.fnmain.tcp;

import java.lang.foreign.MemorySegment;

public interface Sentry {
    int onReadableEvent(MemorySegment reversed, int offset);
    int onWritableEvent();
    Protocol toProtocol();
    void doClose();
}
