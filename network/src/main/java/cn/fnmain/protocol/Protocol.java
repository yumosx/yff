package cn.fnmain.protocol;


import java.lang.foreign.MemorySegment;

public interface Protocol {
    int onReadableEvent(MemorySegment reserved, int len);

    int onWritableEvent();

    int doWrite(MemorySegment data, int len);

    void doShutdown();

    void doClose();

}
